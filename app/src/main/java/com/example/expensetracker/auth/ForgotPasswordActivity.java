package com.example.expensetracker.auth;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.example.expensetracker.R;
import com.google.android.material.textfield.TextInputEditText;

public class ForgotPasswordActivity extends AppCompatActivity {
    private TextInputEditText emailInput;
    private Button resetButton;
    private ProgressBar progressBar;
    private AuthManager authManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        initializeViews();
        setupClickListeners();
        authManager = AuthManager.getInstance();
    }

    private void initializeViews() {
        emailInput = findViewById(R.id.emailInput);
        resetButton = findViewById(R.id.resetButton);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupClickListeners() {
        resetButton.setOnClickListener(v -> attemptPasswordReset());
    }

    private void attemptPasswordReset() {
        String email = emailInput.getText().toString().trim();

        if (validateInput(email)) {
            showLoading(true);
            authManager.resetPassword(email, new AuthCallback() {
                @Override
                public void onSuccess() {
                    showLoading(false);
                    Toast.makeText(ForgotPasswordActivity.this, 
                        "Password reset email sent", Toast.LENGTH_LONG).show();
                    finish();
                }

                @Override
                public void onError(Exception e) {
                    showLoading(false);
                    Toast.makeText(ForgotPasswordActivity.this, 
                        "Failed to send reset email: " + e.getMessage(),
                        Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private boolean validateInput(String email) {
        if (email.isEmpty()) {
            emailInput.setError("Email is required");
            return false;
        }
        return true;
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        resetButton.setEnabled(!show);
    }
} 