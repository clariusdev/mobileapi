package me.clarius.sdk.mobileapi.helper;

import android.content.Context;

import androidx.annotation.Nullable;

import java.io.IOException;
import java.util.HashMap;

/**
 * Helper class to create and store raw data handles for copying raw data.
 */

public class RawDataHandleMap extends HashMap<String, RawDataHandle> {

    private final Context context;
    private final String packageName;
    private final String fileProviderName;
    private final String fileProviderPath;

    /**
     * Construct an empty map.
     */
    public RawDataHandleMap(Context context, String packageName, String fileProviderName, String fileProviderPath) {
        this.context = context;
        this.packageName = packageName;
        this.fileProviderName = fileProviderName;
        this.fileProviderPath = fileProviderPath;
    }

    /**
     * Create a raw data handle and put it in the map.
     */
    @Nullable
    public RawDataHandle emplace(String captureId, String fileName, long sizeBytes) throws IOException {
        RawDataHandle handle = RawDataHandle.create(context,
            packageName, fileProviderName, fileProviderPath,
            captureId, fileName, sizeBytes);
        super.put(captureId, handle);
        return handle;
    }
}
