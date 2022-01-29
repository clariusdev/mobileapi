Changelog
=========

# 9.0.0

Added:
- `MSG_GET_FREEZE`: query freeze state without having to get notified.
- `MSG_SET_SETTINGS_INFO`: set probe settings through the api
- `MSG_COMPLETE_EXAM`: complete exam via store, discard, or shelve

# 8.6.0

Added:
- `MSG_SET_PATIENT_INFO`: set patient demographics in Clarius App.
- `MSG_3P_PACKAGE`: set Partner App launcher from the Clarius App.
- `MSG_POWER_EVENT`: power events messages from server to client.
- Query patient demographics with `MSG_GET_PATIENT_INFO` and `MSG_RETURN_PATIENT_INFO`.
 
Changed:
- Freeze value returned in bundle data instead of `Message.arg1` in `MSG_FREEZE_CHANGED`.

Fixed:
- Checking mandatory parameters for registration and unregistration messages and logging an error if missing.
- Streaming images with the correct orientation.
- Preventing crash when binding to Clarius app when it is not running.

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

# 7.3.0

- Initial release
