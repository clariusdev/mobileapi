package me.clarius.sdk.mobileapi.example;

import static me.clarius.sdk.mobileapi.example.BuildConfig.CLARIUS_PACKAGE_NAME;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import me.clarius.sdk.mobileapi.helper.ImageConfig;

/**
 * Helper functions.
 */

public class Utils {

    private static final String TAG = "MobileApi";

    /**
     * Print all Clarius packages in console to help debug failed connections
     */
    public static void printClariusPackages(Context context) {
        Log.d(TAG, "Listing installed Clarius packages.\n" +
            "Note: by default, the Quick Start app connects to the Clarius App version from the Play Store.\n" +
            "To connect to another version listed below, change the variable `clariusPackageName` in file `gradle.properties`.");
        List<PackageInfo> packages = context.getPackageManager().getInstalledPackages(PackageManager.GET_SERVICES);
        Predicate<PackageInfo> isClarius = packageInfo -> packageInfo.packageName.toLowerCase().contains("clarius");
        List<PackageInfo> clariusPackages = packages.stream().filter(isClarius).collect(Collectors.toList());
        if (clariusPackages.isEmpty()) {
            Log.e(TAG, "No Clarius package found.");
        } else {
            Log.d(TAG, "Found " + clariusPackages.size() + " Clarius packages.");
            clariusPackages.forEach(p -> Log.d(TAG, toString(p)));
        }
    }

    /**
     * Info about one Clarius package.
     */
    private static String toString(PackageInfo p) {
        StringBuilder ret = new StringBuilder("Package '");
        ret.append(p.packageName);
        ret.append("' version ");
        ret.append(p.versionName);
        ret.append(" with service(s): ");
        if ((p.services != null) && (p.services.length > 0)) {
            List<String> names = Arrays.stream(p.services).map(s -> s.name).collect(Collectors.toList());
            ret.append(TextUtils.join(", ", names));
        } else {
            ret.append("no service");
        }
        return ret.toString();
    }

    /**
     * Create an image config object form the app preferences.
     */
    public static ImageConfig createImageConfig(Context context, SharedPreferences prefs) {
        Resources res = context.getResources();
        // Note: width and height are stored as strings in the Android preferences, not integers.
        String widthString = prefs.getString("image_width", res.getString(R.string.default_width));
        if (null == widthString) throw new AssertionError();
        String heightString = prefs.getString("image_height", res.getString(R.string.default_width));
        if (null == heightString) throw new AssertionError();
        return new ImageConfig(Integer.parseInt(widthString), Integer.parseInt(heightString))
            .setCompressionType(prefs.getString("image_compression_type", res.getString(R.string.default_compression_type)))
            .setCompressionQuality(prefs.getInt("image_compression_quality", res.getInteger(R.integer.default_compression_quality)))
            .setSeparateOverlays(prefs.getBoolean("image_separate_overlays", res.getBoolean(R.bool.default_separate_overlays)));
    }

    /**
     * Start the Clarius App designated by the package name specified in the build config.
     */
    public static void startClariusApp(Context context) {
        Log.i(TAG, "Starting Clarius App...");
        Intent intent = context.getPackageManager().getLaunchIntentForPackage(CLARIUS_PACKAGE_NAME);
        if (null == intent) {
            Toast.makeText(context, "Could not find the Clarius App package, verify it is installed.", Toast.LENGTH_SHORT).show();
        } else {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_LAUNCH_ADJACENT);
            context.startActivity(intent);
        }
    }
}
