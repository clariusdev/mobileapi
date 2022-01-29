package me.clarius.mobileapi;

import java.lang.String;

//! Specify the Clarius Mobile API Service protocol.

public class MobileApi
{
    // Messages from client to server.

    //! Command to the service to register a client, to start receiving messages from the service.
    //!
    //! This Messenger will only be used to deliver app-generated events (e.g. freeze status, new image).
    //! In particular, it will not be used to deliver the return status of subsequent commands.
    //! Use the individual command's replyTo field for that purpose.
    //!
    //! Parameter(s):
    //! - Message.replyTo: client's Messenger where messages should be sent. This field is mandatory; if missing, the query will fail.
    //! - Message.arg1 (optional): callback parameter for MSG_RETURN_STATUS.
    //!
    //! Return: 0 = success, -1 = failure.

    public static final int MSG_REGISTER_CLIENT = 1;

    //! Command to the service to unregister a client, to stop receiving messages from the service.
    //!
    //! Parameter(s):
    //! - Message.replyTo: client's Messenger previously registered with MSG_REGISTER_CLIENT.
    //! - Message.arg1 (optional): callback parameter for MSG_RETURN_STATUS.
    //!
    //! Return: 0 = success, -1 = failure.

    public static final int MSG_UNREGISTER_CLIENT = 2;

    //! Command to the service to set the image rendering configuration.
    //!
    //! This will cause the service to start imaging if it is not already.
    //!
    //! Parameter(s):
    //! - Bundle[KEY_IMAGE_SIZE]: android.util.Size, the requested rendering size.
    //! - Bundle[KEY_COMPRESSION_TYPE] (optional): java.lang.String, the compression type. Possible values: COMPRESSION_TYPE_JPEG, COMPRESSION_TYPE_PNG. Default: DEFAULT_COMPRESSION_TYPE.
    //! - Bundle[KEY_COMPRESSION_QUALITY] (optional): int, the compression quality between 0 (worst quality) and 100 (best quality). Default: DEFAULT_COMPRESSION_QUALITY.
    //! - Bundle[KEY_SEPARATE_OVERLAYS] (optional): boolean, flag for separating overlays. If set, two images are sent: one with overlays and one without. Default: DEFAULT_SEPARATE_OVERLAYS. Added in version 8.0.1
    //! - Message.replyTo (optional): if set, Messenger to send the MSG_RETURN_STATUS message to.
    //! - Message.arg1 (optional): callback parameter for MSG_RETURN_STATUS.
    //!
    //! Return: 0 = success, -1 = failure.

    public static final int MSG_CONFIGURE_IMAGE = 3;

    //! Query the service for the current scan area.
    //!
    //! The reply will be delivered with message MSG_RETURN_SCAN_AREA.
    //!
    //! Parameter(s):
    //! - Message.replyTo: client's Messenger where the reply should be sent. This field is mandatory; if missing, the query will be ignored.
    //! - Message.arg1 (optional): callback parameter for MSG_RETURN_SCAN_AREA.

    public static final int MSG_GET_SCAN_AREA = 4;

    //! Query the service for the current probe info.
    //!
    //! The reply will be delivered with message MSG_RETURN_PROBE_INFO.
    //!
    //! Parameter(s):
    //! - Message.replyTo: client's Messenger where the reply should be sent. This field is mandatory; if missing, the query will be ignored.
    //! - Message.arg1 (optional): callback parameter for MSG_RETURN_PROBE_INFO.

    public static final int MSG_GET_PROBE_INFO = 5;

    //! Command to the service to execute a user function.
    //!
    //! Parameter(s):
    //! - Bundle[KEY_USER_FN]: java.lang.string, one of the predefined user functions from the USER_FN_* constants.
    //! - Bundle[KEY_USER_PARAM] (optional): double, parameter for the USER_FN_SET_* functions. Default: DEFAULT_USER_KEY_PARAM.
    //! - Message.replyTo (optional): if set, Messenger to send the MSG_RETURN_STATUS message to.
    //! - Message.arg1 (optional): callback parameter for MSG_RETURN_STATUS.
    //!
    //! Return: 0 = success, -1 = failure.
    //! Note: Success indicates the command was successfully transmitted, but not necessarily successfully executed.

    public static final int MSG_USER_FN = 6;

    //! Query the service for the current depth.
    //!
    //! The reply will be delivered with message MSG_RETURN_DEPTH.
    //!
    //! Parameter(s):
    //! - Message.replyTo: client's Messenger where the reply should be sent. This field is mandatory; if missing, the query will be ignored.
    //! - Message.arg1 (optional): callback parameter for MSG_RETURN_DEPTH.

    public static final int MSG_GET_DEPTH = 8;

    //! Query the service for the current gain.
    //!
    //! The reply will be delivered with message MSG_RETURN_GAIN.
    //!
    //! Parameter(s):
    //! - Message.replyTo: client's Messenger where the reply should be sent. This field is mandatory; if missing, the query will be ignored.
    //! - Message.arg1 (optional): callback parameter for MSG_RETURN_GAIN.

    public static final int MSG_GET_GAIN = 9;

    //! Command to the service to download raw data from the probe.
    //!
    //! Note: this command does not start the raw data collection.
    //! This must be done manually from the Clarius app.
    //!
    //! The reply will be delivered with message MSG_RETURN_RAW_DATA.
    //!
    //! Parameter(s):
    //! - Bundle[KEY_START_FRAME]: long, starting frame timestamp (0 for all). Default: DEFAULT_FRAME.
    //! - Bundle[KEY_END_FRAME]: long, ending frame timestamp (0 for all). Default: DEFAULT_FRAME.
    //! - Bundle[KEY_WRITABLE_URI]: android.net.Uri, the file URI where the service will write the package. It must have permissions for the Clarius app.
    //! - Message.replyTo: client's Messenger where the reply should be sent. This field is mandatory; if missing, the query will be ignored.
    //! - Message.arg1 (optional): callback parameter for MSG_RETURN_RAW_DATA.
    //!
    //! \version Added in version 8.0.1

    public static final int MSG_DOWNLOAD_RAW_DATA = 10;

    //! Query the service for the current patient info.
    //!
    //! The reply will be delivered with message MSG_RETURN_PATIENT_INFO.
    //!
    //! Parameter(s):
    //! - Message.replyTo: client's Messenger where the reply should be sent. This field is mandatory; if missing, the query will be ignored.
    //! - Message.arg1 (optional): callback parameter for MSG_RETURN_PATIENT_INFO.
    //!
    //! \version Added in version 8.6.0

    public static final int MSG_GET_PATIENT_INFO = 11;

    //! Command to the service to set the partner app package name.
    //!
    //! Parameter(s):
    //! - Bundle[KEY_PACKAGE_NAME]: java.lang.string, the package name of the partner app, for example: "com.MyName.MyApp"
    //! - Message.replyTo (optional): if set, Messenger to send the MSG_RETURN_STATUS message to.
    //! - Message.arg1 (optional): callback parameter for MSG_RETURN_STATUS.
    //!
    //! Return: 0 = success, -1 = failure.
    //!
    //! Notes:
    //! - Success indicates the command was successfully transmitted, but not necessarily successfully executed.
    //! - To remove the app launching handling from the Clarius App, send the empty string, "", not null.
    //!
    //! \version Added in version 8.6.0

    public static final int MSG_3P_PACKAGE = 12;

    //! Query the service for the current freeze state.
    //!
    //! The reply will be delivered with message MSG_RETURN_FREEZE.
    //!
    //! Parameter(s):
    //! - Message.replyTo: client's Messenger where the reply should be sent. This field is mandatory; if missing, the query will be ignored.
    //! - Message.arg1 (optional): callback parameter for MSG_RETURN_FREEZE
    //!
    //! \version Added in version 9.0.0

    public static final int MSG_GET_FREEZE = 13;

    //! Send patient info to the Clarius App.
    //!
    //! Parameter(s):
    //! - Bundle[KEY_PATIENT_INFO]: me.clarius.mobileapi.PatientInfo, patient info to set
    //! - Message.replyTo (optional): if set, Messenger to send the MSG_RETURN_STATUS message to.
    //! - Message.arg1 (optional): callback parameter for MSG_RETURN_STATUS.
    //!
    //! \version Added in version 8.6.0

    public static final int MSG_SET_PATIENT_INFO = 14;

    //! Send settings info to the Clarius App.
    //!
    //! Parameter(s):
    //! - Bundle[KEY_SETTINGS_INFO]: me.clarius.mobileapi.SettingsInfo, settings info to set
    //! - Message.replyTo (optional): if set, Messenger to send the MSG_RETURN_STATUS message to.
    //! - Message.arg1 (optional): callback parameter for MSG_RETURN_STATUS.
    //!
    //! \version Added in version 9.0.0

    public static final int MSG_SET_SETTINGS_INFO = 15;

    //! Send complete exam message to the Clarius App.
    //!
    //! Parameter(s):
    //! - Bundle[KEY_COMPLETE_EXAM]: complete exam enumeration (see COMPLETE_EXAM_XXX_
    //! - Message.replyTo (optional): if set, Messenger to send the MSG_RETURN_STATUS message to.
    //! - Message.arg1 (optional): callback parameter for MSG_RETURN_STATUS.
    //!
    //! \version Added in version 9.0.0

    public static final int MSG_COMPLETE_EXAM = 16;

    // Messages from server to client.

    //! Return the outcome of a command sent by the client.
    //!
    //! The return status is sent only if the initial Message had its replyTo field set.
    //!
    //! Parameters:
    //! - Message.arg1: callback parameter copied from the Message.arg1 field sent by the client. Can be used as a request ID.
    //! - Message.arg2: the return status.

    public static final int MSG_RETURN_STATUS = 101;

    //! Message to the client to notify freeze state changed
    //!
    //! Parameters:
    //! - Bundle[KEY_FREEZE]: boolean, freeze state

    public static final int MSG_FREEZE_CHANGED = 102;

    //! Message to the client to send a new processed image.
    //!
    //! Parameters:
    //! - Bundle[KEY_IMAGE_DATA]: Byte[], the image data.
    //! - Bundle[KEY_IMAGE_INFO]: me.clarius.mobileapi.ProcessedImageInfo, the image meta data.
    //! - Bundle[KEY_POS_INFO]: me.clarius.mobileapi.PosInfo[], an array of positional data (null if no positional data is available).

    public static final int MSG_NEW_PROCESSED_IMAGE = 103;

    //! Message to the client on button event.
    //!
    //! Parameters:
    //! - Bundle[KEY_BUTTON_INFO]: me.clarius.mobileapi.ButtonInfo, the button event info.
    //!
    //! Note: only button events associated to a user-defined function are sent.
    //! For example, if a button is associated to the pre-defined "freeze" function, it will not be sent.
    //! Button functions are configured in the app's settings.

    public static final int MSG_BUTTON_EVENT = 104;

    //! Message to the client when the scan area changes.
    //!
    //! The scan area allows 3rd party apps to know where the B-image is displayed, this can be used to display a custom overlay.
    //!
    //! Parameters:
    //! - Bundle[KEY_B_IMAGE_AREA]: android.graphics.Rect, location of the B-image displayed by the Clarius app in pixel coordinates, with the origin in the top left corner of the device's screen.

    public static final int MSG_SCAN_AREA_CHANGED = 105;

    //! Reply to queries MSG_GET_SCAN_AREA.
    //!
    //! See details MSG_SCAN_AREA_CHANGED.
    //!
    //! Parameters:
    //! - Bundle[KEY_B_IMAGE_AREA]: android.graphics.Rect, location of the B-image displayed by the Clarius app in pixel coordinates, with the origin in the top left corner of the device's screen.
    //! - Message.arg1: callback parameter copied from the Message.arg1 sent by the client.

    public static final int MSG_RETURN_SCAN_AREA = 106;

    //! Reply to queries MSG_GET_PROBE_INFO.
    //!
    //! Parameters:
    //! - Bundle[KEY_PROBE_INFO]: me.clarius.mobileapi.ProbeInfo, the current probe info. Can be null if no probe is connected.
    //! - Message.arg1: callback parameter copied from the Message.arg1 sent by the client.

    public static final int MSG_RETURN_PROBE_INFO = 107;

    //! Message to the client when the 3rd party app license has been revoked.
    //!
    //! This occurs when selecting a non-licensed probe.
    //! The service stops and does not resume, even if selecting a licensed probe, the service must be restarted.

    public static final int MSG_NO_LICENSE = 108;

    //! Message to the client when an internal error occurred.
    //!
    //! Parameters:
    //! - Bundle[KEY_ERROR_MESSAGE]: java.lang.String, the error message in English.

    public static final int MSG_ERROR = 111;

    //! Message to the client when the license status changed.
    //!
    //! Parameters:
    //! - Message.arg1: 1 = has license, 0 = no license.

    public static final int MSG_LICENSE_CHANGED = 112;

    //! Message to the client to notify of a new depth change.
    //!
    //! Parameters:
    //! - Bundle[KEY_DEPTH_CM]: double, imaging depth in centimeters.

    public static final int MSG_DEPTH_CHANGED = 113;

    //! Reply to queries MSG_GET_DEPTH.
    //!
    //! Parameters:
    //! - Bundle[KEY_DEPTH_CM]: double, imaging depth in centimeters.
    //! - Message.arg1: callback parameter copied from the Message.arg1 sent by the client.

    public static final int MSG_RETURN_DEPTH = 114;

    //! Message to the client to notify of a new gain change.
    //!
    //! Parameters:
    //! - Bundle[KEY_GAIN]: double, imaging gain.

    public static final int MSG_GAIN_CHANGED = 115;

    //! Reply to queries MSG_GET_GAIN.
    //!
    //! Parameters:
    //! - Bundle[KEY_GAIN]: double, imaging gain.
    //! - Message.arg1: callback parameter copied from the Message.arg1 sent by the client.

    public static final int MSG_RETURN_GAIN = 116;

    //! Reply to queries MSG_DOWNLOAD_RAW_DATA.
    //!
    //! Parameters on success and failure:
    //! - Message.arg1: callback parameter copied from the Message.arg1 sent by the client.
    //!
    //! Parameters on success:
    //! - Bundle[KEY_AVAILABLE]: boolean, indicate if requested raw data was available.
    //! - Bundle[KEY_PACKAGE_SIZE]: long, package size in bytes.
    //! - Bundle[KEY_PACKAGE_EXTENSION]: java.lang.String, package extension (indicates type, for example zip or tar).
    //!
    //! Parameters on failure:
    //! - Bundle[KEY_ERROR_MESSAGE]: java.lang.String, error details (in English).
    //!
    //! \version Added in version 8.0.1

    public static final int MSG_RETURN_RAW_DATA = 117;

    //! Reply to queries MSG_DOWNLOAD_RAW_DATA with the current download progress.
    //!
    //! Parameters:
    //! - Message.arg1: callback parameter copied from the Message.arg1 sent by the client.
    //! - Message.arg2: int, the raw data download progress in percent [0 100]
    //!
    //! \version Added in version 8.0.1

    public static final int MSG_RAW_DATA_DOWNLOAD_PROGRESS = 118;

    //! Message to the client on power event.
    //!
    //! Parameters:
    //! - Bundle[KEY_POWER_INFO]: me.clarius.mobileapi.PowerInfo, the power event info.
    //!
    //! Note: only power events associated with the probe initiating shut down will be sent
    //! For example, if the probe's battery gets too low, or the probe's button is used to shut down,
    //! the event will be sent, but if the user powers the probe down from Clarius App,
    //! the message is not implemented.
    //!
    //! \version Added in version 8.6.0

    public static final int MSG_POWER_EVENT = 119;

    //! Reply to queries MSG_GET_PATIENT_INFO.
    //!
    //! Parameters:
    //! - Bundle[KEY_PATIENT_INFO]: me.clarius.mobileapi.PatientInfo, the current patient info. Can be null if no probe is connected.
    //! - Message.arg1: callback parameter copied from the Message.arg1 sent by the client.
    //!
    //! \version Added in version 8.6.0

    public static final int MSG_RETURN_PATIENT_INFO = 120;

    //! Reply to queries MSG_GET_FREEZE.
    //!
    //! Parameters:
    //! - Bundle[KEY_FREEZE]: boolean, freeze state
    //! - Message.arg1: callback parameter copied from the Message.arg1 sent by the client.
    //!
    //! \version Added in version 9.0.0

    public static final int MSG_RETURN_FREEZE = 121;

    // Bundle keys

    public static final String KEY_IMAGE_SIZE = "size";
    public static final String KEY_COMPRESSION_TYPE = "compressionType";
    public static final String KEY_COMPRESSION_QUALITY = "compressionQuality";
    public static final String KEY_SEPARATE_OVERLAYS = "separateOverlays";
    public static final String KEY_IMAGE_DATA = "data";
    public static final String KEY_IMAGE_INFO = "info";
    public static final String KEY_POS_INFO = "pos";
    public static final String KEY_BUTTON_INFO = "info";
    public static final String KEY_POWER_INFO = "info";
    public static final String KEY_B_IMAGE_AREA = "bImageArea";
    public static final String KEY_PROBE_INFO = "probeInfo";
    public static final String KEY_PATIENT_INFO = "patientInfo";
    public static final String KEY_SETTINGS_INFO = "settingsInfo";
    public static final String KEY_COMPLETE_EXAM = "completeExam";
    public static final String KEY_USER_FN = "userFn";
    public static final String KEY_USER_PARAM = "userParam";
    public static final String KEY_PACKAGE_NAME = "packageName";
    public static final String KEY_FREEZE = "freeze";
    public static final String KEY_DEPTH_CM = "depthCm";
    public static final String KEY_GAIN = "gain";
    public static final String KEY_ERROR_MESSAGE = "errorMessage";
    public static final String KEY_START_FRAME = "startFrame";
    public static final String KEY_END_FRAME = "endFrame";
    public static final String KEY_WRITABLE_URI = "writableUri";
    public static final String KEY_PACKAGE_SIZE = "packageSize";
    public static final String KEY_PACKAGE_EXTENSION = "packageExtension";
    public static final String KEY_AVAILABLE = "available";

    // Predefined bundle values

    public static final String COMPRESSION_TYPE_JPEG = "jpeg";
    public static final String COMPRESSION_TYPE_PNG = "png";

    // Default values for optional bundle data

    public static final String DEFAULT_COMPRESSION_TYPE = "jpeg";
    public static final int DEFAULT_COMPRESSION_QUALITY = 80;
    public static final boolean DEFAULT_SEPARATE_OVERLAYS = false;
    public static final double DEFAULT_USER_KEY_PARAM = 0;
    public static final long DEFAULT_FRAME = 0;

    // Complete exam options
    public static final int COMPLETE_EXAM_FINISH = 0;   //! completes exam by storing all captured data
    public static final int COMPLETE_EXAM_DISCARD = 1;  //! completes exam by discarding any captured data
    public static final int COMPLETE_EXAM_SHELVE = 2;   //! shelves exam for later use

    // User functions

    public static final String USER_FN_TOGGLE_FREEZE       = "freeze";
    public static final String USER_FN_CAPTURE_IMAGE       = "capture image";
    public static final String USER_FN_CAPTURE_CINE        = "capture cine";
    public static final String USER_FN_DEPTH_DEC           = "decrease depth";
    public static final String USER_FN_DEPTH_INC           = "increase depth";
    public static final String USER_FN_GAIN_DEC            = "decrease gain";
    public static final String USER_FN_GAIN_INC            = "increase gain";
    public static final String USER_FN_TOGGLE_AUTOGAIN     = "toggle auto gain";
    public static final String USER_FN_TOGGLE_ZOOM         = "toggle zoom";
    public static final String USER_FN_TOGGLE_FLIP         = "toggle flip";
    public static final String USER_FN_TOGGLE_CINE_PLAY    = "play/stop cine";
    public static final String USER_FN_MODE_B              = "b mode";
    public static final String USER_FN_MODE_M              = "m mode";
    public static final String USER_FN_MODE_CFI            = "color doppler";
    public static final String USER_FN_MODE_PDI            = "power doppler";
    public static final String USER_FN_MODE_PW             = "pw doppler";
    public static final String USER_FN_MODE_NEEDLE         = "needle enhance";
    public static final String USER_FN_MODE_ELASTOGRAPHY   = "elastography";
    public static final String USER_FN_MODE_RF             = "rf mode";

    //! User function to set the depth, to be used with messages MSG_USER_FN.
    //!
    //! Parameters:
    //! - Bundle[KEY_USER_PARAM]: double, the target depth in centimeters.
    //!
    //! Note: the value will be clipped if it is out of range.
    public static final String USER_FN_SET_DEPTH           = "set depth";

    //! User function to set the gain, to be used with messages MSG_USER_FN.
    //!
    //! Parameters:
    //! - Bundle[KEY_USER_PARAM]: double, the target gain as documented below.
    //!
    //! The gain range depends if auto-gain is on or off:
    //! - auto gain off: the gain is the central TGC value in the range [-20 20]
    //! - auto gain on: the gain is the gamma value in the range [-0.5 0.5]
    public static final String USER_FN_SET_GAIN            = "set gain";
}
