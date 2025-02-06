package com.example.expensetracker.data;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.SetOptions;
import com.example.expensetracker.models.Expense;
import com.example.expensetracker.auth.AuthManager;
import com.example.expensetracker.models.Category;
import com.example.expensetracker.models.User;

import java.util.HashMap;
import java.util.Map;

public class FirestoreManager {
    private static FirestoreManager instance;
    private final FirebaseFirestore db;
    private final String COLLECTION_EXPENSES = "expenses";
    private final String COLLECTION_CATEGORIES = "categories";
    private final String COLLECTION_BUDGETS = "budgets";
    private static final String COLLECTION_USERS = "users";

    private FirestoreManager() {
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setCacheSizeBytes(FirebaseFirestoreSettings.CACHE_SIZE_UNLIMITED)
                .build();
        db = FirebaseFirestore.getInstance();
        db.setFirestoreSettings(settings);
    }

    public static synchronized FirestoreManager getInstance() {
        if (instance == null) {
            instance = new FirestoreManager();
        }
        return instance;
    }

    // Create new expense
    public Task<Void> addExpense(Expense expense) {
        return db.collection(COLLECTION_EXPENSES)
                .document()
                .set(expense.toMap());
    }

    // Read expenses for current user
    public Query getUserExpenses() {
        String userId = getCurrentUserId();
        if (userId == null || userId.isEmpty()) {
            throw new IllegalStateException("User ID is null or empty");
        }
        return db.collection(COLLECTION_EXPENSES)
                .whereEqualTo("userId", userId);
    }

    // Update expense
    public Task<Void> updateExpense(String expenseId, Expense expense) {
        return db.collection(COLLECTION_EXPENSES)
                .document(expenseId)
                .update(expense.toMap());
    }

    // Delete expense
    public Task<Void> deleteExpense(String expenseId) {
        return db.collection(COLLECTION_EXPENSES)
                .document(expenseId)
                .delete();
    }

    // Get expenses by category
    public Query getExpensesByCategory(String category) {
        String userId = AuthManager.getInstance().getCurrentUser().getUid();
        return db.collection(COLLECTION_EXPENSES)
                .whereEqualTo("userId", userId)
                .whereEqualTo("category", category)
                .orderBy("date", Query.Direction.DESCENDING);
    }

    // Get expenses by date range
    public Query getExpensesByDateRange(long startDate, long endDate) {
        String userId = AuthManager.getInstance().getCurrentUser().getUid();
        return db.collection(COLLECTION_EXPENSES)
                .whereEqualTo("userId", userId)
                .whereGreaterThanOrEqualTo("date", startDate)
                .whereLessThanOrEqualTo("date", endDate)
                .orderBy("date", Query.Direction.DESCENDING);
    }

    public String getCurrentUserId() {
        return AuthManager.getInstance().getCurrentUser().getUid();
    }

    // Create new category
    public Task<Void> addCategory(Category category) {
        return db.collection(COLLECTION_CATEGORIES)
                .document()
                .set(category.toMap());
    }

    // Get all categories for current user
    public Query getUserCategories() {
        String userId = getCurrentUserId();
        if (userId == null || userId.isEmpty()) {
            throw new IllegalStateException("User ID is null or empty");
        }
        return db.collection(COLLECTION_CATEGORIES)
                .whereEqualTo("userId", userId);
    }

    // Update category
    public Task<Void> updateCategory(String categoryId, Category category) {
        return db.collection(COLLECTION_CATEGORIES)
                .document(categoryId)
                .update(category.toMap());
    }

    // Delete category
    public Task<Void> deleteCategory(String categoryId) {
        return db.collection(COLLECTION_CATEGORIES)
                .document(categoryId)
                .delete();
    }

    // Initialize default categories for new user
    public void initializeDefaultCategories() {
        String userId = getCurrentUserId();
        String[] defaultCategories = {"Food", "Travel", "Shopping", "Bills", "Entertainment"};
        
        for (String categoryName : defaultCategories) {
            Category category = new Category(userId, categoryName, "default_icon", true);
            addCategory(category);
        }
    }

    public Task<DocumentSnapshot> getUserBudget() {
        return db.collection(COLLECTION_USERS)
                .document(getCurrentUserId())
                .get();
    }

    public Task<Void> updateUserBudget(Double budget) {
        Map<String, Object> userData = new HashMap<>();
        userData.put("budget", budget);
        
        return db.collection(COLLECTION_USERS)
                .document(getCurrentUserId())
                .set(userData, SetOptions.merge());
    }

    // Call this when user registers
    public Task<Void> initializeUser() {
        User user = new User(getCurrentUserId());
        return db.collection(COLLECTION_USERS)
                .document(getCurrentUserId())
                .set(user.toMap());
    }
} 