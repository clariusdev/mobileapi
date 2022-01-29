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
import java.lang.String;

/**
 * Keep track of a raw data download request.
 */

public class RawDataDownload {

    private static final String TAG = "MobileApi/RawData";

    private static final String RAW_DATA_DIR = "raw_data"; // from Manifest
    private static final String PROVIDER_NAME = "me.clarius.mobileapi.quickstart.fileprovider"; // from Manifest

    private final Context mContext;

    public long startFrame;
    public long endFrame;
    public Uri writableUri;

    // set by onReceived():
    public boolean available = false;
    public long packageSize = 0;
    public String packageExtension;

    RawDataDownload(Context context, long startFrame, long endFrame, Uri writableUri) {
        this.mContext = context;
        this.startFrame = startFrame;
        this.endFrame = endFrame;
        this.writableUri = writableUri;
    }

    /**
     * Create the destination file, generate its URI and grant writing permission to the Clarius App.
     *
     * Android prevents applications from different packages to share files, unless explicitly granting permission as demonstrated below.
     * The 3rd party app is responsible to create the destination file and grants permission to the Clarius App.
     * This is accomplished with a FileProvider declared in the Manifest file.
     * See https://developer.android.com/training/secure-file-sharing for details.
     */
    static RawDataDownload create(Context context, String packageName, long startFrame, long endFrame) throws IOException {
        File dir = new File(context.getFilesDir(), RAW_DATA_DIR);
        File newFile = new File(dir, uniqueFilename());
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
        return new RawDataDownload(context, startFrame, endFrame, uri);
    }

    void revokePermissions() {
        mContext.revokeUriPermission(writableUri,
            Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION
        );
    }

    /**
     * Call after receiving the raw data from the Clarius App.
     *
     * At this point, URI permissions can be revoked.
     */
    void onReceived(boolean available, long packageSize, String packageExtension) throws IOException {
        this.available = available;
        this.packageSize = packageSize;
        this.packageExtension = packageExtension;
        // rename file and update uri
        if (packageExtension && !packageExtension.isEmpty())
        {
            // there is probably a better way to rename the file but I didn't figure it out
            File dir = new File(mContext.getFilesDir(), RAW_DATA_DIR);
            File from = new File(dir, writableUri.getLastPathSegment());
            File to = new File(dir, writableUri.getLastPathSegment() + packageExtension);
            if (!from.renameTo(to))
                throw new IOException("Cannot rename file");
            boolean exists = to.exists();
            Log.d(TAG, "Raw data file " + to + " exists? " + exists);
            writableUri = FileProvider.getUriForFile(mContext, PROVIDER_NAME, to);
        }
    }

    /**
     * Export the download file somewhere using Android's native sharing view controller.
     */
    public void shareFile(Activity activity) {
        String mime = mContext.getContentResolver().getType(writableUri);
        Log.d(TAG, "Sharing raw data with MIME type: " + mime);
        Intent intentShareFile = ShareCompat.IntentBuilder.from(activity)
            .setStream(writableUri)
            .setType(mime)
            .getIntent()
            .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        activity.startActivity(Intent.createChooser(intentShareFile, "Share File"));
    }

    private static String uniqueFilename() {
        return "raw_" + System.currentTimeMillis();
    }
}
