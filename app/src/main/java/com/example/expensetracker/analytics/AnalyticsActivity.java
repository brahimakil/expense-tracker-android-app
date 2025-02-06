package com.example.expensetracker.analytics;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.expensetracker.R;
import com.example.expensetracker.data.FirestoreManager;
import com.example.expensetracker.models.Category;
import com.example.expensetracker.models.Expense;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.example.expensetracker.utils.DateAxisValueFormatter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AnalyticsActivity extends AppCompatActivity {
    private FirestoreManager firestoreManager;
    private PieChart categoryPieChart;
    private LineChart spendingTrendChart;
    private Map<String, Category> categoryMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analytics);
        
        firestoreManager = FirestoreManager.getInstance();
        categoryMap = new HashMap<>();
        
        initializeViews();
        setupPieChart();
        setupTrendChart();
        loadCategories();
    }

    private void initializeViews() {
        categoryPieChart = findViewById(R.id.categoryPieChart);
        spendingTrendChart = findViewById(R.id.spendingTrendChart);
    }

    private void setupPieChart() {
        // Get theme-dependent text color
        int textColor = getResources().getColor(
            isLightTheme() ? android.R.color.black : android.R.color.white
        );

        categoryPieChart.setUsePercentValues(true);
        categoryPieChart.getDescription().setEnabled(false);
        categoryPieChart.setExtraOffsets(5, 10, 5, 5);
        categoryPieChart.setDragDecelerationFrictionCoef(0.95f);
        categoryPieChart.setDrawHoleEnabled(true);
        categoryPieChart.setHoleColor(getResources().getColor(android.R.color.transparent));
        categoryPieChart.setTransparentCircleRadius(61f);
        categoryPieChart.setDrawCenterText(true);
        categoryPieChart.setCenterText("Expenses by Category");
        categoryPieChart.setCenterTextSize(16f);
        categoryPieChart.setCenterTextColor(textColor);
        
        Legend legend = categoryPieChart.getLegend();
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.CENTER);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
        legend.setOrientation(Legend.LegendOrientation.VERTICAL);
        legend.setDrawInside(false);
        legend.setEnabled(true);
        legend.setTextColor(textColor);
        
        categoryPieChart.setEntryLabelColor(textColor);
        categoryPieChart.setEntryLabelTextSize(12f);
        categoryPieChart.setHoleColor(getResources().getColor(android.R.color.transparent));
        categoryPieChart.setTransparentCircleColor(getResources().getColor(android.R.color.transparent));
    }

    private void setupTrendChart() {
        int textColor = getResources().getColor(
            isLightTheme() ? android.R.color.black : android.R.color.white
        );

        spendingTrendChart.getDescription().setEnabled(false);
        spendingTrendChart.setDrawGridBackground(false);
        spendingTrendChart.setBackgroundColor(getResources().getColor(android.R.color.transparent));
        
        // Enable touch gestures
        spendingTrendChart.setTouchEnabled(true);
        spendingTrendChart.setDragEnabled(true);
        spendingTrendChart.setScaleEnabled(true);
        
        // Customize X axis
        XAxis xAxis = spendingTrendChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setValueFormatter(new DateAxisValueFormatter());
        xAxis.setTextColor(textColor);
        
        // Customize Y axis
        YAxis leftAxis = spendingTrendChart.getAxisLeft();
        leftAxis.setDrawGridLines(true);
        leftAxis.setAxisMinimum(0f);
        leftAxis.setTextColor(textColor);
        leftAxis.setGridColor(Color.GRAY);
        
        // Disable right axis
        spendingTrendChart.getAxisRight().setEnabled(false);
        
        // Enable legend
        Legend legend = spendingTrendChart.getLegend();
        legend.setForm(Legend.LegendForm.LINE);
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        legend.setDrawInside(false);
        legend.setTextColor(textColor);
    }

    private void loadCategories() {
        try {
            firestoreManager.getUserCategories()
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e("AnalyticsActivity", "Error loading categories", error);
                        Toast.makeText(this, "Error loading categories: " + error.getMessage(), Toast.LENGTH_LONG).show();
                        return;
                    }

                    if (value == null || value.isEmpty()) {
                        Log.d("AnalyticsActivity", "No categories found");
                        Toast.makeText(this, "No categories found", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    categoryMap.clear();
                    List<Category> sortedCategories = new ArrayList<>();
                    
                    for (QueryDocumentSnapshot doc : value) {
                        try {
                            Category category = Category.fromMap(doc.getId(), doc.getData());
                            sortedCategories.add(category);
                        } catch (Exception e) {
                            Log.e("AnalyticsActivity", "Error parsing category", e);
                        }
                    }
                    
                    // Sort categories by name
                    Collections.sort(sortedCategories, (c1, c2) -> c1.getName().compareTo(c2.getName()));
                    
                    // Add to map after sorting
                    for (Category category : sortedCategories) {
                        categoryMap.put(category.getId(), category);
                    }
                    
                    loadExpenseData();
                });
        } catch (Exception e) {
            Log.e("AnalyticsActivity", "Failed to load categories", e);
            Toast.makeText(this, "Failed to load categories: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void loadExpenseData() {
        Map<String, Float> categoryTotals = new HashMap<>();
        List<Expense> expensesList = new ArrayList<>();
        
        firestoreManager.getUserExpenses()
            .addSnapshotListener((value, error) -> {
                if (error != null) {
                    Log.e("AnalyticsActivity", "Error loading expenses", error);
                    Toast.makeText(this, "Error loading expenses: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    categoryPieChart.setCenterText("Error loading data");
                    return;
                }

                float totalExpenses = 0f;
                if (value != null && !value.isEmpty()) {
                    for (QueryDocumentSnapshot doc : value) {
                        try {
                            Expense expense = Expense.fromMap(doc.getId(), doc.getData());
                            expensesList.add(expense); // Add to list for line chart
                            String categoryId = expense.getCategoryId();
                            if (categoryId != null && categoryMap.containsKey(categoryId)) {
                                float amount = (float) expense.getAmount();
                                categoryTotals.merge(categoryId, amount, Float::sum);
                                totalExpenses += amount;
                            }
                        } catch (Exception e) {
                            Log.e("AnalyticsActivity", "Error parsing expense", e);
                        }
                    }
                    updatePieChartData(categoryTotals, totalExpenses);
                    updateTrendChart(expensesList); // Update line chart
                } else {
                    categoryPieChart.setCenterText("No expenses yet");
                    spendingTrendChart.setNoDataText("No expenses yet");
                    categoryPieChart.invalidate();
                    spendingTrendChart.invalidate();
                }
            });
    }

    private void updatePieChartData(Map<String, Float> categoryTotals, float totalExpenses) {
        int textColor = getResources().getColor(
            isLightTheme() ? android.R.color.black : android.R.color.white
        );
        
        List<PieEntry> entries = new ArrayList<>();
        
        for (Map.Entry<String, Float> entry : categoryTotals.entrySet()) {
            Category category = categoryMap.get(entry.getKey());
            if (category != null) {
                float percentage = (entry.getValue() / totalExpenses) * 100;
                entries.add(new PieEntry(percentage, category.getName()));
            }
        }

        if (entries.isEmpty()) {
            categoryPieChart.setCenterText("No data to display");
            categoryPieChart.invalidate();
            return;
        }

        PieDataSet dataSet = new PieDataSet(entries, "Expenses by Category");
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        dataSet.setValueTextSize(12f);
        dataSet.setValueTextColor(textColor);
        dataSet.setValueFormatter(new PercentFormatter(categoryPieChart));
        
        // Update these lines for better visibility
        dataSet.setValueTextColor(textColor);
        dataSet.setValueTextSize(14f);
        dataSet.setValueLineColor(textColor);
        
        PieData data = new PieData(dataSet);
        data.setValueTextColor(textColor);
        data.setValueTextSize(14f);

        categoryPieChart.setData(data);
        categoryPieChart.invalidate();
    }

    private void updateTrendChart(List<Expense> expenses) {
        if (expenses.isEmpty()) {
            spendingTrendChart.setNoDataText("No expenses yet");
            spendingTrendChart.invalidate();
            return;
        }

        // Sort expenses by date
        Collections.sort(expenses, (e1, e2) -> e1.getDate().compareTo(e2.getDate()));
        
        ArrayList<Entry> entries = new ArrayList<>();
        for (int i = 0; i < expenses.size(); i++) {
            Expense expense = expenses.get(i);
            entries.add(new Entry(expense.getDate().toDate().getTime(), (float) expense.getAmount()));
        }

        LineDataSet dataSet = new LineDataSet(entries, "Daily Spending");
        dataSet.setColor(getResources().getColor(R.color.md_theme_light_primary));
        dataSet.setLineWidth(3f);
        dataSet.setCircleColor(getResources().getColor(R.color.md_theme_light_primary));
        dataSet.setCircleRadius(5f);
        dataSet.setDrawFilled(true);
        dataSet.setFillColor(getResources().getColor(R.color.md_theme_light_primaryContainer));
        dataSet.setFillAlpha(50);
        dataSet.setValueTextColor(getResources().getColor(
            isLightTheme() ? android.R.color.black : android.R.color.white
        ));
        dataSet.setValueTextSize(12f);

        LineData lineData = new LineData(dataSet);
        spendingTrendChart.setData(lineData);
        spendingTrendChart.invalidate();
    }

    // Add this helper method to check the current theme
    private boolean isLightTheme() {
        return (getResources().getConfiguration().uiMode & 
                android.content.res.Configuration.UI_MODE_NIGHT_MASK) 
                != android.content.res.Configuration.UI_MODE_NIGHT_YES;
    }
} 