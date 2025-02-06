package com.example.expensetracker.models;

import com.google.firebase.firestore.DocumentSnapshot;
import java.util.HashMap;
import java.util.Map;

public class User {
    private String id;
    private Double budget;

    public User() {
        // Required empty constructor for Firestore
    }

    public User(String id) {
        this.id = id;
        this.budget = null;
    }

    public static User fromMap(String id, Map<String, Object> map) {
        User user = new User();
        user.id = id;
        if (map != null) {
            Object budgetObj = map.get("budget");
            if (budgetObj != null) {
                if (budgetObj instanceof Double) {
                    user.budget = (Double) budgetObj;
                } else if (budgetObj instanceof Long) {
                    user.budget = ((Long) budgetObj).doubleValue();
                }
            }
        }
        return user;
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("budget", budget);
        return map;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public Double getBudget() { return budget; }
    public void setBudget(Double budget) { this.budget = budget; }
} 