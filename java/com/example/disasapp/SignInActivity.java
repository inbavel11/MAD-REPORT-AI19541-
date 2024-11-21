package com.example.disasapp;
// SignInActivity.java
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
public class SignInActivity extends AppCompatActivity {

    private EditText usernameInput;
    private EditText passwordInput;
    private Button registerButton;
    private Button signInButton;
    private Button chatButton;
    private Button aboutButton;

    // SharedPreferences to store user credentials
    private SharedPreferences sharedPreferences;
    private static final String PREF_NAME = "UserCredentials";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_PASSWORD = "password";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        // Initialize views
        usernameInput = findViewById(R.id.usernameInput);
        passwordInput = findViewById(R.id.passwordInput);
        registerButton = findViewById(R.id.registerButton);
        signInButton = findViewById(R.id.signInButton);
        chatButton = findViewById(R.id.chatButton);
        aboutButton = findViewById(R.id.aboutButton);

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE);

        // Set button click listeners
        registerButton.setOnClickListener(v -> registerUser());
        signInButton.setOnClickListener(v -> signInUser());
        chatButton.setOnClickListener(v -> startChat());
        aboutButton.setOnClickListener(v -> viewAbout());
    }

    private void registerUser() {
        String username = usernameInput.getText().toString();
        String password = passwordInput.getText().toString();

        if (isValidUsername(username) && isValidPassword(password)) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(KEY_USERNAME, username);
            editor.putString(KEY_PASSWORD, password);
            editor.apply();

            Toast.makeText(this, "Registration successful! You may now sign in.", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Username or Password doesn't meet requirements.", Toast.LENGTH_SHORT).show();
        }
    }

    private void signInUser() {
        String username = usernameInput.getText().toString();
        String password = passwordInput.getText().toString();

        String savedUsername = sharedPreferences.getString(KEY_USERNAME, null);
        String savedPassword = sharedPreferences.getString(KEY_PASSWORD, null);

        if (username.equals(savedUsername) && password.equals(savedPassword)) {
            Toast.makeText(this, "Sign in successful!", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(SignInActivity.this, MainActivity.class));
            finish();
        } else {
            Toast.makeText(this, "Invalid credentials. Please try again.", Toast.LENGTH_SHORT).show();
        }
    }

    private void startChat() {
        Intent intent = new Intent(SignInActivity.this, WeatherActivity.class);
        startActivity(intent);
    }

    private void viewAbout() {
        Intent intent = new Intent(SignInActivity.this, AboutActivity.class);
        startActivity(intent);
    }

    private boolean isValidUsername(String username) {
        return username.matches("^[a-zA-Z]+$");
    }

    private boolean isValidPassword(String password) {
        return password.length() >= 8 &&
                password.matches(".*[A-Z].*") &&
                password.matches(".*\\d.*") &&
                password.matches(".*[!@#$%^&*()].*");
    }
}