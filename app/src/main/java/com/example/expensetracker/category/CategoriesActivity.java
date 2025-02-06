package com.example.expensetracker.category;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.expensetracker.R;
import com.example.expensetracker.data.FirestoreManager;
import com.example.expensetracker.models.Category;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class CategoriesActivity extends AppCompatActivity {
    private RecyclerView categoryRecyclerView;
    private FloatingActionButton addCategoryFab;
    private FirestoreManager firestoreManager;
    private List<Category> categories;
    private CategoryAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_categories);
        
        firestoreManager = FirestoreManager.getInstance();
        initializeViews();
        loadCategories();
    }

    private void initializeViews() {
        categoryRecyclerView = findViewById(R.id.categoryRecyclerView);
        addCategoryFab = findViewById(R.id.addCategoryFab);
        
        categoryRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        addCategoryFab.setOnClickListener(v -> showAddCategoryDialog());
        
        categories = new ArrayList<>();
        adapter = new CategoryAdapter(categories, new CategoryAdapter.OnCategoryClickListener() {
            @Override
            public void onCategoryClick(Category category) {
                showEditCategoryDialog(category);
            }

            @Override
            public void onDeleteClick(Category category) {
                if (!category.isDefault()) {
                    showDeleteConfirmationDialog(category);
                } else {
                    Toast.makeText(CategoriesActivity.this, 
                        "Cannot delete default category", Toast.LENGTH_SHORT).show();
                }
            }
        });
        categoryRecyclerView.setAdapter(adapter);
    }

    private void loadCategories() {
        firestoreManager.getUserCategories()
            .addSnapshotListener((value, error) -> {
                if (error != null && error.getMessage() != null && !error.getMessage().isEmpty()) {
                    return;
                }

                categories.clear();
                if (value != null) {
                    for (QueryDocumentSnapshot doc : value) {
                        categories.add(Category.fromMap(doc.getId(), doc.getData()));
                    }
                    adapter.updateCategories(categories);
                }
            });
    }

    private void showAddCategoryDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_add_category, null);
        EditText categoryNameInput = view.findViewById(R.id.categoryNameInput);

        builder.setView(view)
                .setTitle("Add New Category")
                .setPositiveButton("Add", (dialog, which) -> {
                    String categoryName = categoryNameInput.getText().toString().trim();
                    if (!categoryName.isEmpty()) {
                        Category category = new Category(
                                firestoreManager.getCurrentUserId(),
                                categoryName,
                                "default_icon",
                                false
                        );
                        firestoreManager.addCategory(category)
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(this, "Category added", Toast.LENGTH_SHORT).show();
                                    loadCategories(); // Reload immediately
                                })
                                .addOnFailureListener(e -> Toast.makeText(this,
                                        "Error adding category", Toast.LENGTH_SHORT).show());
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showEditCategoryDialog(Category category) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_add_category, null);
        EditText categoryNameInput = view.findViewById(R.id.categoryNameInput);
        categoryNameInput.setText(category.getName());

        builder.setView(view)
                .setTitle("Edit Category")
                .setPositiveButton("Save", (dialog, which) -> {
                    String newName = categoryNameInput.getText().toString().trim();
                    if (!newName.isEmpty()) {
                        category.setName(newName);
                        firestoreManager.updateCategory(category.getId(), category)
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(this, "Category updated", Toast.LENGTH_SHORT).show();
                                    loadCategories(); // Reload immediately
                                })
                                .addOnFailureListener(e -> Toast.makeText(this,
                                        "Error updating category", Toast.LENGTH_SHORT).show());
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showDeleteConfirmationDialog(Category category) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Category")
                .setMessage("Are you sure you want to delete this category?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    firestoreManager.deleteCategory(category.getId())
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(this, "Category deleted", Toast.LENGTH_SHORT).show();
                                loadCategories(); // Reload immediately
                            })
                            .addOnFailureListener(e -> Toast.makeText(this,
                                    "Error deleting category", Toast.LENGTH_SHORT).show());
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
} 