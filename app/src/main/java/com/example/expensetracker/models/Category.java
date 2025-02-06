package com.example.expensetracker.models;

import java.util.HashMap;
import java.util.Map;

public class Category {
    private String id;
    private String userId;
    private String name;
    private String icon;
    private boolean isDefault;

    public Category() {
        // Required empty constructor for Firestore
    }

    public Category(String userId, String name, String icon, boolean isDefault) {
        this.userId = userId;
        this.name = name;
        this.icon = icon;
        this.isDefault = isDefault;
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("userId", userId);
        map.put("name", name);
        map.put("icon", icon);
        map.put("isDefault", isDefault);
        return map;
    }

    public static Category fromMap(String id, Map<String, Object> data) {
        Category category = new Category(
            (String) data.get("userId"),
            (String) data.get("name"),
            (String) data.get("icon"),
            (Boolean) data.get("isDefault")
        );
        category.setId(id);
        return category;
    }

    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }
    public boolean isDefault() { return isDefault; }
    public void setDefault(boolean isDefault) { this.isDefault = isDefault; }
} 