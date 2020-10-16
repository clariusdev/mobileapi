package me.clarius.mobileapi.quickstart;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.PointF;
import android.graphics.Rect;
import android.os.Bundle;
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

import me.clarius.mobileapi.ButtonInfo;
import me.clarius.mobileapi.PosInfo;
import me.clarius.mobileapi.ProcessedImageInfo;
import me.clarius.mobileapi.ProbeInfo;
import me.clarius.mobileapi.quickstart.helper.ImageConfig;
import me.clarius.mobileapi.quickstart.helper.MobileApiHelper;

public class ImageFragment extends Fragment {

    private static final String TAG = "ImageFragment";

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
        mClarius = new MobileApiHelper(requireContext());
        mClarius.setObserver(mClariusObserver);
        getDefaultSharedPreferences().registerOnSharedPreferenceChangeListener(mPreferenceListener);
    }

    @Override
    public void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter(Intents.CONNECT);
        filter.addAction(Intents.DISCONNECT);
        filter.addAction(Intents.ASK_SCAN_AREA);
        filter.addAction(Intents.ASK_SCAN_CONVERT);
        filter.addAction(Intents.ASK_PROBE_INFO);
        filter.addAction(Intents.ASK_DEPTH);
        filter.addAction(Intents.ASK_GAIN);
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
                .compressionQuality(p.getInt("image_compression_quality", res.getInteger(R.integer.default_compression_quality)));
    }

    SharedPreferences.OnSharedPreferenceChangeListener mPreferenceListener = (sharedPreferences, key) -> {
        if (key.startsWith("image_")) {
            mClarius.sendImageConfig(makeImageConfig(sharedPreferences));
        }
    };

    private MobileApiHelper.Observer mClariusObserver = new MobileApiHelper.Observer() {
        @Override
        public void onConnected(boolean connected) {
            String log = "Connected: " + connected;
            Toast.makeText(getContext(), log, Toast.LENGTH_SHORT).show();
            if (connected) {
                mClarius.sendImageConfig(makeImageConfig(getDefaultSharedPreferences()));
            }
        }
        @Override
        public void onFrozen(boolean frozen) {
            String log = "Frozen: " + frozen;
            Toast.makeText(getContext(), log, Toast.LENGTH_SHORT).show();
        }
        @Override
        public void onDepthChanged(double cm) {
            String log = "Depth: " + cm + " cm";
            Toast.makeText(getContext(), log, Toast.LENGTH_SHORT).show();
        }
        @Override
        public void onGainChanged(double gain) {
            String log = "Gain: " + gain;
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
                Toast.makeText(getContext(), log, Toast.LENGTH_SHORT).show();
            }
        }
        @Override
        public void onButtonEvent(int buttonId, int clicks, int longPress) {
            String log = "Button: " + buttonName(buttonId) + " x" + clicks + " long? " + longPress;
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
        ImageInfoWrapper mImageInfo = new ImageInfoWrapper();
        @Override
        public void onProbeInfoChanged(ProbeInfo probeInfo) {
            StringBuilder log = new StringBuilder();
            log.append("Probe info: ");
            if (null != probeInfo) {
                log.append("v").append(probeInfo.version).append(", elements: ").append(probeInfo.elements).append(", pitch: ").append(probeInfo.pitch).append(" radius: ").append(probeInfo.radius);
            } else {
                log.append("not connected");
            }
            Log.i(TAG, log.toString());
        }
        @Override
        public void onScanConvertChanged(PointF originMicrons, double pixelSizeMicrons) {
            String log = "Scan convert origin: " + originMicrons + ", pixel size: " + pixelSizeMicrons;
            Log.i(TAG, log);
        }
        @Override
        public void onError(String msg) {
            Toast.makeText(getContext(), "Error: " + msg, Toast.LENGTH_SHORT).show();
        }
        @Override
        public void onLicenseChanged(boolean hasLicense) {
            String log = "3rd party app license changed: " + hasLicense;
            Toast.makeText(getContext(), log, Toast.LENGTH_SHORT).show();
            if (hasLicense) {
                mClarius.sendImageConfig(makeImageConfig(getDefaultSharedPreferences()));
            }
        }
    };

    // Receive intents from MainActivity.
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (Intents.CONNECT.equals(action)) {
                mClarius.connect();
            }
            else if (Intents.DISCONNECT.equals(action)) {
                mClarius.disconnect();
            }
            else if (Intents.ASK_SCAN_AREA.equals(action)) {
                mClarius.askScanArea();
            }
            else if (Intents.ASK_SCAN_CONVERT.equals(action)) {
                mClarius.askScanConvert();
            }
            else if (Intents.ASK_PROBE_INFO.equals(action)) {
                mClarius.askProbeInfo();
            }
            else if (Intents.ASK_DEPTH.equals(action)) {
                mClarius.askDepth();
            }
            else if (Intents.ASK_GAIN.equals(action)) {
                mClarius.askGain();
            }
            else if (Intents.USER_FN.equals(action)) {
                Bundle extras = intent.getExtras();
                if (null != extras) {
                    mClarius.userFn(
                            extras.getString(Intents.KEY_USER_FN),
                            extras.getDouble(Intents.KEY_USER_PARAM, 0)
                    );
                }
            }
        }
    };
}
