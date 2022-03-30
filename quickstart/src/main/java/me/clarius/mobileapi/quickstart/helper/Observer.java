package me.clarius.mobileapi.quickstart.helper;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.RemoteException;
import android.util.Log;
import android.util.Size;
import android.widget.Toast;

import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceManager;

import java.util.ArrayList;

import me.clarius.mobileapi.ButtonInfo;
import me.clarius.mobileapi.PatientInfo;
import me.clarius.mobileapi.PosInfo;
import me.clarius.mobileapi.PowerInfo;
import me.clarius.mobileapi.ProbeInfo;
import me.clarius.mobileapi.ProcessedImageInfo;
import me.clarius.mobileapi.quickstart.ImageViewModel;
import me.clarius.mobileapi.quickstart.MainActivity;
import me.clarius.mobileapi.quickstart.Utils;

/**
 * Observe events from MobileApiHelper.
 */

public class Observer implements MobileApiHelper.Observer {
    private static final String TAG = "MobileApi";
    private final MainActivity context;
    private final MobileApiHelper api;
    private final ImageViewModel viewModel;
    private final Prop<String> gainProp = new Prop<>();
    private final Prop<String> depthProp = new Prop<>();
    private final Prop<Size> imageSizeProp = new Prop<>();
    private final Prop<Rect> scanAreaProp = new Prop<>();

    private static String doubleToShortString(double d) {
        return String.format("%.2f", d);
    }

    public Observer(MainActivity context, MobileApiHelper api) {
        this.context = context;
        this.api = api;
        this.viewModel = new ViewModelProvider(context).get(ImageViewModel.class);
    }

    private void logToast(String message) {
        Log.v(TAG, message);
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    private void sendImageConfig(SharedPreferences preferences) {
        try {
            api.sendImageConfig(Utils.createImageConfig(context, preferences));
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private void sendPackageName() {
        try {
            api.sendPackageName();
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
            sendPackageName();
        }
    }

    @Override
    public void onFrozen(boolean frozen) {
        logToast("Frozen: " + frozen);
    }

    @Override
    public void onDepthChanged(double cm) {
        if (depthProp.update(doubleToShortString(cm))) {
            logToast("Depth: " + depthProp.value + " cm");
        }
    }

    @Override
    public void onGainChanged(double gain) {
        Log.v("JLE", "doubleToShortString: " + doubleToShortString(gain));
        if (gainProp.update(doubleToShortString(gain))) {
            logToast("Gain: " + gainProp.value);
        }
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
    public void onProbeInfoReceived(ProbeInfo probeInfo) {
        logToast(Strings.toString(probeInfo));
    }

    @Override
    public void onPatientInfoReceived(PatientInfo patientInfo) {
        logToast(Strings.toString(patientInfo));
    }

    @Override
    public void onError(String msg) {
        logToast("Error: " + msg);
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
    public void onRawDataCopied(RawDataHandle handle) {
        if (null != handle) {
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
