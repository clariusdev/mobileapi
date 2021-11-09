package me.clarius.mobileapi.quickstart.helper;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.function.Consumer;

import me.clarius.mobileapi.ButtonInfo;
import me.clarius.mobileapi.MobileApi;
import me.clarius.mobileapi.PatientInfo;
import me.clarius.mobileapi.PosInfo;
import me.clarius.mobileapi.PowerInfo;
import me.clarius.mobileapi.ProbeInfo;
import me.clarius.mobileapi.ProcessedImageInfo;

/**
 * Wrapper for the Clarius Mobile API Service.
 *
 * Support for:
 * - bind/unbind to the Android service;
 * - receive messages from the service and notify the client through the Observer interface;
 * - send messages to the service.
 *
 * Also demonstrates callback parameters, used to map outbound messages to return status messages from the service.
 *
 * How it works:
 *  1. When sending a message, set Message.arg1 to a unique value.
 *      Here, it is simply set to the message code;
 *  2. Wait for a MSG_RETURN_STATUS from the service and check its Message.arg1 field:
 *      a. if it matches, it is the return status for our request;
 *      b. otherwise, it is the return status for another request;
 *  3. Use different codes to differentiate requests.
 */

public class MobileApiHelper {

    private static final String TAG = "MobileApi/Helper";

    /** Clarius App package name. */
    private final String mPackageName;

    /** Clarius Mobile API Service name. */
    private final String mServiceName;

    /** Context needed to bind to service. */
    private final Context mContext;

    /** Messenger to send messages to the service. */
    private Messenger mService = null;

    /** Flag indicating whether we have called bind on the service. */
    private boolean mBound = false;

    /** Target we publish for service to send messages to IncomingHandler. */
    private Messenger mMessenger = null;

    /** Flag indicating whether we have registered our own messenger with the service. */
    private boolean mRegistered = false;

    /** Observer set by the user of this class. */
    private Observer mObserver = null;

    /** Keep track of raw data download requests. */
    private RawDataDownload mRawDataDownload = null;

    /**
     * Send important events to client.
     */
    public interface Observer {
        void onConnected(boolean connected);
        void onFrozen(boolean frozen);
        void onDepthChanged(double cm);
        void onGainChanged(double gain);
        void onNewProcessedImage(Bitmap imageData, ProcessedImageInfo imageInfo, ArrayList<PosInfo> posInfo);
        void onButtonEvent(int buttonId, int clicks, int longPress);
        void onScanAreaChanged(Rect rect);
        void onProbeInfoReceived(ProbeInfo probeInfo);
        void onPatientInfoReceived(PatientInfo patientInfo);
        void onError(String msg);
        void onLicenseChanged(boolean hasLicense);
        void onRawDataDownloaded(RawDataDownload download);
        void onRawDataDownloadProgress(int progress);
        void onPowerEvent(PowerInfo powerInfo);
    }

    /** Helper to report errors. */
    private void logAndReportError(String message) {
        Log.e(TAG, message);
        if (null != mObserver) mObserver.onError(message);
    }

    /**
     * Constructor, the service is initially disconnected.
     * @param context Application context needed to bind/unbind to the Clarius Mobile API Service.
     */
    public MobileApiHelper(Context context, String packageName, String serviceName) {
        mContext = context.getApplicationContext();
        mPackageName = packageName;
        mServiceName = serviceName;
    }

    /**
     * Register an observer to get important messages from the service.
     * @param observer Object that implements the Observer interface.
     */
    public void setObserver(Observer observer) {
        mObserver = observer;
    }

    /** Connect to the service: bind and register our messenger. Does nothing if already bound. */
    public void connect() {
        if (mBound)
            return;
        Log.v(TAG, "Connecting to Clarius service, package: " + mPackageName + " class: " + mServiceName);
        Intent i = new Intent();
        i.setComponent(new ComponentName(mPackageName, mServiceName));
        boolean res = mContext.bindService(i, mConnection, Context.BIND_AUTO_CREATE);
        if (!res)
        {
            logAndReportError("Could not find the Clarius Mobile API service. Check the package name in the gradle.properties file.");
            mContext.unbindService(mConnection);
        }
    }

    /**
     * Disconnect from the service: unregister our messenger and unbind. Does nothing if not bound.
     */
    public void disconnect() {
        if (!mBound)
            return;
        if (mRegistered) {
            unregisterMessenger();
        }
        Log.v(TAG, "Disconnecting from Clarius service");
        mContext.unbindService(mConnection);
        mBound = false;
        if (null != mObserver) {
            mObserver.onConnected(false);
        }
    }

    /**
     * Send the new image configuration to the service (required to start imaging). Does nothing if not bound.
     *
     * @param config contains the imaging parameters as a bundle.
     */
    public void sendImageConfig(ImageConfig config) throws RemoteException {
        if (!mBound)
            return;
        Log.v(TAG, "Sending image config"
            + " " + config.bundle().getSize(MobileApi.KEY_IMAGE_SIZE)
            + " " + config.bundle().getString(MobileApi.KEY_COMPRESSION_TYPE)
            + " " + config.bundle().getInt(MobileApi.KEY_COMPRESSION_QUALITY)
            + " separateOverlay=" + config.bundle().getBoolean(MobileApi.KEY_SEPARATE_OVERLAYS)
        );
        Message msg = Message.obtain(null, MobileApi.MSG_CONFIGURE_IMAGE);
        msg.replyTo = mMessenger;
        msg.setData(config.bundle());
        setCallbackParam(msg, MobileApi.MSG_CONFIGURE_IMAGE);
        mService.send(msg);
    }

    /**
     * Sends the package name to the Clarius App so the App toggle icon will show up and allow switching of Apps
     */
    public void sendPackageName() throws RemoteException {
        if (!mBound)
            return;
        String packageName = mContext.getPackageName();
        Log.v(TAG, "Sending package name" + packageName);
        Message msg = Message.obtain(null, MobileApi.MSG_3P_PACKAGE);
        msg.replyTo = mMessenger;
        Bundle data = new Bundle();
        data.putString(MobileApi.KEY_PACKAGE_NAME, packageName);
        msg.setData(data);
        setCallbackParam(msg, MobileApi.MSG_3P_PACKAGE);
        mService.send(msg);
    }

    /** Ask the service to give us the current scan area geometry. Does nothing if not bound. */
    public void askScanArea() throws RemoteException {
        if (!mBound)
            return;
        Log.v(TAG, "Asking scan area");
        Message msg = Message.obtain(null, MobileApi.MSG_GET_SCAN_AREA);
        msg.replyTo = mMessenger;
        setCallbackParam(msg, MobileApi.MSG_GET_SCAN_AREA);
        mService.send(msg);
    }

    /** Ask the service to give us the current probe info. Does nothing if not bound. */
    public void askProbeInfo() throws RemoteException {
        if (!mBound)
            return;
        Log.v(TAG, "Asking probe info");
        Message msg = Message.obtain(null, MobileApi.MSG_GET_PROBE_INFO);
        msg.replyTo = mMessenger;
        setCallbackParam(msg, MobileApi.MSG_GET_PROBE_INFO);
        mService.send(msg);
    }

    /** Ask the service to give us the current patient info. Does nothing if not bound. */
    public void askPatientInfo() throws RemoteException {
        if (!mBound)
            return;
        Log.v(TAG, "Asking patient info");
        Message msg = Message.obtain(null, MobileApi.MSG_GET_PATIENT_INFO);
        msg.replyTo = mMessenger;
        setCallbackParam(msg, MobileApi.MSG_GET_PATIENT_INFO);
        mService.send(msg);
    }

    /** Ask the service to give us the current imaging depth. Does nothing if not bound. */
    public void askDepth() throws RemoteException {
        if (!mBound)
            return;
        Log.v(TAG, "Asking imaging depth");
        Message msg = Message.obtain(null, MobileApi.MSG_GET_DEPTH);
        msg.replyTo = mMessenger;
        setCallbackParam(msg, MobileApi.MSG_GET_DEPTH);
        mService.send(msg);
    }

    /** Ask the service to give us the current imaging gain. Does nothing if not bound. */
    public void askGain() throws RemoteException {
        if (!mBound)
            return;
        Log.v(TAG, "Asking imaging gain");
        Message msg = Message.obtain(null, MobileApi.MSG_GET_GAIN);
        msg.replyTo = mMessenger;
        setCallbackParam(msg, MobileApi.MSG_GET_GAIN);
        mService.send(msg);
    }

    /**
     * Run a user function on the scanner. Does nothing if not bound.
     *
     * @param fn The identifier of the function to run, one of MobileApi.USER_FN_* constants.
     */
    public void userFn(String fn, double param) throws RemoteException {
        if (!mBound)
            return;
        Log.v(TAG, "Running user function: " + fn + "(" + param + ")");
        Message msg = Message.obtain(null, MobileApi.MSG_USER_FN);
        msg.replyTo = mMessenger;
        Bundle data = new Bundle();
        data.putString(MobileApi.KEY_USER_FN, fn);
        data.putDouble(MobileApi.KEY_USER_PARAM, param);
        msg.setData(data);
        setCallbackParam(msg, MobileApi.MSG_USER_FN);
        mService.send(msg);
    }

    /**
     * Ask the service to download raw data from the probe and save it in a file. Does nothing if not bound.
     *
     * Note: this command does not start the raw data collection.
     * This must be done manually from the Clarius app.
     */
    public void downloadRawData() throws RemoteException, IOException {
        if (!mBound)
            return;
        if (null != mRawDataDownload) {
            Log.e(TAG, "Raw data download already in progress");
            return;
        }
        Log.v(TAG, "Downloading raw data");
        int startFrame = 0;
        int endFrame = 0;
        mRawDataDownload = RawDataDownload.create(mContext, mPackageName, startFrame, endFrame);
        Message msg = Message.obtain(null, MobileApi.MSG_DOWNLOAD_RAW_DATA);
        msg.replyTo = mMessenger;
        setCallbackParam(msg, MobileApi.MSG_DOWNLOAD_RAW_DATA);
        Bundle data = new Bundle();
        data.putLong(MobileApi.KEY_START_FRAME, mRawDataDownload.startFrame);
        data.putLong(MobileApi.KEY_END_FRAME, mRawDataDownload.endFrame);
        data.putParcelable(MobileApi.KEY_WRITABLE_URI, mRawDataDownload.writableUri);
        msg.setData(data);
        mService.send(msg);
    }

    /** Showing how to set the callback parameter that will be returned in MSG_RETURN_STATUS message. */
    static private void setCallbackParam(Message msg, int param) {
        msg.arg1 = param;
    }

    /** Showing how to get the return status from MSG_RETURN_STATUS message. */
    static private int getReturnStatus(Message msg) {
        return msg.arg2;
    }

    /** Showing how to get the callback parameter from MSG_RETURN_STATUS message. */
    static private int getCallbackParam(Message msg) {
        return msg.arg1;
    }

    /** Class for interacting with the main interface of the service. */
    private final ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            // This is called when the connection with the service has been
            // established, giving us the object we can use to
            // interact with the service. We are communicating with the
            // service using a messenger, so here we get a client-side
            // representation of that from the raw IBinder object.
            if (service == null) throw new AssertionError("Received null service");
            Log.v(TAG, "Service connected");
            mService = new Messenger(service);
            mBound = true;
            registerMessenger();
        }
        @Override
        public void onServiceDisconnected(ComponentName className) {
            // This is called when the connection with the service has been
            // unexpectedly disconnected -- that is, its process crashed.
            Log.v(TAG, "Service disconnected");
            mService = null;
            mBound = false;
        }
        @Override
        public void onNullBinding(ComponentName name) {
            // This is called when the service was not able to start,
            // for example when the Clarius App is not running.
            mContext.unbindService(mConnection);
            logAndReportError("Cannot bind to '" + name.getShortClassName() + "', is the Clarius App running?");
        }
    };

    /** Handler of incoming messages from service. */
    private class IncomingHandler implements Handler.Callback {
        @Override
        public boolean handleMessage(Message msg) {
            if (MobileApi.MSG_FREEZE_CHANGED == msg.what) {
                if (null != mObserver) {
                    mObserver.onFrozen(msg.getData().getBoolean(MobileApi.KEY_FREEZE));
                }
                return true;
            }
            if (MobileApi.MSG_DEPTH_CHANGED == msg.what) {
                if (null != mObserver) {
                    mObserver.onDepthChanged(msg.getData().getDouble(MobileApi.KEY_DEPTH_CM));
                }
                return true;
            }
            if (MobileApi.MSG_GAIN_CHANGED == msg.what) {
                if (null != mObserver) {
                    mObserver.onGainChanged(msg.getData().getDouble(MobileApi.KEY_GAIN));
                }
                return true;
            }
            if (MobileApi.MSG_RETURN_STATUS == msg.what) {
                int param = getCallbackParam(msg);
                int status = getReturnStatus(msg);
                Log.v(TAG, "Return status: " + status + ", param: " + param);
                if (null != mObserver && MobileApi.MSG_REGISTER_CLIENT == param) {
                    mObserver.onConnected(0 == status);
                }
                return true;
            }
            if (MobileApi.MSG_NEW_PROCESSED_IMAGE == msg.what) {
                try {
                    onImageUpdated(msg.getData());
                } catch (Throwable e) {
                    logAndReportError("Error when receiving processed image: " + e);
                }
                return true;
            }
            if (MobileApi.MSG_BUTTON_EVENT == msg.what) {
                try {
                    onButtonEvent(msg.getData());
                } catch (Throwable e) {
                    logAndReportError("Error when receiving button event: " + e);
                }
                return true;
            }
            if (MobileApi.MSG_SCAN_AREA_CHANGED == msg.what) {
                try {
                    onScanAreaChanged(msg.getData());
                } catch (Throwable e) {
                    Log.e(TAG, "Error when receiving scan area geometry: " + e);
                }
                return true;
            }
            if (MobileApi.MSG_RETURN_SCAN_AREA == msg.what) {
                Log.v(TAG, "MSG_RETURN_SCAN_AREA returned with callback param: " + getCallbackParam(msg));
                try {
                    onScanAreaChanged(msg.getData());
                } catch (Throwable e) {
                    Log.e(TAG, "Error when receiving scan area geometry: " + e);
                }
                return true;
            }
            if (MobileApi.MSG_RETURN_PROBE_INFO == msg.what) {
                Log.v(TAG, "MSG_RETURN_PROBE_INFO returned with callback param: " + getCallbackParam(msg));
                if (null != mObserver) {
                    emit(mObserver::onProbeInfoReceived, msg, ProbeInfo.class, MobileApi.KEY_PROBE_INFO);
                }
                return true;
            }
            if (MobileApi.MSG_RETURN_PATIENT_INFO == msg.what) {
                Log.v(TAG, "MSG_RETURN_PATIENT_INFO returned with callback param: " + getCallbackParam(msg));
                if (null != mObserver) {
                    emit(mObserver::onPatientInfoReceived, msg, PatientInfo.class, MobileApi.KEY_PATIENT_INFO);
                }
                return true;
            }
            if (MobileApi.MSG_NO_LICENSE == msg.what) {
                logAndReportError("No license");
                return true;
            }
            if (MobileApi.MSG_RETURN_DEPTH == msg.what) {
                Log.v(TAG, "MSG_RETURN_DEPTH returned with callback param: " + getCallbackParam(msg));
                if (null != mObserver) {
                    mObserver.onDepthChanged(msg.getData().getDouble(MobileApi.KEY_DEPTH_CM));
                }
                return true;
            }
            if (MobileApi.MSG_RETURN_GAIN == msg.what) {
                Log.v(TAG, "MSG_RETURN_GAIN returned with callback param: " + getCallbackParam(msg));
                if (null != mObserver) {
                    mObserver.onGainChanged(msg.getData().getDouble(MobileApi.KEY_GAIN));
                }
                return true;
            }
            if (MobileApi.MSG_ERROR == msg.what) {
                Bundle data = msg.getData();
                String error = null != data ? data.getString(MobileApi.KEY_ERROR_MESSAGE, "<unknown>") : "<unknown>";
                logAndReportError("Service error: " + error);
                return true;
            }
            if (MobileApi.MSG_LICENSE_CHANGED == msg.what) {
                if (null != mObserver) {
                    mObserver.onLicenseChanged(msg.arg1 == 1);
                }
                return true;
            }
            if (MobileApi.MSG_RETURN_RAW_DATA == msg.what) {
                Log.v(TAG, "MSG_RETURN_RAW_DATA returned with callback param: " + getCallbackParam(msg));
                try {
                    onRawDataDownloaded(msg.getData());
                } catch (Throwable e) {
                    logAndReportError("Error when receiving raw data: " + e);
                }
                return true;
            }
            if (MobileApi.MSG_RAW_DATA_DOWNLOAD_PROGRESS == msg.what) {
                Log.v(TAG, "MSG_RAW_DATA_DOWNLOAD_PROGRESS returned with callback param: " + getCallbackParam(msg) + " progress: " + msg.arg2);
                if (null != mObserver) {
                    mObserver.onRawDataDownloadProgress(msg.arg2);
                }
                return true;
            }
            if (MobileApi.MSG_POWER_EVENT == msg.what) {
                try {
                    onPowerEvent(msg.getData());
                } catch (Throwable e) {
                    logAndReportError("Error when receiving power event: " + e);
                }
            }
            return false;
        }
    }

    /**
     * Send our own messenger to the service so it can send messages to us.
     *
     * Also demonstrating the use of Message.arg1: it will be copied back in the reply.
     * When we receive the return status with our code, we can consider we are connected.
     */
    private void registerMessenger() {
        if (!mBound)
            return;
        Log.v(TAG, "Registering messenger");
        mMessenger = new Messenger(new Handler(Looper.getMainLooper(), new IncomingHandler()));
        Message msg = Message.obtain(null, MobileApi.MSG_REGISTER_CLIENT);
        msg.replyTo = mMessenger;
        setCallbackParam(msg, MobileApi.MSG_REGISTER_CLIENT);
        try {
            mService.send(msg);
            mRegistered = true;
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /** Remove our own messenger from the service to stop receiving messages from the service. */
    private void unregisterMessenger() {
        if (!mBound)
            return;
        Log.v(TAG, "Unregistering messenger");
        Message msg = Message.obtain(null, MobileApi.MSG_UNREGISTER_CLIENT);
        msg.replyTo = mMessenger;
        try {
            mService.send(msg);
            mRegistered = false;
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /** Extract the image data received from the service. */
    private void onImageUpdated(Bundle data) {
        if (null == mObserver)
            return;
        data.setClassLoader(ProcessedImageInfo.class.getClassLoader());
        ProcessedImageInfo info = data.getParcelable(MobileApi.KEY_IMAGE_INFO);
        if (info == null) throw new AssertionError("image info missing");
        data.setClassLoader(PosInfo.class.getClassLoader());
        ArrayList<PosInfo> posInfo = data.getParcelableArrayList(MobileApi.KEY_POS_INFO);
        byte[] imageData = data.getByteArray(MobileApi.KEY_IMAGE_DATA);
        if (imageData == null) throw new AssertionError("image data missing");
        Bitmap bitmap = BitmapFactory.decodeByteArray(imageData, 0, imageData.length);
        if (bitmap == null) throw new AssertionError("bad image data");
        mObserver.onNewProcessedImage(bitmap, info, posInfo);
    }

    /** Helper to convert a parcelable from a Bundle and send it to the observer (field can be null). */
    private <T> void emit(Consumer<T> fn, Message msg, Class<T> klass, String field) {
        Bundle data = msg.getData();
        data.setClassLoader(klass.getClassLoader());
        fn.accept(data.getParcelable(field));
    }

    /** Extract the button event info received from the service. */
    private void onButtonEvent(Bundle data) {
        if (null == mObserver)
            return;
        data.setClassLoader(ButtonInfo.class.getClassLoader());
        ButtonInfo info = data.getParcelable(MobileApi.KEY_BUTTON_INFO);
        if (info == null) throw new AssertionError("button event info missing");
        mObserver.onButtonEvent(info.id, info.clicks, info.longPress);
    }

    /** Extract the scan area geometry received from the service. */
    private void onScanAreaChanged(Bundle data) {
        if (null == mObserver)
            return;
        Rect imageArea = data.getParcelable(MobileApi.KEY_B_IMAGE_AREA);
        if (imageArea == null) throw new AssertionError("B-image area info missing");
        mObserver.onScanAreaChanged(imageArea);
    }

    /** Extract the power info received from the service. */
    private void onPowerEvent(Bundle data) {
        if (null == mObserver)
            return;
        data.setClassLoader(PowerInfo.class.getClassLoader());
        PowerInfo powerInfo = data.getParcelable(MobileApi.KEY_POWER_INFO);
        if (powerInfo == null) throw new AssertionError("Power info missing");
        mObserver.onPowerEvent(powerInfo);
    }

    /** Extract the raw data info received from the service. */
    private void onRawDataDownloaded(Bundle data) throws IOException {
        if (null == mRawDataDownload)
            throw new AssertionError("Received raw data download completion but request is missing");
        RawDataDownload download = mRawDataDownload;
        mRawDataDownload = null;
        download.revokePermissions();
        if (data.containsKey(MobileApi.KEY_ERROR_MESSAGE)) {
            logAndReportError("Raw data download failed: " + data.getString(MobileApi.KEY_ERROR_MESSAGE));
        }
        else {
            boolean available = data.getBoolean(MobileApi.KEY_AVAILABLE, false);
            long packageSize = data.getLong(MobileApi.KEY_PACKAGE_SIZE, -1);
            String packageExtension = data.getString(MobileApi.KEY_PACKAGE_EXTENSION);
            Log.d(TAG, "Raw data available? " + available + ", size: " + packageSize + ", extension: " + packageExtension);
            download.onReceived(available, packageSize, packageExtension);
            mObserver.onRawDataDownloaded(download);
        }
    }
}
