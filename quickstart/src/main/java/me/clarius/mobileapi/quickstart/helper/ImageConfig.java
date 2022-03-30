package me.clarius.mobileapi.quickstart.helper;

import android.os.Bundle;
import android.util.Size;

import me.clarius.mobileapi.MobileApi;

/**
 * Build the bundle containing the imaging configuration to send with MSG_CONFIGURE_IMAGE.
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
    public ImageConfig setSize(int width, int height) {
        mBundle.putSize(MobileApi.KEY_IMAGE_SIZE, new Size(width, height));
        return this;
    }

    /**
     * Set the optional compression type.
     */
    public ImageConfig setCompressionType(String type) {
        mBundle.putString(MobileApi.KEY_COMPRESSION_TYPE, type);
        return this;
    }

    /**
     * Set the optional compression quality.
     */
    public ImageConfig setCompressionQuality(int quality) {
        mBundle.putInt(MobileApi.KEY_COMPRESSION_QUALITY, quality);
        return this;
    }

    /**
     * Set the flag for separating overlays.
     */
    public ImageConfig setSeparateOverlays(boolean separate) {
        mBundle.putBoolean(MobileApi.KEY_SEPARATE_OVERLAYS, separate);
        return this;
    }

    /**
     * Build the bundle for MSG_CONFIGURE_IMAGE.
     */
    public Bundle bundle() {
        return mBundle;
    }
}
