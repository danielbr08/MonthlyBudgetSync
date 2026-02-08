package com.brosh.finance.monthlybudgetsync.adapters;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.brosh.finance.monthlybudgetsync.R;

import java.util.ArrayList;
import java.util.List;

/**
 * A paginated adapter for year selection in spinners.
 * Shows a window of years with "..." indicators to load more older or newer years.
 * 
 * Example display when viewing middle years:
 * - "▲ Newer..."
 * - 2024
 * - 2023
 * - 2022
 * - "▼ Older..."
 */
public class PaginatedYearAdapter extends ArrayAdapter<String> {
    
    // Pagination markers
    public static final String LOAD_NEWER = "▲ …";
    public static final String LOAD_OLDER = "▼ …";
    
    private static final int DEFAULT_PAGE_SIZE = 5;
    
    private final Context context;
    private final List<String> allYears;        // Full sorted list (newest first)
    private final List<String> displayItems;    // Currently visible items + markers
    private final int pageSize;
    
    private int windowStart = 0;  // Index in allYears where current window starts
    
    // Color for pagination markers
    private static final int MARKER_COLOR = 0xFF6B7280;  // Gray color
    
    /**
     * Creates a paginated year adapter.
     *
     * @param context  The context
     * @param allYears Full list of years, sorted descending (newest first)
     */
    public PaginatedYearAdapter(@NonNull Context context, @NonNull List<String> allYears) {
        this(context, allYears, DEFAULT_PAGE_SIZE);
    }
    
    /**
     * Creates a paginated year adapter with custom page size.
     *
     * @param context  The context
     * @param allYears Full list of years, sorted descending (newest first)
     * @param pageSize Number of years to show at once
     */
    public PaginatedYearAdapter(@NonNull Context context, @NonNull List<String> allYears, int pageSize) {
        super(context, R.layout.custom_spinner, new ArrayList<>());
        this.context = context;
        this.allYears = new ArrayList<>(allYears);
        this.pageSize = Math.max(3, pageSize);  // Minimum 3 years
        this.displayItems = new ArrayList<>();
        
        rebuildDisplayItems();
    }
    
    /**
     * Rebuilds the display items based on current window position.
     */
    private void rebuildDisplayItems() {
        displayItems.clear();
        
        if (allYears.isEmpty()) {
            notifyDataSetChanged();
            return;
        }
        
        // Add "Load Newer" marker if we're not at the beginning
        boolean hasNewer = windowStart > 0;
        if (hasNewer) {
            displayItems.add(LOAD_NEWER);
        }
        
        // Add visible years
        int windowEnd = Math.min(windowStart + pageSize, allYears.size());
        for (int i = windowStart; i < windowEnd; i++) {
            displayItems.add(allYears.get(i));
        }
        
        // Add "Load Older" marker if there are more years
        boolean hasOlder = windowEnd < allYears.size();
        if (hasOlder) {
            displayItems.add(LOAD_OLDER);
        }
        
        clear();
        addAll(displayItems);
        notifyDataSetChanged();
    }
    
    @Override
    public int getCount() {
        return displayItems.size();
    }
    
    @Nullable
    @Override
    public String getItem(int position) {
        return position < displayItems.size() ? displayItems.get(position) : null;
    }
    
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return createItemView(position, convertView, parent, R.layout.custom_spinner);
    }
    
    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return createItemView(position, convertView, parent, R.layout.spinner_dropdown_item);
    }
    
    private View createItemView(int position, @Nullable View convertView, @NonNull ViewGroup parent, int layoutRes) {
        View view = convertView;
        if (view == null) {
            LayoutInflater inflater = LayoutInflater.from(context);
            view = inflater.inflate(layoutRes, parent, false);
        }
        
        TextView textView = view.findViewById(android.R.id.text1);
        String item = getItem(position);
        
        if (textView != null && item != null) {
            textView.setText(item);
            
            // Style pagination markers differently
            if (isPaginationMarker(item)) {
                textView.setTextColor(MARKER_COLOR);
                textView.setTypeface(textView.getTypeface(), Typeface.ITALIC);
            } else {
                // Normal year item - restore default styling
                if (layoutRes == R.layout.custom_spinner) {
                    textView.setTextColor(Color.WHITE);
                } else {
                    textView.setTextColor(0xFF1B263B);  // Dark color for dropdown
                }
                textView.setTypeface(textView.getTypeface(), Typeface.NORMAL);
            }
        }
        
        return view;
    }
    
    /**
     * Checks if an item is a pagination marker.
     */
    public static boolean isPaginationMarker(@Nullable String item) {
        return LOAD_NEWER.equals(item) || LOAD_OLDER.equals(item);
    }
    
    /**
     * Checks if the item is the "load newer" marker.
     */
    public static boolean isLoadNewerMarker(@Nullable String item) {
        return LOAD_NEWER.equals(item);
    }
    
    /**
     * Checks if the item is the "load older" marker.
     */
    public static boolean isLoadOlderMarker(@Nullable String item) {
        return LOAD_OLDER.equals(item);
    }
    
    /**
     * Loads newer years (moves window towards the beginning).
     * 
     * @return The first real year in the new window, or null if already at start
     */
    @Nullable
    public String loadNewer() {
        if (windowStart <= 0) {
            return null;
        }
        
        // Move window back by pageSize, but keep some overlap
        windowStart = Math.max(0, windowStart - pageSize + 1);
        rebuildDisplayItems();
        
        // Return first real year (skip LOAD_NEWER marker if present)
        for (String item : displayItems) {
            if (!isPaginationMarker(item)) {
                return item;
            }
        }
        return null;
    }
    
    /**
     * Loads older years (moves window towards the end).
     * 
     * @return The last real year in the new window, or null if already at end
     */
    @Nullable
    public String loadOlder() {
        int maxStart = Math.max(0, allYears.size() - pageSize);
        if (windowStart >= maxStart) {
            return null;
        }
        
        // Move window forward by pageSize, but keep some overlap
        windowStart = Math.min(maxStart, windowStart + pageSize - 1);
        rebuildDisplayItems();
        
        // Return first real year in new window
        for (String item : displayItems) {
            if (!isPaginationMarker(item)) {
                return item;
            }
        }
        return null;
    }
    
    /**
     * Sets the window to show a specific year.
     * 
     * @param year The year to show in the window
     * @return true if the year was found and window adjusted
     */
    public boolean showYear(@NonNull String year) {
        int yearIndex = allYears.indexOf(year);
        if (yearIndex < 0) {
            return false;
        }
        
        // Center the window around the year if possible
        int newStart = Math.max(0, yearIndex - pageSize / 2);
        int maxStart = Math.max(0, allYears.size() - pageSize);
        windowStart = Math.min(newStart, maxStart);
        
        rebuildDisplayItems();
        return true;
    }
    
    /**
     * Gets the position of a year in the display list.
     * 
     * @param year The year to find
     * @return Position in display list, or -1 if not visible
     */
    public int getYearPosition(@NonNull String year) {
        return displayItems.indexOf(year);
    }
    
    /**
     * Checks if pagination is needed (more items than page size).
     */
    public boolean needsPagination() {
        return allYears.size() > pageSize;
    }
    
    /**
     * Gets the total number of years (not including markers).
     */
    public int getTotalYearCount() {
        return allYears.size();
    }
    
    /**
     * Gets all years.
     */
    @NonNull
    public List<String> getAllYears() {
        return new ArrayList<>(allYears);
    }
    
    /**
     * Gets the first (newest) real year in the current display.
     */
    @Nullable
    public String getFirstVisibleYear() {
        for (String item : displayItems) {
            if (!isPaginationMarker(item)) {
                return item;
            }
        }
        return null;
    }
    
    /**
     * Gets the last (oldest) real year in the current display.
     */
    @Nullable
    public String getLastVisibleYear() {
        for (int i = displayItems.size() - 1; i >= 0; i--) {
            String item = displayItems.get(i);
            if (!isPaginationMarker(item)) {
                return item;
            }
        }
        return null;
    }
}
