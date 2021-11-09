package me.clarius.mobileapi.quickstart.helper;

import android.os.Bundle;
import android.util.Size;

import me.clarius.mobileapi.MobileApi;

/**
 * Prepare the bundle data to add to MSG_CONFIGURE_IMAGE messages to the service.
 */

public class ImageConfig {
    private final Bundle mBundle;
    /**
     * Construct the bundle with the required image dimensions.
     */
    public ImageConfig(int width, int height) {
        mBundle = new Bundle();
        mBundle.putSize(MobileApi.KEY_IMAGE_SIZE, new Size(width, height));
    }
    /**
     * Set the image dimensions.
     */
    public ImageConfig size(int width, int height) {
        mBundle.putSize(MobileApi.KEY_IMAGE_SIZE, new Size(width, height));
        return this;
    }
    /**
     * Set the optional compression type.
     */
    public ImageConfig compressionType(String type) {
        mBundle.putString(MobileApi.KEY_COMPRESSION_TYPE, type);
        return this;
    }
    /**
     * Set the optional compression quality.
     */
    public ImageConfig compressionQuality(int quality) {
        mBundle.putInt(MobileApi.KEY_COMPRESSION_QUALITY, quality);
        return this;
    }
    /**
     * Set the flag for separating overlays.
     */
    public ImageConfig separateOverlays(boolean separate) {
        mBundle.putBoolean(MobileApi.KEY_SEPARATE_OVERLAYS, separate);
        return this;
    }
    /**
     * Get the bundle for MSG_CONFIGURE_IMAGE.
     */
    public Bundle bundle() {
        return mBundle;
    }
}
