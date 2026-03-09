package de.ub0r.android.choosebrowser;

import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Objects;

/**
 * Data model representing a browser application.
 */
public class BrowserInfo {

    private final String packageName;
    private final String activityName;
    private String label;
    private transient Drawable icon;
    private transient PackageManager packageManager;

    public BrowserInfo(@NonNull String packageName, @NonNull String activityName) {
        this.packageName = packageName;
        this.activityName = activityName;
    }

    @NonNull
    public static BrowserInfo fromResolveInfo(@NonNull ResolveInfo info, @NonNull PackageManager pm) {
        BrowserInfo browserInfo = new BrowserInfo(
                info.activityInfo.packageName,
                info.activityInfo.name
        );
        browserInfo.packageManager = pm;
        browserInfo.label = info.loadLabel(pm).toString();
        browserInfo.icon = info.loadIcon(pm);
        return browserInfo;
    }

    @NonNull
    public String getPackageName() {
        return packageName;
    }

    @NonNull
    public String getActivityName() {
        return activityName;
    }

    @NonNull
    public String getUniqueKey() {
        return packageName + "/" + activityName;
    }

    @Nullable
    public String getLabel() {
        return label;
    }

    public void setLabel(@Nullable String label) {
        this.label = label;
    }

    @Nullable
    public Drawable getIcon() {
        return icon;
    }

    public void setIcon(@Nullable Drawable icon) {
        this.icon = icon;
    }

    public void setPackageManager(@NonNull PackageManager pm) {
        this.packageManager = pm;
    }

    /**
     * Load label and icon from PackageManager if not already loaded.
     */
    public void loadDetails(@NonNull PackageManager pm) {
        if (label == null || icon == null) {
            try {
                android.content.pm.ActivityInfo activityInfo = pm.getActivityInfo(
                        new android.content.ComponentName(packageName, activityName), 0);
                if (label == null) {
                    label = activityInfo.loadLabel(pm).toString();
                }
                if (icon == null) {
                    icon = activityInfo.loadIcon(pm);
                }
            } catch (PackageManager.NameNotFoundException e) {
                // Browser may have been uninstalled
                if (label == null) {
                    label = packageName;
                }
            }
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BrowserInfo that = (BrowserInfo) o;
        return Objects.equals(packageName, that.packageName) &&
                Objects.equals(activityName, that.activityName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(packageName, activityName);
    }

    @NonNull
    @Override
    public String toString() {
        return "BrowserInfo{" +
                "packageName='" + packageName + '\'' +
                ", activityName='" + activityName + '\'' +
                ", label='" + label + '\'' +
                '}';
    }
}
