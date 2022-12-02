package me.clarius.sdk.mobileapi.example;

import static me.clarius.sdk.mobileapi.example.BuildConfig.CLARIUS_PACKAGE_NAME;
import static me.clarius.sdk.mobileapi.example.BuildConfig.FILE_PROVIDER_NAME;
import static me.clarius.sdk.mobileapi.example.BuildConfig.FILE_PROVIDER_PATH;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.RemoteException;
import android.util.Log;
import android.util.Size;
import android.widget.Toast;

import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Optional;

import me.clarius.sdk.mobileapi.ButtonInfo;
import me.clarius.sdk.mobileapi.PatientInfo;
import me.clarius.sdk.mobileapi.PosInfo;
import me.clarius.sdk.mobileapi.PowerInfo;
import me.clarius.sdk.mobileapi.ProbeInfo;
import me.clarius.sdk.mobileapi.ProcessedImageInfo;
import me.clarius.sdk.mobileapi.helper.ApiHelper;
import me.clarius.sdk.mobileapi.helper.RawDataHandle;
import me.clarius.sdk.mobileapi.helper.RawDataHandleMap;
import me.clarius.sdk.mobileapi.helper.Strings;

/**
 * Observe events from the service.
 */

public class Listener implements ApiHelper.Listener {

    private static final String TAG = "MobileApi";
    private final MainActivity context;
    private final ApiHelper api;
    private final ImageViewModel viewModel;
    private final RawDataHandleMap rawDataMap;
    private final Prop<String> gainProp = new Prop<>();
    private final Prop<String> depthProp = new Prop<>();
    private final Prop<Size> imageSizeProp = new Prop<>();
    private final Prop<Rect> scanAreaProp = new Prop<>();

    private static String doubleToShortString(double d) {
        return String.format("%.2f", d);
    }

    public Listener(MainActivity context, ApiHelper api) {
        this.context = context;
        this.api = api;
        this.viewModel = new ViewModelProvider(context).get(ImageViewModel.class);
        this.rawDataMap = new RawDataHandleMap(context, CLARIUS_PACKAGE_NAME, FILE_PROVIDER_NAME, FILE_PROVIDER_PATH);
    }

    private void logToast(String message) {
        Log.v(TAG, message);
        showToast(message);
    }

    private void showToast(String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    private void sendImageConfig(SharedPreferences preferences) {
        try {
            api.sendImageConfig(Utils.createImageConfig(context, preferences));
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private SharedPreferences getDefaultSharedPreferences() {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }

    @Override
    public void onConnected(boolean connected) {
        logToast("Connected: " + connected);
        if (connected) {
            sendImageConfig(getDefaultSharedPreferences());
        }
    }

    @Override
    public void onFrozenChanged(boolean frozen) {
        logToast("Frozen: " + frozen);
    }

    @Override
    public void onFrozenReturned(boolean frozen) {
        logToast("Reply for frozen: " + frozen);
    }

    @Override
    public void onDepthChanged(double cm) {
        if (depthProp.update(doubleToShortString(cm))) {
            logToast("Depth: " + depthProp.value + " cm");
        }
    }

    @Override
    public void onDepthReturned(double cm) {
        logToast("Reply for depth: " + depthProp.value + " cm");
    }

    @Override
    public void onGainChanged(double gain) {
        if (gainProp.update(doubleToShortString(gain))) {
            logToast("Gain: " + gainProp.value);
        }
    }

    @Override
    public void onGainReturned(double gain) {
        logToast("Reply for gain: " + gainProp.value);
    }

    @Override
    public void onNewProcessedImage(Bitmap imageData, ProcessedImageInfo imageInfo, ArrayList<PosInfo> posInfo) {
        viewModel.setBImage(imageData);
        if (imageSizeProp.update(new Size(imageInfo.width, imageInfo.height))) {
            logToast("Image size: " + imageInfo.width + " x " + imageInfo.height);
        }
    }

    @Override
    public void onButtonEvent(ButtonInfo info) {
        logToast(Strings.toString(info));
    }

    @Override
    public void onScanAreaChanged(Rect rect) {
        if (scanAreaProp.update(rect)) {
            logToast("B-image area: " + rect);
        }
    }

    @Override
    public void onScanAreaReturned(Rect rect) {
        logToast("Reply for B-image area: " + rect);
    }

    @Override
    public void onProbeInfoReceived(ProbeInfo probeInfo) {
        logToast(Strings.toString(probeInfo));
    }

    @Override
    public void onPatientInfoReceived(PatientInfo patientInfo) {
        logToast(Strings.toString(patientInfo));
    }

    @Override
    public void onError(String msg) {
        Log.e(TAG, msg);
        showToast(msg);
    }

    @Override
    public void onLicenseChanged(boolean hasLicense) {
        logToast("3rd party app license changed: " + hasLicense);
        if (hasLicense) {
            sendImageConfig(getDefaultSharedPreferences());
        }
    }

    @Override
    public void onPowerEvent(PowerInfo powerInfo) {
        logToast(Strings.toString(powerInfo));
    }

    @Override
    public void onRawDataAvailable(String captureId, String fileName, long sizeBytes) {
        logToast("New raw data available"
            + ", capture ID: " + captureId
            + ", file name: " + fileName
            + ", size (bytes): " + sizeBytes);
        try {
            RawDataHandle handle = rawDataMap.emplace(captureId, fileName, sizeBytes);
            api.copyRawData(captureId, handle.mWritableUri);
            logToast("Sent request to copy the raw data");
        } catch (IOException e) {
            e.printStackTrace();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRawDataCopied(String captureId, Optional<String> error) {
        RawDataHandle handle = rawDataMap.get(captureId);
        if (error.isPresent()) {
            logToast("Failed to copy raw data: " + error.get());
        } else {
            logToast("Raw data copied");
            handle.shareFile(context);
        }
    }

    /**
     * A property emits a notification when its value was changed (here, it simply returns a boolean).
     */
    class Prop<T> {
        T value;

        boolean update(T newValue) {
            if (newValue != null && !newValue.equals(value)) {
                value = newValue;
                return true;
            }
            return false;
        }
    }
}
