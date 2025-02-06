package com.example.expensetracker.category;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.expensetracker.R;
import com.example.expensetracker.models.Category;
import com.google.android.material.chip.Chip;
import java.util.List;

public class CategoryChipAdapter extends RecyclerView.Adapter<CategoryChipAdapter.CategoryChipViewHolder> {
    private List<Category> categories;
    private OnCategoryChipClickListener listener;

    public interface OnCategoryChipClickListener {
        void onCategoryChipClick(Category category);
    }

    public CategoryChipAdapter(List<Category> categories, OnCategoryChipClickListener listener) {
        this.categories = categories;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CategoryChipViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Chip chip = (Chip) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_category_chip, parent, false);
        return new CategoryChipViewHolder(chip);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryChipViewHolder holder, int position) {
        Category category = categories.get(position);
        holder.bind(category);
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    public void updateCategories(List<Category> newCategories) {
        this.categories = newCategories;
        notifyDataSetChanged();
    }

    class CategoryChipViewHolder extends RecyclerView.ViewHolder {
        private Chip chip;

        CategoryChipViewHolder(@NonNull Chip chip) {
            super(chip);
            this.chip = chip;
        }

        void bind(Category category) {
            chip.setText(category.getName());
            chip.setOnClickListener(v -> listener.onCategoryChipClick(category));
        }
    }
} 