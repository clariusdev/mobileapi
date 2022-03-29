package me.clarius.mobileapi.quickstart;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;

import java.util.ArrayList;
import java.lang.StringBuilder;
import java.util.HashMap;
import java.util.Map;

import me.clarius.mobileapi.ButtonInfo;
import me.clarius.mobileapi.PatientInfo;
import me.clarius.mobileapi.PosInfo;
import me.clarius.mobileapi.PowerInfo;
import me.clarius.mobileapi.ProcessedImageInfo;
import me.clarius.mobileapi.ProbeInfo;
import me.clarius.mobileapi.quickstart.helper.ImageConfig;
import me.clarius.mobileapi.quickstart.helper.MobileApiHelper;
import me.clarius.mobileapi.quickstart.helper.RawDataHandle;

public class ImageFragment extends Fragment {

    private static final String TAG = "MobileApi/ImageFragment";

    /** Clarius App package name, set in gradle.properties file. */
    private static final String CLARIUS_PACKAGE_NAME = BuildConfig.CLARIUS_PACKAGE_NAME;

    /** Clarius Mobile API Service name, set in gradle.properties file. */
    private static final String CLARIUS_SERVICE_NAME = BuildConfig.CLARIUS_SERVICE_NAME;

    private MobileApiHelper mClarius = null;
    private ImageView mImageView = null;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.image_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mImageView = requireActivity().findViewById(R.id.imageView);
        if (null == mImageView) throw new AssertionError();
        mClarius = new MobileApiHelper(requireContext(), CLARIUS_PACKAGE_NAME, CLARIUS_SERVICE_NAME);
        mClarius.setObserver(mClariusObserver);
        getDefaultSharedPreferences().registerOnSharedPreferenceChangeListener(mPreferenceListener);
    }

    @Override
    public void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter(Intents.CONNECT);
        filter.addAction(Intents.DISCONNECT);
        filter.addAction(Intents.ASK_SCAN_AREA);
        filter.addAction(Intents.ASK_PROBE_INFO);
        filter.addAction(Intents.ASK_FREEZE);
        filter.addAction(Intents.ASK_DEPTH);
        filter.addAction(Intents.ASK_GAIN);
        filter.addAction(Intents.ASK_PATIENT_INFO);
        filter.addAction(Intents.SEND_PATIENT_INFO);
        filter.addAction(Intents.USER_FN);
        requireContext().registerReceiver(mReceiver, filter);
    }

    @Override
    public void onPause() {
        super.onPause();
        requireContext().unregisterReceiver(mReceiver);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mClarius.disconnect();
    }

    SharedPreferences getDefaultSharedPreferences() {
        return PreferenceManager.getDefaultSharedPreferences(requireContext());
    }

    private ImageConfig makeImageConfig(SharedPreferences p) {
        if (null == p) throw new AssertionError();
        Resources res = getResources();
        // Note: width and height are stored as strings in the Android preferences, not integers.
        String widthString = p.getString("image_width", res.getString(R.string.default_width));
        if (null == widthString) throw new AssertionError();
        String heightString = p.getString("image_height", res.getString(R.string.default_width));
        if (null == heightString) throw new AssertionError();
        return new ImageConfig(Integer.parseInt(widthString), Integer.parseInt(heightString))
            .compressionType(p.getString("image_compression_type", res.getString(R.string.default_compression_type)))
            .compressionQuality(p.getInt("image_compression_quality", res.getInteger(R.integer.default_compression_quality)))
            .separateOverlays(p.getBoolean("image_separate_overlays", res.getBoolean(R.bool.default_separate_overlays)));
    }

    private void sendImageConfig(SharedPreferences p) {
        try {
            mClarius.sendImageConfig(makeImageConfig(p));
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private void sendPackageName() {
        try {
            mClarius.sendPackageName();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    SharedPreferences.OnSharedPreferenceChangeListener mPreferenceListener = (sharedPreferences, key) -> {
        if (key.startsWith("image_")) {
            sendImageConfig(sharedPreferences);
        }
    };

    private final MobileApiHelper.Observer mClariusObserver = new MobileApiHelper.Observer() {
        @Override
        public void onConnected(boolean connected) {
            String log = "Connected: " + connected;
            Log.v(TAG, log);
            Toast.makeText(getContext(), log, Toast.LENGTH_SHORT).show();
            if (connected) {
                sendImageConfig(getDefaultSharedPreferences());
                sendPackageName();
            }
        }
        @Override
        public void onFrozen(boolean frozen) {
            String log = "Frozen: " + frozen;
            Log.v(TAG, log);
            Toast.makeText(getContext(), log, Toast.LENGTH_SHORT).show();
        }
        @Override
        public void onDepthChanged(double cm) {
            String log = "Depth: " + cm + " cm";
            Log.v(TAG, log);
            Toast.makeText(getContext(), log, Toast.LENGTH_SHORT).show();
        }
        @Override
        public void onGainChanged(double gain) {
            String log = "Gain: " + gain;
            Log.v(TAG, log);
            Toast.makeText(getContext(), log, Toast.LENGTH_SHORT).show();
        }
        @Override
        public void onScanAreaChanged(Rect rect) {
            String log = "B-image area: " + rect;
            Log.v(TAG, log);
        }
        @Override
        public void onNewProcessedImage(Bitmap imageData, ProcessedImageInfo imageInfo, ArrayList<PosInfo> posInfo) {
            if (null != mImageView) {
                mImageView.setImageBitmap(imageData);
            }
            if (mImageInfo.update(imageInfo)) {
                String log = "Image size: " + imageInfo.width + " x " + imageInfo.height;
                Log.i(TAG, log);
                Toast.makeText(getContext(), log, Toast.LENGTH_SHORT).show();
            }
        }
        @Override
        public void onButtonEvent(ButtonInfo info) {
            String log = "Button: " + buttonName(info.id) + " x" + info.clicks + " long? " + info.longPress;
            Log.i(TAG, log);
            Toast.makeText(getContext(), log, Toast.LENGTH_SHORT).show();
        }
        String buttonName(int buttonId) {
            switch (buttonId) {
                case ButtonInfo.BUTTON_UP: return "up";
                case ButtonInfo.BUTTON_DOWN: return "down";
            }
            return "???";
        }
        // Keep the previous image info and compare it to the new image info.
        class ImageInfoWrapper {
            private ProcessedImageInfo mPrev;
            public boolean update(ProcessedImageInfo info) {
                if (null == info)
                    return false;
                if (mPrev == null || mPrev.width != info.width || mPrev.height != info.height) {
                    mPrev = info;
                    return true;
                }
                return false;
            }
        }
        final ImageInfoWrapper mImageInfo = new ImageInfoWrapper();
        @Override
        public void onProbeInfoReceived(ProbeInfo probeInfo) {
            StringBuilder log = new StringBuilder();
            log.append("Probe info: ");
            if (null != probeInfo) {
                log.append("v").append(probeInfo.version).append(", elements: ").append(probeInfo.elements).append(", pitch: ").append(probeInfo.pitch).append(" radius: ").append(probeInfo.radius);
            } else {
                log.append("not connected");
            }
            Log.i(TAG, log.toString());
            Toast.makeText(getContext(), "Probe info received and logged in console", Toast.LENGTH_SHORT).show();
        }
        @Override
        public void onPatientInfoReceived(PatientInfo patientInfo) {
            String log = "Patient info: " + patientInfo.id + " - " + patientInfo.name;
            Log.i(TAG, log);
            Toast.makeText(getContext(), log, Toast.LENGTH_SHORT).show();
        }
        @Override
        public void onError(String msg) {
            Toast.makeText(getContext(), "Error: " + msg, Toast.LENGTH_SHORT).show();
        }
        @Override
        public void onLicenseChanged(boolean hasLicense) {
            String log = "3rd party app license changed: " + hasLicense;
            Log.i(TAG, log);
            Toast.makeText(getContext(), log, Toast.LENGTH_SHORT).show();
            if (hasLicense) {
                sendImageConfig(getDefaultSharedPreferences());
            }
        }
        @Override
        public void onRawDataCopied(RawDataHandle handle) {
            if (null != handle) {
                handle.shareFile(getActivity());
            }
        }
        private Map<Integer, String> powerStrings() {
            Map<Integer, String> strings = new HashMap<>();
            strings.put(PowerInfo.POWER_OFF_IDLE, "Idle");
            strings.put(PowerInfo.POWER_OFF_BATTERY, "Battery");
            strings.put(PowerInfo.POWER_OFF_TEMPERATURE, "Temperature");
            strings.put(PowerInfo.POWER_OFF_BUTTON, "Button");
            return strings;
        }
        @Override
        public void onPowerEvent(PowerInfo powerInfo) {
            String log = "Power event: " + powerStrings().get(powerInfo.type);
            Log.i(TAG, log);
            Toast.makeText(getContext(), log, Toast.LENGTH_SHORT).show();
        }
    };

    interface IntentHandler {
        void run(Intent intent) throws Exception;
    }

    // Receive intents from MainActivity.
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        private final Map<String, IntentHandler> mIntentHandlers = makeIntentHandlers();
        private Map<String, IntentHandler> makeIntentHandlers() {
            HashMap<String, IntentHandler> ret = new HashMap<>();
            ret.put(Intents.CONNECT, (intent) -> mClarius.connect());
            ret.put(Intents.DISCONNECT, (intent) -> mClarius.disconnect());
            ret.put(Intents.ASK_SCAN_AREA, (intent) -> mClarius.askScanArea());
            ret.put(Intents.ASK_PROBE_INFO, (intent) -> mClarius.askProbeInfo());
            ret.put(Intents.ASK_PATIENT_INFO, (intent) -> mClarius.askPatientInfo());
            ret.put(Intents.ASK_FREEZE, (intent) -> mClarius.askFreeze());
            ret.put(Intents.ASK_DEPTH, (intent) -> mClarius.askDepth());
            ret.put(Intents.ASK_GAIN, (intent) -> mClarius.askGain());
            ret.put(Intents.SEND_PATIENT_INFO, (intent) -> mClarius.sendPatientInfo());
            ret.put(Intents.USER_FN, (intent) -> {
                Bundle extras = intent.getExtras();
                if (null != extras) {
                    mClarius.userFn(
                        extras.getString(Intents.KEY_USER_FN),
                        extras.getDouble(Intents.KEY_USER_PARAM, 0)
                    );
                }
            });
            return ret;
        }
        @Override
        public void onReceive(Context context, Intent intent) {
            IntentHandler handler = mIntentHandlers.get(intent.getAction());
            if (null != handler) {
                try {
                    handler.run(intent);
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        }
    };
}
