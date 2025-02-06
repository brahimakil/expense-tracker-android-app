package com.example.expensetracker;

import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.ItemTouchHelper;
import com.example.expensetracker.auth.AuthManager;
import com.example.expensetracker.auth.LoginActivity;
import com.example.expensetracker.data.FirestoreManager;
import com.example.expensetracker.expense.AddExpenseActivity;
import com.example.expensetracker.models.Expense;
import com.example.expensetracker.profile.ProfileActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.example.expensetracker.category.CategoryChipAdapter;
import com.example.expensetracker.models.Category;
import com.example.expensetracker.category.CategoriesActivity;
import com.example.expensetracker.expense.TransactionAdapter;
import com.example.expensetracker.models.*;
import com.example.expensetracker.models.User;
import com.example.expensetracker.models.User;
import com.example.expensetracker.analytics.AnalyticsActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {
    private AuthManager authManager;
    private FirestoreManager firestoreManager;
    private TextView totalExpensesText;
    private RecyclerView recentTransactionsRecyclerView;
    private FloatingActionButton addExpenseFab;
    private NavigationBarView bottomNavigation;
    private RecyclerView categoriesRecyclerView;
    private CategoryChipAdapter categoryAdapter;
    private List<Category> categories;
    private TransactionAdapter transactionAdapter;
    private TextView budgetStatusText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        
        authManager = AuthManager.getInstance();
        firestoreManager = FirestoreManager.getInstance();
        
        // Check if user is signed in
        if (!authManager.isUserSignedIn()) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        initializeViews();
        setupBottomNavigation();
        setupAddExpenseFab();
        loadDashboardData();
        loadCategories();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadDashboardData();
        loadCategories();
    }

    private void initializeViews() {
        totalExpensesText = findViewById(R.id.totalExpensesText);
        budgetStatusText = findViewById(R.id.budgetStatusText);
        recentTransactionsRecyclerView = findViewById(R.id.recentTransactionsRecyclerView);
        addExpenseFab = findViewById(R.id.addExpenseFab);
        bottomNavigation = findViewById(R.id.bottomNavigation);
        categoriesRecyclerView = findViewById(R.id.categoriesRecyclerView);
        categories = new ArrayList<>();
        
        // Initialize transaction adapter
        transactionAdapter = new TransactionAdapter(new ArrayList<>(), this, this);
        recentTransactionsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        recentTransactionsRecyclerView.setAdapter(transactionAdapter);
        
        // Set up horizontal layout for categories
        LinearLayoutManager layoutManager = new LinearLayoutManager(
                this, LinearLayoutManager.HORIZONTAL, false);
        categoriesRecyclerView.setLayoutManager(layoutManager);
        
        categoryAdapter = new CategoryChipAdapter(categories, category -> {
            // Filter expenses by category
            // TODO: Implement filtering
        });
        categoriesRecyclerView.setAdapter(categoryAdapter);
    }

    private void setupBottomNavigation() {
        bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.navigation_dashboard) {
                return true;
            } else if (itemId == R.id.navigation_categories) {
                startActivity(new Intent(this, CategoriesActivity.class));
                return true;
            } else if (itemId == R.id.navigation_analytics) {
                startActivity(new Intent(this, AnalyticsActivity.class));
                return true;
            } else if (itemId == R.id.navigation_profile) {
                startActivity(new Intent(this, ProfileActivity.class));
                return true;
            }
            return false;
        });
    }

    private void setupAddExpenseFab() {
        addExpenseFab.setOnClickListener(v -> 
            startActivity(new Intent(this, AddExpenseActivity.class)));
    }

    private void loadDashboardData() {
        firestoreManager.getUserExpenses()
            .addSnapshotListener((value, error) -> {
                if (error != null) {
//                    Toast.makeText(this, "Error loading expenses", Toast.LENGTH_SHORT).show();
                    return;
                }

                final double[] totalExpenses = {0.0};
                List<Expense> expensesList = new ArrayList<>();
                
                if (value != null) {
                    for (QueryDocumentSnapshot doc : value) {
                        Expense expense = Expense.fromMap(doc.getId(), doc.getData());
                        expensesList.add(expense);
                        totalExpenses[0] += expense.getAmount();
                    }
                }
                
                // Update transactions list
                transactionAdapter.updateTransactions(expensesList);
                totalExpensesText.setText(String.format("$%.2f", totalExpenses[0]));
                
                // Load and compare with budget
                firestoreManager.getUserBudget()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot != null && documentSnapshot.exists()) {
                            User user = User.fromMap(documentSnapshot.getId(), documentSnapshot.getData());
                            Double budget = user.getBudget();
                            
                            if (budget != null) {
                                double remaining = budget - totalExpenses[0];
                                String statusText;
                                int textColor;
                                
                                if (remaining > (budget * 0.5)) {  // More than 50% remaining
                                    statusText = String.format("Healthy Budget: $%.2f remaining", remaining);
                                    textColor = getColor(R.color.green);
                                } else if (remaining > (budget * 0.2)) {  // Between 20-50% remaining
                                    statusText = String.format("Watch Spending: $%.2f remaining", remaining);
                                    textColor = getColor(R.color.yellow);  // Add yellow color to colors.xml
                                } else if (remaining > 0) {  // Less than 20% remaining
                                    statusText = String.format("Budget Alert: Only $%.2f remaining", remaining);
                                    textColor = getColor(R.color.orange);  // Add orange color to colors.xml
                                } else {  // Over budget
                                    statusText = String.format("Over Budget: $%.2f exceeded", Math.abs(remaining));
                                    textColor = getColor(R.color.red);
                                }
                                
                                budgetStatusText.setText(statusText);
                                budgetStatusText.setTextColor(textColor);
                            } else {
                                budgetStatusText.setText("No budget set");
                                budgetStatusText.setTextColor(getColor(android.R.color.darker_gray));
                            }
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Error loading budget: " + e.getMessage(), 
                            Toast.LENGTH_SHORT).show();
                    });
            });
    }

    private void loadCategories() {
        firestoreManager.getUserCategories()
            .addSnapshotListener((value, error) -> {
                if (error != null && error.getMessage() != null && !error.getMessage().isEmpty()) {
                    return;
                }

                if (value != null) {
                    categories.clear();
                    for (QueryDocumentSnapshot doc : value) {
                        categories.add(Category.fromMap(doc.getId(), doc.getData()));
                    }
                    categoryAdapter.updateCategories(categories);
                }
            });
    }

    public void showDeleteConfirmationDialog(Expense expense) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Transaction")
                .setMessage("Are you sure you want to delete this transaction?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    firestoreManager.deleteExpense(expense.getId())
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(this, "Transaction deleted", Toast.LENGTH_SHORT).show();
                                loadDashboardData();
                            })
                            .addOnFailureListener(e -> Toast.makeText(this,
                                    "Error deleting transaction", Toast.LENGTH_SHORT).show());
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    public void showEditExpenseDialog(Expense expense) {
        Intent intent = new Intent(this, AddExpenseActivity.class);
        intent.putExtra("EXPENSE_ID", expense.getId());
        intent.putExtra("EXPENSE_AMOUNT", expense.getAmount());
        intent.putExtra("EXPENSE_CATEGORY", getCategoryName(expense.getCategoryId()));
        intent.putExtra("EXPENSE_DATE", expense.getDate().toDate().getTime());
        intent.putExtra("EXPENSE_DESCRIPTION", expense.getDescription());
        startActivity(intent);
    }

    private String getCategoryName(String categoryId) {
        for (Category category : categories) {
            if (category.getId().equals(categoryId)) {
                return category.getName();
            }
        }
        return "Unknown";
    }
}