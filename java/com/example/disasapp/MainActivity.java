package com.example.disasapp;

import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.disasapp.ml.DisasterDamageAssessmentModel;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.text.BreakIterator;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_PRE_IMAGE = 1;
    private static final int REQUEST_POST_IMAGE = 2;

    private ImageView preImageView;
    private ImageView postImageView;
    private ImageView maskImageView;
    private TextView damageLevelTextView;
    private TextView damagePercentageTextView;
    private TextView damageMetricsTextView;

    private Bitmap preImage;
    private Bitmap postImage;
    private Spinner modeSpinner;
    private SeekBar thresholdSeekBar;
    private TextView thresholdTextView;
    private float thresholdPercentage = 0.90f; // Initial threshold at 10%
    private String selectedMode = "Grayscale";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        preImageView = findViewById(R.id.preImageView);
        postImageView = findViewById(R.id.postImageView);
        maskImageView = findViewById(R.id.maskImageView);
        damageLevelTextView = findViewById(R.id.damageLevelTextView);
        damagePercentageTextView = findViewById(R.id.damagePercentageTextView);
        damageMetricsTextView = findViewById(R.id.damageMetricsTextView);

        Button uploadPreButton = findViewById(R.id.uploadPreButton);
        Button uploadPostButton = findViewById(R.id.uploadPostButton);
        Button assessDamageButton = findViewById(R.id.assessDamageButton);
        modeSpinner = findViewById(R.id.modeSpinner);
        Button viewDisasterInfoButton = findViewById(R.id.viewDisasterInfoButton);

        // Set button listener to navigate to DisasterInfoActivity
        viewDisasterInfoButton.setOnClickListener(v -> {
            // Start the DisasterInfoActivity
            startActivity(new Intent(MainActivity.this, DisasterInfoActivity.class));
        });
        modeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedMode = parent.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        uploadPreButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGallery(REQUEST_PRE_IMAGE);
            }
        });

        uploadPostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGallery(REQUEST_POST_IMAGE);
            }
        });

        assessDamageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (postImage != null) {
                    assessDamage(postImage);
                }
            }
        });
    }

    private void openGallery(int requestCode) {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, requestCode);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null) {
            Uri selectedImage = data.getData();
            try {
                InputStream imageStream = getContentResolver().openInputStream(selectedImage);
                Bitmap bitmap = BitmapFactory.decodeStream(imageStream);
                if (requestCode == REQUEST_PRE_IMAGE) {
                    preImage = Bitmap.createScaledBitmap(bitmap, 256, 256, true);
                    preImageView.setImageBitmap(preImage);
                } else if (requestCode == REQUEST_POST_IMAGE) {
                    postImage = Bitmap.createScaledBitmap(bitmap, 256, 256, true);
                    postImageView.setImageBitmap(postImage);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void assessDamage(Bitmap postImage) {
        try {
            DisasterDamageAssessmentModel model = DisasterDamageAssessmentModel.newInstance(this);

            TensorBuffer inputBuffer = TensorBuffer.createFixedSize(new int[]{1, 256, 256, 3}, DataType.FLOAT32);
            ByteBuffer byteBuffer = ByteBuffer.allocateDirect(4 * 256 * 256 * 3);
            byteBuffer.order(ByteOrder.nativeOrder());

            int[] intValues = new int[256 * 256];
            postImage.getPixels(intValues, 0, postImage.getWidth(), 0, 0, postImage.getWidth(), postImage.getHeight());

            for (int pixelValue : intValues) {
                byteBuffer.putFloat(((pixelValue >> 16) & 0xFF) / 255.0f);
                byteBuffer.putFloat(((pixelValue >> 8) & 0xFF) / 255.0f);
                byteBuffer.putFloat((pixelValue & 0xFF) / 255.0f);
            }

            inputBuffer.loadBuffer(byteBuffer);

            DisasterDamageAssessmentModel.Outputs outputs = model.process(inputBuffer);
            TensorBuffer outputBuffer = outputs.getOutputFeature0AsTensorBuffer();
            ByteBuffer outputByteBuffer = outputBuffer.getBuffer();
            outputByteBuffer.rewind();

            int width = 256;
            int height = 256;
            int[] colors = new int[width * height];
            int damagedPixels = 0;
            int totalPixels = width * height;

            for (int i = 0; i < width * height; i++) {
                float value = outputByteBuffer.getFloat();
                float normalizedValue = Math.min(Math.max(value, 0.0f), 1.0f);

                // Apply threshold and selected mode
                if (normalizedValue > thresholdPercentage) {
                    damagedPixels++;
                    colors[i] = getModeColor(normalizedValue);
                } else {
                    colors[i] = Color.TRANSPARENT;
                }
            }

            Bitmap maskBitmap = Bitmap.createBitmap(colors, width, height, Bitmap.Config.ARGB_8888);
            maskImageView.setImageBitmap(maskBitmap);

            float damagePercentage = (damagedPixels/ (float) totalPixels) * 100;
            damagePercentageTextView.setText(String.format("Damage Percentage: %.2f%%", damagePercentage));

            String damageLevel = (damagePercentage <= 40) ? "Low" : (damagePercentage <= 70) ? "Medium" : "High";
            damageLevelTextView.setText(String.format("Damage Level: %s", damageLevel));
            String metrics = getMetricsFromCSV(damagePercentage);
            damageMetricsTextView.setText(metrics);


            model.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private int getModeColor(float normalizedValue) {
        int grayValue = (int) (normalizedValue * 255);

        switch (selectedMode) {
            case "Grayscale":
                return 0xFF000000 | (grayValue << 16) | (grayValue << 8) | grayValue;
            case "Dark Grayscale":
                int darkGrayValue = (int) (grayValue * 0.5);
                return 0xFF000000 | (darkGrayValue << 16) | (darkGrayValue << 8) | darkGrayValue;
            case "Blue Tint":
                int blueGrayValue = (int) (grayValue * 0.3);
                return 0xFF000000 | (blueGrayValue << 16) | (blueGrayValue << 8) | (blueGrayValue + 50);
            case "Red Tint":
                int redGrayValue = (int) (grayValue * 0.3);
                return 0xFF000000 | (redGrayValue + 50 << 16) | (redGrayValue << 8) | redGrayValue;
            default:
                return 0xFF000000 | (grayValue << 16) | (grayValue << 8) | grayValue;
        }
    }
    private ByteBuffer createByteBufferFromDamagePercentage(float damagePercentage) {
        // Allocate a ByteBuffer with a size of 4 bytes (since a float is 4 bytes)
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(4);
        byteBuffer.order(ByteOrder.nativeOrder()); // Set the byte order to the native order of the platform

        // Put the damage percentage into the ByteBuffer as a float
        byteBuffer.putFloat(damagePercentage);

        // Rewind the ByteBuffer to prepare it for reading
        byteBuffer.rewind();

        return byteBuffer;
    }


    private String getMetricsFromCSV(float damagePercentage) {
        StringBuilder metrics = new StringBuilder("Damage Metrics:\n");
        try {
            AssetManager assetManager = getAssets();
            InputStream inputStream = ((AssetManager) assetManager).open("recovery.csv");
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            String[] headers = reader.readLine().split(","); // Read the header line
            float closestMatch = Float.MAX_VALUE;
            Map<String, String> closestRow = new HashMap<>();

            // Read each line and find the closest match to damagePercentage
            while ((line = reader.readLine()) != null) {
                String[] columns = line.split(",");
                float rowDamagePercentage = Float.parseFloat(columns[0]);

                if (Math.abs(rowDamagePercentage - damagePercentage) < Math.abs(closestMatch - damagePercentage)) {
                    closestMatch = rowDamagePercentage;
                    for (int i = 0; i < columns.length; i++) {
                        closestRow.put(headers[i], columns[i]);
                    }
                }
            }

            // Format and append the closest row's data to metrics string
            for (String header : headers) {
                metrics.append(header).append(": ").append(closestRow.get(header)).append("\n");
            }

            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
            metrics.append("Error loading data.");
        }

        return metrics.toString();
    }

}