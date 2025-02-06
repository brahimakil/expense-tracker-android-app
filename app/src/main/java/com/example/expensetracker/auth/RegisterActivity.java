package com.example.expensetracker.auth;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.example.expensetracker.MainActivity;
import com.example.expensetracker.R;
import com.google.android.material.textfield.TextInputEditText;

public class RegisterActivity extends AppCompatActivity {
    private TextInputEditText emailInput;
    private TextInputEditText passwordInput;
    private TextInputEditText confirmPasswordInput;
    private Button registerButton;
    private TextView loginLink;
    private ProgressBar progressBar;
    private AuthManager authManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        initializeViews();
        setupClickListeners();
        authManager = AuthManager.getInstance();
    }

    private void initializeViews() {
        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        confirmPasswordInput = findViewById(R.id.confirmPasswordInput);
        registerButton = findViewById(R.id.registerButton);
        loginLink = findViewById(R.id.loginLink);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupClickListeners() {
        registerButton.setOnClickListener(v -> attemptRegistration());
        loginLink.setOnClickListener(v -> finish());
    }

    private void attemptRegistration() {
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();
        String confirmPassword = confirmPasswordInput.getText().toString().trim();

        if (validateInput(email, password, confirmPassword)) {
            showLoading(true);
            authManager.signUp(email, password, new AuthCallback() {
                @Override
                public void onSuccess() {
                    showLoading(false);
                    authManager.initializeUser()
                        .addOnSuccessListener(aVoid -> {
                            startActivity(new Intent(RegisterActivity.this, MainActivity.class));
                            finishAffinity();
                        })
                        .addOnFailureListener(e -> {
                            showLoading(false);
                            Toast.makeText(RegisterActivity.this, 
                                "Error initializing user: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                        });
                }

                @Override
                public void onError(Exception e) {
                    showLoading(false);
                    Toast.makeText(RegisterActivity.this, "Registration failed: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private boolean validateInput(String email, String password, String confirmPassword) {
        if (email.isEmpty()) {
            emailInput.setError("Email is required");
            return false;
        }
        if (password.isEmpty()) {
            passwordInput.setError("Password is required");
            return false;
        }
        if (!password.equals(confirmPassword)) {
            confirmPasswordInput.setError("Passwords do not match");
            return false;
        }
        if (password.length() < 6) {
            passwordInput.setError("Password must be at least 6 characters");
            return false;
        }
        return true;
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        registerButton.setEnabled(!show);
    }
} 