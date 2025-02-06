package com.example.expensetracker.expense;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.expensetracker.R;
import com.example.expensetracker.category.CategoryManagementActivity;
import com.example.expensetracker.data.FirestoreManager;
import com.example.expensetracker.models.Category;
import com.example.expensetracker.models.Expense;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import com.google.android.gms.tasks.Task;

public class AddExpenseActivity extends AppCompatActivity {
    private TextInputEditText amountInput;
    private Spinner categorySpinner;
    private Button datePickerButton;
    private TextInputEditText descriptionInput;
    private Button saveButton;
    private Date selectedDate;
    private FirestoreManager firestoreManager;
    private List<Category> categories;
    private ArrayAdapter<String> categoryAdapter;
    private String expenseId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_expense);
        
        firestoreManager = FirestoreManager.getInstance();
        categories = new ArrayList<>();
        
        // Initialize views first
        initializeViews();
        setupCategorySpinner();
        setupDatePicker();
        setupSaveButton();
        
        // Then check for edit mode and populate fields
        expenseId = getIntent().getStringExtra("EXPENSE_ID");
        if (expenseId != null) {
            setTitle("Edit Expense");
            populateFields();
        }
        
        loadCategories();
    }

    private void initializeViews() {
        amountInput = findViewById(R.id.amountInput);
        categorySpinner = findViewById(R.id.categorySpinner);
        datePickerButton = findViewById(R.id.datePickerButton);
        descriptionInput = findViewById(R.id.descriptionInput);
        saveButton = findViewById(R.id.saveButton);
        selectedDate = new Date();
    }

    private void populateFields() {
        double amount = getIntent().getDoubleExtra("EXPENSE_AMOUNT", 0);
        String category = getIntent().getStringExtra("EXPENSE_CATEGORY");
        long dateMillis = getIntent().getLongExtra("EXPENSE_DATE", System.currentTimeMillis());
        String description = getIntent().getStringExtra("EXPENSE_DESCRIPTION");

        amountInput.setText(String.valueOf(amount));
        selectedDate = new Date(dateMillis);
        datePickerButton.setText(android.text.format.DateFormat.getDateFormat(this).format(selectedDate));
        descriptionInput.setText(description);
        
        loadCategories(() -> {
            int categoryPosition = getCategoryPosition(category);
            if (categoryPosition != -1) {
                categorySpinner.setSelection(categoryPosition);
            }
        });
    }

    private void setupCategorySpinner() {
        categoryAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item,
                new ArrayList<>());
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(categoryAdapter);
    }

    private void loadCategories() {
        loadCategories(() -> {});
    }

    private void loadCategories(Runnable onCategoriesLoaded) {
        firestoreManager.getUserCategories()
                .addSnapshotListener((value, error) -> {
                    if (error != null && error.getMessage() != null && !error.getMessage().isEmpty()) {
                        return;
                    }

                    categories.clear();
                    List<String> categoryNames = new ArrayList<>();
                    
                    if (value != null) {
                        for (QueryDocumentSnapshot doc : value) {
                            Category category = Category.fromMap(doc.getId(), doc.getData());
                            categories.add(category);
                            categoryNames.add(category.getName());
                        }
                        categoryAdapter.clear();
                        categoryAdapter.addAll(categoryNames);
                        onCategoriesLoaded.run();
                    }
                });
    }

    private int getCategoryPosition(String categoryName) {
        for (int i = 0; i < categoryAdapter.getCount(); i++) {
            if (categoryAdapter.getItem(i).equals(categoryName)) {
                return i;
            }
        }
        return -1;
    }

    private String getCategoryIdByName(String categoryName) {
        for (Category category : categories) {
            if (category.getName().equals(categoryName)) {
                return category.getId();
            }
        }
        return null;
    }

    private void setupDatePicker() {
        MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Select date")
                .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                .build();

        datePickerButton.setOnClickListener(v -> datePicker.show(getSupportFragmentManager(), "DATE_PICKER"));

        datePicker.addOnPositiveButtonClickListener(selection -> {
            selectedDate = new Date(selection);
            datePickerButton.setText(android.text.format.DateFormat.getDateFormat(this).format(selectedDate));
        });
    }

    private void setupSaveButton() {
        saveButton.setOnClickListener(v -> saveExpense());
    }

    private void saveExpense() {
        String amountStr = amountInput.getText().toString();
        String selectedCategory = categorySpinner.getSelectedItem().toString();
        String description = descriptionInput.getText().toString();

        if (!validateInput(amountStr, selectedCategory)) {
            return;
        }

        String categoryId = getCategoryIdByName(selectedCategory);
        if (categoryId == null) {
            Toast.makeText(this, "Invalid category selected", Toast.LENGTH_SHORT).show();
            return;
        }

        double amount = Double.parseDouble(amountStr);
        Expense expense = new Expense(
                firestoreManager.getCurrentUserId(),
                amount,
                categoryId,
                selectedDate != null ? new Timestamp(selectedDate) : new Timestamp(new Date()),
                description
        );

        Task<Void> task = expenseId != null ?
                firestoreManager.updateExpense(expenseId, expense) :
                firestoreManager.addExpense(expense);

        task.addOnSuccessListener(aVoid -> {
            Toast.makeText(this, "Expense saved", Toast.LENGTH_SHORT).show();
            finish();
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Error saving expense: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private boolean validateInput(String amount, String category) {
        if (amount.isEmpty()) {
            amountInput.setError("Amount is required");
            return false;
        }
        try {
            Double.parseDouble(amount);
        } catch (NumberFormatException e) {
            amountInput.setError("Invalid amount");
            return false;
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_add_expense, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_manage_categories) {
            startActivity(new Intent(this, CategoryManagementActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
} 