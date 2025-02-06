package com.example.expensetracker.utils;

import com.github.mikephil.charting.formatter.ValueFormatter;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DateAxisValueFormatter extends ValueFormatter {
    private final SimpleDateFormat sdf;

    public DateAxisValueFormatter() {
        sdf = new SimpleDateFormat("MM/dd", Locale.getDefault());
    }

    @Override
    public String getFormattedValue(float value) {
        return sdf.format(new Date((long) value));
    }
} 