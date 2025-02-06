package com.example.expensetracker.expense;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.expensetracker.MainActivity;
import com.example.expensetracker.R;
import com.example.expensetracker.data.FirestoreManager;
import com.example.expensetracker.models.Category;
import com.example.expensetracker.models.Expense;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import com.google.firebase.firestore.FirebaseFirestore;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.ViewHolder> {
    private List<Expense> expenses;
    private Context context;
    private SimpleDateFormat dateFormat;
    private FirebaseFirestore db;
    private final MainActivity activity;

    public TransactionAdapter(List<Expense> expenses, Context context, MainActivity activity) {
        this.expenses = expenses;
        this.context = context;
        this.dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        this.db = FirebaseFirestore.getInstance();
        this.activity = activity;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_transaction, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Expense expense = expenses.get(position);
        holder.amountText.setText(String.format("$%.2f", expense.getAmount()));
        holder.dateText.setText(dateFormat.format(expense.getDate().toDate()));
        holder.descriptionText.setText(expense.getDescription());
        
        // Set default category text
        holder.categoryText.setText("Uncategorized");
        
        // Look up category name from categoryId only if categoryId is not null
        String categoryId = expense.getCategoryId();
        if (categoryId != null && !categoryId.isEmpty()) {
            db.collection("categories")
                .document(categoryId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Category category = Category.fromMap(documentSnapshot.getId(), documentSnapshot.getData());
                        holder.categoryText.setText(category.getName());
                    }
                })
                .addOnFailureListener(e -> {
                    // Keep the default "Uncategorized" text on failure
                });
        }

        holder.editButton.setOnClickListener(v -> activity.showEditExpenseDialog(expense));
        holder.deleteButton.setOnClickListener(v -> activity.showDeleteConfirmationDialog(expense));
    }

    @Override
    public int getItemCount() {
        return expenses.size();
    }

    public void updateTransactions(List<Expense> newExpenses) {
        this.expenses = newExpenses;
        notifyDataSetChanged();
    }

    public Expense getExpense(int position) {
        return expenses.get(position);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView amountText, categoryText, dateText, descriptionText;
        ImageButton editButton, deleteButton;

        ViewHolder(View view) {
            super(view);
            amountText = view.findViewById(R.id.transactionAmount);
            categoryText = view.findViewById(R.id.transactionCategory);
            dateText = view.findViewById(R.id.transactionDate);
            descriptionText = view.findViewById(R.id.transactionDescription);
            editButton = view.findViewById(R.id.editButton);
            deleteButton = view.findViewById(R.id.deleteButton);
        }
    }
} 