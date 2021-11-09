package me.clarius.mobileapi.quickstart;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.text.TextUtils;
import android.util.Log;

import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class ClariusPackages {
    private static final String TAG = "MobileApi/Packages";

    public static void print(Context context) {
        Log.d(TAG, "Listing installed Clarius packages.\n" +
            "Note: by default, the Quick Start app connects to the Clarius App version from the Play Store.\n" +
            "To connect to another version listed below, change the variable `clariusPackageName` in file `gradle.properties`.");
        List<PackageInfo> packages = context.getPackageManager().getInstalledPackages(PackageManager.GET_SERVICES);
        Predicate<PackageInfo> isClarius = packageInfo -> packageInfo.packageName.toLowerCase().contains("clarius");
        List<PackageInfo> clariusPackages = packages.stream().filter(isClarius).collect(Collectors.toList());
        if (clariusPackages.isEmpty()) {
            Log.e(TAG, "No Clarius package found.");
        }
        else {
            Log.d(TAG, "Found " + clariusPackages.size() + " Clarius packages.");
            clariusPackages.forEach(p -> Log.d(TAG, toString(p)));
        }
    }

    private static String toString(PackageInfo p) {
        StringBuilder ret = new StringBuilder("Package '");
        ret.append(p.packageName);
        ret.append("' with service(s): ");
        if ((p.services != null) && (p.services.length > 0)) {
            List<String> names = Arrays.stream(p.services).map(s -> s.name).collect(Collectors.toList());
            ret.append(TextUtils.join(", ", names));
        }
        else {
            ret.append("no service");
        }
        return ret.toString();
    }
}
