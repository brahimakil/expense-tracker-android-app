package com.example.expensetracker.profile;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.expensetracker.R;
import com.example.expensetracker.auth.AuthManager;
import com.example.expensetracker.auth.LoginActivity;
import com.example.expensetracker.category.CategoriesActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import android.widget.Button;
import android.widget.Toast;
import com.example.expensetracker.data.FirestoreManager;
import android.app.ProgressDialog;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import androidx.core.content.FileProvider;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import com.google.firebase.Timestamp;
import com.example.expensetracker.models.Expense;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.Map;
import java.util.HashMap;
import com.example.expensetracker.models.Category;

public class ProfileActivity extends AppCompatActivity {
    private AuthManager authManager;
    private TextView userEmailText;
    private MaterialButton logoutButton;
    private MaterialButton manageCategoriesButton;
    private TextInputEditText budgetInput;
    private Button saveBudgetButton;
    private FirestoreManager firestoreManager;
    private Button generateReportButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        
        authManager = AuthManager.getInstance();
        firestoreManager = FirestoreManager.getInstance();
        
        initializeViews();
        setupButtons();
        displayUserInfo();
        loadCurrentBudget();
        setupSaveBudgetButton();
        setupReportGeneration();
    }

    private void initializeViews() {
        userEmailText = findViewById(R.id.userEmailText);
        logoutButton = findViewById(R.id.logoutButton);
        manageCategoriesButton = findViewById(R.id.manageCategoriesButton);
        budgetInput = findViewById(R.id.budgetInput);
        saveBudgetButton = findViewById(R.id.saveBudgetButton);
        generateReportButton = findViewById(R.id.generateReportButton);
    }

    private void setupButtons() {
        logoutButton.setOnClickListener(v -> {
            authManager.signOut();
            startActivity(new Intent(this, LoginActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
            finish();
        });

        manageCategoriesButton.setOnClickListener(v -> 
            startActivity(new Intent(this, CategoriesActivity.class)));
    }

    private void displayUserInfo() {
        String userEmail = authManager.getCurrentUser().getEmail();
        userEmailText.setText(userEmail);
    }

    private void loadCurrentBudget() {
        firestoreManager.getUserBudget().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists() && documentSnapshot.contains("budget")) {
                Double budget = documentSnapshot.getDouble("budget");
                if (budget != null) {
                    budgetInput.setText(String.format("%.2f", budget));
                }
            }
        });
    }

    private void setupSaveBudgetButton() {
        saveBudgetButton.setOnClickListener(v -> {
            String budgetStr = budgetInput.getText().toString().trim();
            if (!budgetStr.isEmpty()) {
                try {
                    double budget = Double.parseDouble(budgetStr);
                    firestoreManager.updateUserBudget(budget)
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(this, "Budget updated successfully", Toast.LENGTH_SHORT).show();
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(this, "Error updating budget", Toast.LENGTH_SHORT).show();
                        });
                } catch (NumberFormatException e) {
                    budgetInput.setError("Invalid number format");
                }
            }
        });
    }

    private void setupReportGeneration() {
        generateReportButton.setOnClickListener(v -> generatePDFReport());
    }

    private void generatePDFReport() {
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Generating expense report...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        // First, load all categories to have a mapping
        firestoreManager.getUserCategories().get().addOnSuccessListener(categorySnapshot -> {
            Map<String, String> categoryMap = new HashMap<>();
            for (QueryDocumentSnapshot doc : categorySnapshot) {
                Category category = Category.fromMap(doc.getId(), doc.getData());
                categoryMap.put(category.getId(), category.getName());
            }

            // Create PDF document and canvas outside the nested callbacks
            PdfDocument document = new PdfDocument();
            PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(595, 842, 1).create();
            PdfDocument.Page page = document.startPage(pageInfo);
            Canvas canvas = page.getCanvas();
            Paint paint = new Paint();

            // Header
            paint.setTextSize(20);
            paint.setTextAlign(Paint.Align.CENTER);
            canvas.drawText("Expense Report", pageInfo.getPageWidth()/2, 50, paint);

            // Date
            paint.setTextSize(12);
            paint.setTextAlign(Paint.Align.RIGHT);
            String date = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(new Date());
            canvas.drawText("Generated: " + date, pageInfo.getPageWidth() - 50, 80, paint);

            // User Info
            paint.setTextAlign(Paint.Align.LEFT);
            paint.setTextSize(14);
            canvas.drawText("User: " + authManager.getCurrentUser().getEmail(), 50, 120, paint);

            // Create final references for use in lambda
            final PdfDocument finalDocument = document;
            final Canvas finalCanvas = canvas;
            final Paint finalPaint = paint;
            final PdfDocument.Page finalPage = page;

            // Then load expenses and generate PDF
            firestoreManager.getUserExpenses().get().addOnSuccessListener(querySnapshot -> {
                try {
                    float y = 200;
                    double total = 0;

                    // Draw expenses
                    finalPaint.setTextSize(16);
                    finalCanvas.drawText("Expense Transactions:", 50, y, finalPaint);
                    y += 40;

                    finalPaint.setTextSize(12);
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        Expense expense = Expense.fromMap(doc.getId(), doc.getData());
                        String expenseDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                            .format(expense.getDate().toDate());
                        String categoryName = categoryMap.getOrDefault(expense.getCategoryId(), "Uncategorized");
                        String line = String.format("%s | $%.2f | %s | %s", 
                            expenseDate, expense.getAmount(), categoryName, expense.getDescription());
                        finalCanvas.drawText(line, 50, y, finalPaint);
                        y += 20;
                        total += expense.getAmount();
                    }

                    // Total
                    finalPaint.setTextSize(14);
                    finalPaint.setFakeBoldText(true);
                    finalCanvas.drawText("Total Expenses: $" + String.format("%.2f", total), 50, y + 40, finalPaint);

                    finalDocument.finishPage(finalPage);

                    // Save and share PDF
                    String fileName = "expense_report_" + System.currentTimeMillis() + ".pdf";
                    File pdfFile = new File(getExternalFilesDir(null), fileName);
                    FileOutputStream fos = new FileOutputStream(pdfFile);
                    finalDocument.writeTo(fos);
                    finalDocument.close();
                    fos.close();

                    progressDialog.dismiss();

                    // Share PDF
                    Uri pdfUri = FileProvider.getUriForFile(this,
                        "com.example.expensetracker.fileprovider", pdfFile);
                    Intent intent = new Intent(Intent.ACTION_SEND);
                    intent.setType("application/pdf");
                    intent.putExtra(Intent.EXTRA_STREAM, pdfUri);
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    startActivity(Intent.createChooser(intent, "Share Expense Report"));

                } catch (IOException e) {
                    progressDialog.dismiss();
                    Toast.makeText(this, "Error generating report: " + e.getMessage(), 
                        Toast.LENGTH_SHORT).show();
                }
            }).addOnFailureListener(e -> {
                progressDialog.dismiss();
                Toast.makeText(this, "Error loading expenses: " + e.getMessage(), 
                    Toast.LENGTH_SHORT).show();
            });
        }).addOnFailureListener(e -> {
            progressDialog.dismiss();
            Toast.makeText(this, "Error loading categories: " + e.getMessage(), 
                Toast.LENGTH_SHORT).show();
        });
    }
} 