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
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

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
 * Here, a callback parameter is used to detect the reply to our MSG_REGISTER_CLIENT requests:
 *
 *  1. When sending MSG_REGISTER_CLIENT, set Message.arg1 to a unique value.
 *      Here, it is simply set to the message code MSG_REGISTER_CLIENT.
 *  2. Wait for a MSG_RETURN_STATUS from the service and check its Message.arg1 field:
 *      a. if it matches, it is the return status for our request;
 *      b. otherwise, it is the return status for another request.
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
    private Map<String, RawDataHandle> mRawDataMap = new HashMap<>();

    /**
     * Send important events to client.
     */
    public interface Observer {
        void onConnected(boolean connected);
        void onFrozen(boolean frozen);
        void onDepthChanged(double cm);
        void onGainChanged(double gain);
        void onNewProcessedImage(Bitmap imageData, ProcessedImageInfo imageInfo, ArrayList<PosInfo> posInfo);
        void onButtonEvent(ButtonInfo info);
        void onScanAreaChanged(Rect rect);
        void onProbeInfoReceived(ProbeInfo probeInfo);
        void onPatientInfoReceived(PatientInfo patientInfo);
        void onError(String msg);
        void onLicenseChanged(boolean hasLicense);
        void onPowerEvent(PowerInfo powerInfo);
        void onRawDataCopied(RawDataHandle handle);
    }

    /** Helper to report errors. */
    private void logAndReportError(String message) {
        Log.e(TAG, message);
        if (null != mObserver)
            mObserver.onError(message);
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
        if (mRegistered)
            unregisterMessenger();
        Log.v(TAG, "Disconnecting from Clarius service");
        mContext.unbindService(mConnection);
        mBound = false;
        if (null != mObserver)
            mObserver.onConnected(false);
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
        mService.send(msg);
    }

    /** Ask the service to give us the current scan area geometry. Does nothing if not bound. */
    public void askScanArea() throws RemoteException {
        if (!mBound)
            return;
        Log.v(TAG, "Asking scan area");
        Message msg = Message.obtain(null, MobileApi.MSG_GET_SCAN_AREA);
        msg.replyTo = mMessenger;
        mService.send(msg);
    }

    /** Ask the service to give us the current probe info. Does nothing if not bound. */
    public void askProbeInfo() throws RemoteException {
        if (!mBound)
            return;
        Log.v(TAG, "Asking probe info");
        Message msg = Message.obtain(null, MobileApi.MSG_GET_PROBE_INFO);
        msg.replyTo = mMessenger;
        mService.send(msg);
    }

    /** Ask the service to give us the current patient info. Does nothing if not bound. */
    public void askPatientInfo() throws RemoteException {
        if (!mBound)
            return;
        Log.v(TAG, "Asking patient info");
        Message msg = Message.obtain(null, MobileApi.MSG_GET_PATIENT_INFO);
        msg.replyTo = mMessenger;
        mService.send(msg);
    }

    /** Ask the service to give us the current freeze state. Does nothing if not bound. */
    public void askFreeze() throws RemoteException {
        if (!mBound)
            return;
        Log.v(TAG, "Asking freeze state");
        Message msg = Message.obtain(null, MobileApi.MSG_GET_FREEZE);
        msg.replyTo = mMessenger;
        mService.send(msg);
    }

    /** Ask the service to give us the current imaging depth. Does nothing if not bound. */
    public void askDepth() throws RemoteException {
        if (!mBound)
            return;
        Log.v(TAG, "Asking imaging depth");
        Message msg = Message.obtain(null, MobileApi.MSG_GET_DEPTH);
        msg.replyTo = mMessenger;
        mService.send(msg);
    }

    /** Ask the service to give us the current imaging gain. Does nothing if not bound. */
    public void askGain() throws RemoteException {
        if (!mBound)
            return;
        Log.v(TAG, "Asking imaging gain");
        Message msg = Message.obtain(null, MobileApi.MSG_GET_GAIN);
        msg.replyTo = mMessenger;
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
        mService.send(msg);
    }

    /** Showing how to populate the demographics page in the Clarius App. */
    public void sendPatientInfo() throws RemoteException {
        if (!mBound)
            return;
        PatientInfo info = new PatientInfo();
        info.id = "pid";
        info.name = "last name";
        Message msg = Message.obtain(null, MobileApi.MSG_SET_PATIENT_INFO);
        Bundle data = new Bundle();
        data.putParcelable(MobileApi.KEY_PATIENT_INFO, info);
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
            if (service == null)
                throw new AssertionError("Received null service");
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

    /** Helper to convert a parcelable from a Bundle and send it to the observer. */
    private <T> void emit(BiConsumer<Observer, T> fn, Message msg, Class<T> klass, String field) {
        if (null != mObserver) {
            Bundle data = msg.getData();
            if (null != klass)
                data.setClassLoader(klass.getClassLoader());
            T info = data.getParcelable(field);
            if (null == info)
                throw new AssertionError("Field missing '" + field + "'");
            fn.accept(mObserver, info);
        }
    }

    interface MessageHandler {
        void run(Message msg) throws Exception;
    }

    private Map<Integer, MessageHandler> makeMessageHandlers() {
        HashMap<Integer, MessageHandler> ret = new HashMap<>();
        ret.put(MobileApi.MSG_FREEZE_CHANGED, (Message msg) -> {
            if (null != mObserver) mObserver.onFrozen(msg.getData().getBoolean(MobileApi.KEY_FREEZE));
        });
        ret.put(MobileApi.MSG_DEPTH_CHANGED, (Message msg) -> {
            if (null != mObserver) mObserver.onDepthChanged(msg.getData().getDouble(MobileApi.KEY_DEPTH_CM));
        });
        ret.put(MobileApi.MSG_GAIN_CHANGED, (Message msg) -> {
            if (null != mObserver) mObserver.onGainChanged(msg.getData().getDouble(MobileApi.KEY_GAIN));
        });
        ret.put(MobileApi.MSG_RETURN_STATUS, (Message msg) -> {
            int param = getCallbackParam(msg);
            int status = getReturnStatus(msg);
            Log.v(TAG, "Return status: " + status + ", param: " + param);
            if (null != mObserver && MobileApi.MSG_REGISTER_CLIENT == param) {
                mObserver.onConnected(0 == status);
            }
        });
        ret.put(MobileApi.MSG_NEW_PROCESSED_IMAGE, this::onImageUpdated);
        ret.put(MobileApi.MSG_BUTTON_EVENT, (Message msg) -> {
            emit(Observer::onButtonEvent, msg, ButtonInfo.class, MobileApi.KEY_BUTTON_INFO);
        });
        ret.put(MobileApi.MSG_SCAN_AREA_CHANGED, (Message msg) -> {
            emit(Observer::onScanAreaChanged, msg, null, MobileApi.KEY_B_IMAGE_AREA);
        });
        ret.put(MobileApi.MSG_RETURN_SCAN_AREA, (Message msg) -> {
            emit(Observer::onScanAreaChanged, msg, null, MobileApi.KEY_B_IMAGE_AREA);
        });
        ret.put(MobileApi.MSG_RETURN_PROBE_INFO, (Message msg) -> {
            emit(Observer::onProbeInfoReceived, msg, ProbeInfo.class, MobileApi.KEY_PROBE_INFO);
        });
        ret.put(MobileApi.MSG_RETURN_PATIENT_INFO, (Message msg) -> {
            emit(Observer::onPatientInfoReceived, msg, PatientInfo.class, MobileApi.KEY_PATIENT_INFO);
        });
        ret.put(MobileApi.MSG_NO_LICENSE, (Message msg) -> {
            logAndReportError("No license");
        });
        ret.put(MobileApi.MSG_RETURN_FREEZE, (Message msg) -> {
            if (null != mObserver) mObserver.onFrozen(msg.getData().getBoolean(MobileApi.KEY_FREEZE));
        });
        ret.put(MobileApi.MSG_RETURN_DEPTH, (Message msg) -> {
            if (null != mObserver) mObserver.onDepthChanged(msg.getData().getDouble(MobileApi.KEY_DEPTH_CM));
        });
        ret.put(MobileApi.MSG_RETURN_GAIN, (Message msg) -> {
            if (null != mObserver) mObserver.onGainChanged(msg.getData().getDouble(MobileApi.KEY_GAIN));
        });
        ret.put(MobileApi.MSG_ERROR, (Message msg) -> {
            String error = msg.getData().getString(MobileApi.KEY_ERROR_MESSAGE, "<unknown>");
            logAndReportError("Service error: " + error);
        });
        ret.put(MobileApi.MSG_LICENSE_CHANGED, (Message msg) -> {
            if (null != mObserver) mObserver.onLicenseChanged(msg.arg1 == 1);
        });
        ret.put(MobileApi.MSG_RAW_DATA_AVAILABLE, this::onRawDataAvailable);
        ret.put(MobileApi.MSG_RAW_DATA_COPIED, this::onRawDataCopied);
        ret.put(MobileApi.MSG_POWER_EVENT, (Message msg) -> {
            emit(Observer::onPowerEvent, msg, PowerInfo.class, MobileApi.KEY_POWER_INFO);
        });
        return ret;
    }

    /** Handler of incoming messages from service. */
    private class IncomingHandler implements Handler.Callback {
        final Map<Integer, MessageHandler> mMessageHandlers = makeMessageHandlers();
        @Override
        public boolean handleMessage(Message msg) {
            MessageHandler handler = mMessageHandlers.get(msg.what);
            if (null != handler) {
                try {
                    handler.run(msg);
                } catch (Throwable e) {
                    logAndReportError("Error when receiving message " + msg.what + ": " + e);
                }
                return true;
            }
            else {
                return false;
            }
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
    private void onImageUpdated(Message msg) {
        if (null == mObserver)
            return;
        Bundle data = msg.getData();
        data.setClassLoader(ProcessedImageInfo.class.getClassLoader());
        ProcessedImageInfo info = data.getParcelable(MobileApi.KEY_IMAGE_INFO);
        if (info == null)
            throw new AssertionError("image info missing");
        data.setClassLoader(PosInfo.class.getClassLoader());
        ArrayList<PosInfo> posInfo = data.getParcelableArrayList(MobileApi.KEY_POS_INFO);
        byte[] imageData = data.getByteArray(MobileApi.KEY_IMAGE_DATA);
        if (imageData == null)
            throw new AssertionError("image data missing");
        Bitmap bitmap = BitmapFactory.decodeByteArray(imageData, 0, imageData.length);
        if (bitmap == null)
            throw new AssertionError("bad image data");
        mObserver.onNewProcessedImage(bitmap, info, posInfo);
    }

    /** When new raw data is available, print the details and send a request to copy the archive. */
    private void onRawDataAvailable(Message msg) throws IOException, RemoteException {
        Bundle data = msg.getData();
        String captureId = data.getString(MobileApi.KEY_CAPTURE_ID);
        String fileName = data.getString(MobileApi.KEY_FILE_NAME);
        long sizeBytes = data.getLong(MobileApi.KEY_SIZE_BYTES);
        Log.v(TAG, "New raw data available"
            + ", capture ID: " + captureId
            + ", file name: " + fileName
            + ", size (bytes): " + sizeBytes);
        RawDataHandle handle = RawDataHandle.create(mContext, mPackageName, captureId, fileName, sizeBytes);
        Message reply = Message.obtain(null, MobileApi.MSG_COPY_RAW_DATA);
        reply.replyTo = mMessenger;
        data.putParcelable(MobileApi.KEY_WRITABLE_URI, handle.mWritableUri);
        reply.setData(data);
        mService.send(reply);
        mRawDataMap.put(captureId, handle);
        Log.v(TAG, "Sent request to copy the raw data");
    }

    private void onRawDataCopied(Message msg) {
        if (null == mObserver)
            return;
        Bundle data = msg.getData();
        String captureId = data.getString(MobileApi.KEY_CAPTURE_ID);
        RawDataHandle handle = mRawDataMap.get(captureId);
        if (data.containsKey(MobileApi.KEY_ERROR_MESSAGE)) {
            logAndReportError("Failed to copy raw data for capture " + captureId
                + ", error: " + data.getString(MobileApi.KEY_ERROR_MESSAGE));
        }
        else {
            Log.v(TAG, "Raw data copied for capture ID " + captureId);
            mObserver.onRawDataCopied(handle);
        }
    }
}
