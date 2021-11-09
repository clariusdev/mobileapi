Changelog
=========

# 8.0.1

Changed:
- Renamed `KEY_MESSAGE` to `KEY_ERROR_MESSAGE`.
- Scan convert info is now transmitted with each image.
- Sending more probe information: model, serial number, battery, temperature.
- Allowing streaming from Virtual Scanner without license, useful for testing.

Added:
- Added RF data download with messages `MSG_DOWNLOAD_RAW_DATA` and `MSG_RETURN_RAW_DATA`.
- Added an option to obtain overlays (like Doppler) in a separate image with `KEY_SEPARATE_OVERLAYS`.

Removed:
- `MSG_GET_SCAN_CONVERT`
- `MSG_SCAN_CONVERT_CHANGED`
- `MSG_RETURN_SCAN_CONVERT`
- `KEY_ORIGIN_MICRONS`
- `KEY_PIXEL_SIZE_MICRONS`


# 8.3.0

Added:
- `MSG_POWER_EVENT`: power events messages from server to client.
- Query patient demographics with `MSG_GET_PATIENT_INFO` and `MSG_RETURN_PATIENT_INFO`.

Fixed:
- Streaming images with the correct orientation.
- Preventing crash when binding to Clarius app when it is not running.


# 8.5.0

Added:
- `MSG_3P_PACKAGE`: set Partner App launcher from the Clarius App.

Fixed:
- Checking mandatory parameters for registration and unregistration messages and logging an error if missing.


# 8.6.0

Added:
- `MSG_SET_PATIENT_INFO`: set patient demographics in Clarius App.

Changed:
- Freeze value returned in bundle data instead of `Message.arg1` in `MSG_FREEZE_CHANGED`.
