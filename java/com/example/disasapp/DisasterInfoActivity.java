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
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;


import com.example.disasapp.ml.DisasterDamageModel;

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


public class DisasterInfoActivity extends AppCompatActivity {

    private static final int REQUEST_PRE_IMAGE = 1;
    private static final int REQUEST_POST_IMAGE = 2;

    private ImageView preImageView, postImageView;
    private TextView preDamageTextView, postDamageTextView;
    private Button uploadPreImageButton, uploadPostImageButton, backButton;

    private Bitmap preImage, postImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_disaster_info);

        // Initialize views
        preImageView = findViewById(R.id.imageViewPreDisaster);
        postImageView = findViewById(R.id.imageViewPostDisaster);
        preDamageTextView = findViewById(R.id.textViewPreDamagePercentage);
        postDamageTextView = findViewById(R.id.textViewPostDamagePercentage);
        uploadPreImageButton = findViewById(R.id.uploadPreImageButton);
        uploadPostImageButton = findViewById(R.id.uploadPostImageButton);
        backButton = findViewById(R.id.backButton);

        // Set up listeners for image upload buttons
        uploadPreImageButton.setOnClickListener(v -> openImagePicker(REQUEST_PRE_IMAGE));
        uploadPostImageButton.setOnClickListener(v -> openImagePicker(REQUEST_POST_IMAGE));

        // Back button functionality
        backButton.setOnClickListener(v -> onBackPressed());
    }

    private void openImagePicker(int requestCode) {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
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
                    calculateDamagePercentage(preImage, true);  // Calculate pre-disaster damage
                } else if (requestCode == REQUEST_POST_IMAGE) {
                    postImage = Bitmap.createScaledBitmap(bitmap, 256, 256, true);
                    postImageView.setImageBitmap(postImage);
                    calculateDamagePercentage(postImage, false);  // Calculate post-disaster damage
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void calculateDamagePercentage(Bitmap image, boolean isPreDisaster) {
        try {
            // Convert Bitmap to ByteBuffer
            ByteBuffer byteBuffer = convertBitmapToByteBuffer(image);

            // Load model (assuming disaster model is available)
            DisasterDamageModel model = DisasterDamageModel.newInstance(this);

            // Prepare input TensorBuffer
            TensorBuffer inputFeature0 = TensorBuffer.createFixedSize(new int[]{1, 256, 256, 3}, DataType.FLOAT32);
            inputFeature0.loadBuffer(byteBuffer);

            // Run inference
            DisasterDamageModel.Outputs outputs = model.process(inputFeature0);
            TensorBuffer outputFeature0 = outputs.getOutputFeature0AsTensorBuffer();

            // Assuming the model returns an array of floats (damage percentages)
            float[] damagePercentages = outputFeature0.getFloatArray();

            // Format the result
            StringBuilder resultText = new StringBuilder();

            // Display each damage percentage as a string
            for (int i = 2; i < damagePercentages.length-1; i++) {
                resultText.append("Building Percentage" + damagePercentages[i] + "%\n");
            }

            // Display the result
            if (isPreDisaster) {
                preDamageTextView.setText(resultText.toString());
            } else {
                postDamageTextView.setText(resultText.toString());
            }

            // Release model resources if no longer needed
            model.close();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error processing image", Toast.LENGTH_SHORT).show();
        }
    }



    private ByteBuffer convertBitmapToByteBuffer(Bitmap bitmap) {
        // Bitmap to ByteBuffer conversion
        int size = 256;
        ByteBuffer buffer = ByteBuffer.allocateDirect(4 * size * size * 3);  // 3 channels (RGB)
        buffer.order(ByteOrder.nativeOrder());

        int[] pixels = new int[size * size];
        bitmap.getPixels(pixels, 0, size, 0, 0, size, size);

        for (int pixel : pixels) {
            // Normalize RGB values to [0, 1]
            buffer.putFloat(((pixel >> 16) & 0xFF) / 255.0f);  // Red
            buffer.putFloat(((pixel >> 8) & 0xFF) / 255.0f);   // Green
            buffer.putFloat((pixel & 0xFF) / 255.0f);          // Blue
        }

        return buffer;
    }
}