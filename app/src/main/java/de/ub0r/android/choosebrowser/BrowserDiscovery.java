package de.ub0r.android.choosebrowser;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Discovers browsers installed on the device.
 */
public class BrowserDiscovery {

    private final Context context;
    private final PackageManager pm;

    public BrowserDiscovery(@NonNull Context context) {
        this.context = context;
        this.pm = context.getPackageManager();
    }

    /**
     * Discover all browsers that can handle HTTP/HTTPS URLs.
     *
     * @return List of BrowserInfo for all discovered browsers
     */
    @NonNull
    public List<BrowserInfo> discoverAllBrowsers() {
        Uri testUri = Uri.parse("https://example.com");
        Intent intent = new Intent(Intent.ACTION_VIEW, testUri);
        List<ResolveInfo> resolveInfos = pm.queryIntentActivities(intent, PackageManager.MATCH_ALL);

        List<BrowserInfo> browsers = new ArrayList<>();
        String myPackage = context.getPackageName();

        for (ResolveInfo info : resolveInfos) {
            if (!myPackage.equals(info.activityInfo.packageName)) {
                browsers.add(BrowserInfo.fromResolveInfo(info, pm));
            }
        }
        return browsers;
    }

    /**
     * Discover all browsers for a specific URI.
     *
     * @param uri The URI to check
     * @return List of BrowserInfo for browsers that can handle this URI
     */
    @NonNull
    public List<BrowserInfo> discoverBrowsersForUri(@NonNull Uri uri) {
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        List<ResolveInfo> resolveInfos = pm.queryIntentActivities(intent, PackageManager.MATCH_ALL);

        List<BrowserInfo> browsers = new ArrayList<>();
        String myPackage = context.getPackageName();

        for (ResolveInfo info : resolveInfos) {
            if (!myPackage.equals(info.activityInfo.packageName)) {
                browsers.add(BrowserInfo.fromResolveInfo(info, pm));
            }
        }
        return browsers;
    }

    /**
     * Detect newly installed browsers by comparing with known browsers.
     *
     * @param knownKeys Set of unique keys for previously known browsers
     * @return List of newly discovered browsers
     */
    @NonNull
    public List<BrowserInfo> detectNewBrowsers(@NonNull Set<String> knownKeys) {
        List<BrowserInfo> allBrowsers = discoverAllBrowsers();
        List<BrowserInfo> newBrowsers = new ArrayList<>();

        for (BrowserInfo browser : allBrowsers) {
            if (!knownKeys.contains(browser.getUniqueKey())) {
                newBrowsers.add(browser);
            }
        }
        return newBrowsers;
    }

    /**
     * Get unique keys for all currently installed browsers.
     *
     * @return Set of unique keys
     */
    @NonNull
    public Set<String> getAllBrowserKeys() {
        List<BrowserInfo> browsers = discoverAllBrowsers();
        Set<String> keys = new HashSet<>();
        for (BrowserInfo browser : browsers) {
            keys.add(browser.getUniqueKey());
        }
        return keys;
    }

    /**
     * Filter browsers based on hidden list.
     *
     * @param browsers   List of browsers to filter
     * @param hiddenKeys Set of unique keys for hidden browsers
     * @return Filtered list with hidden browsers removed
     */
    @NonNull
    public static List<BrowserInfo> filterHiddenBrowsers(
            @NonNull List<BrowserInfo> browsers,
            @NonNull Set<String> hiddenKeys) {
        List<BrowserInfo> visible = new ArrayList<>();
        for (BrowserInfo browser : browsers) {
            if (!hiddenKeys.contains(browser.getUniqueKey())) {
                visible.add(browser);
            }
        }
        return visible;
    }

    /**
     * Check if a browser is still installed.
     *
     * @param browser The browser to check
     * @return true if still installed, false otherwise
     */
    public boolean isBrowserInstalled(@NonNull BrowserInfo browser) {
        try {
            pm.getActivityInfo(
                    new android.content.ComponentName(browser.getPackageName(), browser.getActivityName()),
                    0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    /**
     * Filter out uninstalled browsers from a list.
     *
     * @param browsers List of browsers to check
     * @return List with only currently installed browsers
     */
    @NonNull
    public List<BrowserInfo> filterInstalledBrowsers(@NonNull List<BrowserInfo> browsers) {
        List<BrowserInfo> installed = new ArrayList<>();
        for (BrowserInfo browser : browsers) {
            if (isBrowserInstalled(browser)) {
                installed.add(browser);
            }
        }
        return installed;
    }
}
