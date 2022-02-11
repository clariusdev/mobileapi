package me.clarius.mobileapi.quickstart.helper;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import androidx.core.app.ShareCompat;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.IOException;

/**
 * Keep track of a raw data download request.
 */

public class RawDataHandle {

    private static final String TAG = "MobileApi/RawData";

    private static final String RAW_DATA_DIR = "raw_data"; // from Manifest
    private static final String PROVIDER_NAME = "me.clarius.mobileapi.quickstart.fileprovider"; // from Manifest

    private final Context mContext;

    public String mCaptureId;
    public long mSizeBytes;
    public Uri mWritableUri;

    RawDataHandle(Context context, String captureId, long sizeBytes, Uri writableUri) {
        this.mContext = context;
        this.mCaptureId = captureId;
        this.mSizeBytes = sizeBytes;
        this.mWritableUri = writableUri;
    }

    /**
     * Create the destination file, generate its URI and grant writing permission to the Clarius App.
     *
     * Android prevents applications from different packages to share files, unless explicitly granting permission as demonstrated below.
     * The 3rd party app is responsible to create the destination file and grants permission to the Clarius App.
     * This is accomplished with a FileProvider declared in the Manifest file.
     * See https://developer.android.com/training/secure-file-sharing for details.
     */
    static RawDataHandle create(Context context, String packageName, String captureId, String fileName, long sizeBytes) throws IOException {
        File dir = new File(context.getCacheDir(), RAW_DATA_DIR);
        File newFile = new File(dir, fileName);
        if (!dir.exists()) {
            if (!dir.mkdirs())
                throw new IOException("Cannot create directory");
        }
        if (!newFile.exists()) {
            if (!newFile.createNewFile())
                throw new IOException("Cannot create file");
        }
        Uri uri = FileProvider.getUriForFile(context, PROVIDER_NAME, newFile);
        context.grantUriPermission(packageName, uri,
            Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION
        );
        return new RawDataHandle(context, captureId, sizeBytes, uri);
    }

    /**
     * Export the download file somewhere using Android's native sharing view controller.
     */
    public void shareFile(Activity activity) {
        String mime = mContext.getContentResolver().getType(mWritableUri);
        Log.d(TAG, "Sharing raw data with MIME type: " + mime);
        Intent intentShareFile = ShareCompat.IntentBuilder.from(activity)
            .setStream(mWritableUri)
            .setType(mime)
            .getIntent()
            .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        activity.startActivity(Intent.createChooser(intentShareFile, "Share File"));
    }
}
