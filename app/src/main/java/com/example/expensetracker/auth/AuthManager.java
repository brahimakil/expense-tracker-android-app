package com.example.expensetracker.auth;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.example.expensetracker.data.FirestoreManager;

public class AuthManager {
    private static AuthManager instance;
    private final FirebaseAuth firebaseAuth;
    private final FirestoreManager firestoreManager;

    private AuthManager() {
        firebaseAuth = FirebaseAuth.getInstance();
        firestoreManager = FirestoreManager.getInstance();
    }

    public static synchronized AuthManager getInstance() {
        if (instance == null) {
            instance = new AuthManager();
        }
        return instance;
    }

    public void signIn(String email, String password, AuthCallback callback) {
        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> callback.onSuccess())
                .addOnFailureListener(callback::onError);
    }

    public void signUp(String email, String password, AuthCallback callback) {
        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> callback.onSuccess())
                .addOnFailureListener(callback::onError);
    }

    public void signOut() {
        firebaseAuth.signOut();
    }

    public FirebaseUser getCurrentUser() {
        return firebaseAuth.getCurrentUser();
    }

    public boolean isUserSignedIn() {
        return getCurrentUser() != null;
    }

    public void resetPassword(String email, AuthCallback callback) {
        firebaseAuth.sendPasswordResetEmail(email)
                .addOnSuccessListener(unused -> callback.onSuccess())
                .addOnFailureListener(callback::onError);
    }

    public Task<Void> initializeUser() {
        return firestoreManager.initializeUser();
    }
} 