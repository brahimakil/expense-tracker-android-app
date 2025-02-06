package com.example.expensetracker.auth;

public interface AuthCallback {
    void onSuccess();
    void onError(Exception e);
} 