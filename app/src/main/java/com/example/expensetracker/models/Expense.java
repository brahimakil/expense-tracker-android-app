package com.example.expensetracker.models;

import com.google.firebase.Timestamp;
import java.util.HashMap;
import java.util.Map;

public class Expense {
    private String id;
    private String userId;
    private double amount;
    private String categoryId;
    private Timestamp date;
    private String description;

    public Expense() {
        // Required empty constructor for Firestore
    }

    public Expense(String userId, double amount, String categoryId, Timestamp date, String description) {
        this.userId = userId;
        this.amount = amount;
        this.categoryId = categoryId;
        this.date = date;
        this.description = description;
    }

    // Convert Firestore document to Expense object
    public static Expense fromMap(String id, Map<String, Object> map) {
        Expense expense = new Expense();
        expense.id = id;
        expense.userId = (String) map.get("userId");
        expense.amount = (double) map.get("amount");
        expense.categoryId = (String) map.get("categoryId");
        expense.date = (Timestamp) map.get("date");
        expense.description = (String) map.get("description");
        return expense;
    }

    // Convert Expense object to Firestore document
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("userId", userId);
        map.put("amount", amount);
        map.put("categoryId", categoryId);
        map.put("date", date);
        map.put("description", description);
        return map;
    }

    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }
    public String getCategoryId() { return categoryId; }
    public void setCategoryId(String categoryId) { this.categoryId = categoryId; }
    public Timestamp getDate() { return date; }
    public void setDate(Timestamp date) { this.date = date; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
} 