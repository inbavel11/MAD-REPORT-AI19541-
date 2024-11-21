package com.example.disasapp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class AboutActivity extends AppCompatActivity {

    private TextView aboutTitle, aboutDescription, creatorName1, creatorDeptYear1, creatorName2, creatorDeptYear2;
    private Button creatorLinkedIn1, creatorLinkedIn2;
    private ImageView creatorImage1, creatorImage2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);  // Link to the layout file

        // Initialize the UI elements
        aboutTitle = findViewById(R.id.aboutTitle);
        aboutDescription = findViewById(R.id.aboutDescription);
        creatorName1 = findViewById(R.id.creatorName1);
        creatorDeptYear1 = findViewById(R.id.creatorDeptYear1);
        creatorName2 = findViewById(R.id.creatorName2);
        creatorDeptYear2 = findViewById(R.id.creatorDeptYear2);
        creatorLinkedIn1 = findViewById(R.id.creatorLinkedIn1);
        creatorLinkedIn2 = findViewById(R.id.creatorLinkedIn2);
        creatorImage1 = findViewById(R.id.creatorImage1);
        creatorImage2 = findViewById(R.id.creatorImage2);


        // Setup LinkedIn button click listeners
        creatorLinkedIn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openLinkedIn("https://www.linkedin.com/in/inbavel-s");  // Replace with real LinkedIn URL
            }
        });

        creatorLinkedIn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openLinkedIn("https://www.linkedin.com/in/creator2");  // Replace with real LinkedIn URL
            }
        });
    }

    // Method to open LinkedIn profile in the browser
    private void openLinkedIn(String url) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(intent);
    }
}
