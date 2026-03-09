package de.ub0r.android.choosebrowser;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Manages app preferences using SharedPreferences.
 */
public class PreferencesManager {

    private static final String PREFS_NAME = "choose_browser_prefs";

    private static final String KEY_SORT_MODE = "sort_mode";
    private static final String KEY_HIDDEN_BROWSERS = "hidden_browsers";
    private static final String KEY_CUSTOM_SORT_SELECTED = "custom_sort_selected";
    private static final String KEY_CUSTOM_SORT_UNSELECTED = "custom_sort_unselected";
    private static final String KEY_KNOWN_BROWSERS = "known_browsers";

    public static final String SORT_MODE_DEFAULT = "default";
    public static final String SORT_MODE_CUSTOM = "custom";

    private final SharedPreferences prefs;
    private final Gson gson;

    public PreferencesManager(@NonNull Context context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        gson = new Gson();
    }

    // ==================== Sort Mode ====================

    @NonNull
    public String getSortMode() {
        return prefs.getString(KEY_SORT_MODE, SORT_MODE_DEFAULT);
    }

    public void setSortMode(@NonNull String mode) {
        prefs.edit().putString(KEY_SORT_MODE, mode).apply();
    }

    public boolean isDefaultSortMode() {
        return SORT_MODE_DEFAULT.equals(getSortMode());
    }

    public boolean isCustomSortMode() {
        return SORT_MODE_CUSTOM.equals(getSortMode());
    }

    // ==================== Hidden Browsers ====================

    @NonNull
    public Set<String> getHiddenBrowsers() {
        String json = prefs.getString(KEY_HIDDEN_BROWSERS, "[]");
        Type type = new TypeToken<Set<String>>() {}.getType();
        Set<String> result = gson.fromJson(json, type);
        return result != null ? result : new HashSet<>();
    }

    public void setHiddenBrowsers(@NonNull Set<String> packageNames) {
        prefs.edit().putString(KEY_HIDDEN_BROWSERS, gson.toJson(packageNames)).apply();
    }

    public void addHiddenBrowser(@NonNull String uniqueKey) {
        Set<String> hidden = getHiddenBrowsers();
        hidden.add(uniqueKey);
        setHiddenBrowsers(hidden);
    }

    public void removeHiddenBrowser(@NonNull String uniqueKey) {
        Set<String> hidden = getHiddenBrowsers();
        hidden.remove(uniqueKey);
        setHiddenBrowsers(hidden);
    }

    public boolean isHidden(@NonNull String uniqueKey) {
        return getHiddenBrowsers().contains(uniqueKey);
    }

    // ==================== Custom Sort List ====================

    @NonNull
    public List<BrowserInfo> getCustomSortSelectedBrowsers() {
        String json = prefs.getString(KEY_CUSTOM_SORT_SELECTED, "[]");
        Type type = new TypeToken<List<BrowserInfo>>() {}.getType();
        List<BrowserInfo> result = gson.fromJson(json, type);
        return result != null ? result : new ArrayList<>();
    }

    public void setCustomSortSelectedBrowsers(@NonNull List<BrowserInfo> browsers) {
        prefs.edit().putString(KEY_CUSTOM_SORT_SELECTED, gson.toJson(browsers)).apply();
    }

    @NonNull
    public List<BrowserInfo> getCustomSortUnselectedBrowsers() {
        String json = prefs.getString(KEY_CUSTOM_SORT_UNSELECTED, "[]");
        Type type = new TypeToken<List<BrowserInfo>>() {}.getType();
        List<BrowserInfo> result = gson.fromJson(json, type);
        return result != null ? result : new ArrayList<>();
    }

    public void setCustomSortUnselectedBrowsers(@NonNull List<BrowserInfo> browsers) {
        prefs.edit().putString(KEY_CUSTOM_SORT_UNSELECTED, gson.toJson(browsers)).apply();
    }

    public void saveCustomSortLists(@NonNull List<BrowserInfo> selected, @NonNull List<BrowserInfo> unselected) {
        prefs.edit()
                .putString(KEY_CUSTOM_SORT_SELECTED, gson.toJson(selected))
                .putString(KEY_CUSTOM_SORT_UNSELECTED, gson.toJson(unselected))
                .apply();
    }

    // ==================== Known Browsers (for detecting new installs) ====================

    @NonNull
    public Set<String> getKnownBrowsers() {
        String json = prefs.getString(KEY_KNOWN_BROWSERS, "[]");
        Type type = new TypeToken<Set<String>>() {}.getType();
        Set<String> result = gson.fromJson(json, type);
        return result != null ? result : new HashSet<>();
    }

    public void setKnownBrowsers(@NonNull Set<String> uniqueKeys) {
        prefs.edit().putString(KEY_KNOWN_BROWSERS, gson.toJson(uniqueKeys)).apply();
    }

    public void addKnownBrowsers(@NonNull List<BrowserInfo> browsers) {
        Set<String> known = getKnownBrowsers();
        for (BrowserInfo browser : browsers) {
            known.add(browser.getUniqueKey());
        }
        setKnownBrowsers(known);
    }

    /**
     * Append new browsers to the end of custom sort selected list.
     */
    public void appendToCustomSortSelected(@NonNull List<BrowserInfo> newBrowsers) {
        List<BrowserInfo> selected = getCustomSortSelectedBrowsers();
        selected.addAll(newBrowsers);
        setCustomSortSelectedBrowsers(selected);
    }
}
