Changelog
=========

# 10.1.0

Removed:
- `MSG_3P_PACKAGE`: replaced by Clarius Marketplace launcher.
- `KEY_PACKAGE_NAME`: replaced by Clarius Marketplace launcher.
- Messages that can set the app state: `MSG_SET_PATIENT_INFO`, `MSG_SET_SETTINGS_INFO`, `MSG_COMPLETE_EXAM` and `KEY_COMPLETE_EXAM`.

# 9.4.0

Changed:
- The Mobile API is now distributed as an Android Package in the GitHub Gradle registry.
- Renamed package from `me.clarius.mobileapi` to `me.clarius.sdk.mobileapi`.

# 9.3.0

Fixed:
- Preventing crash when sending message `MSG_SET_SETTINGS_INFO`.

# 9.2.0

Added:
- `MSG_RAW_DATA_AVAILABLE`: signal that new data is available in the Clarius App.
- `MSG_COPY_RAW_DATA`: request a copy of a raw data archive from the Clarius App.
- `MSG_RAW_DATA_COPIED`: raw data copy completion.

Changed:
- Modified how raw data is obtained: it must be obtained by doing a capture instead of requesting a download.

Removed:
- Raw data download messages `MSG_DOWNLOAD_RAW_DATA`, `MSG_RETURN_RAW_DATA`, `MSG_RAW_DATA_DOWNLOAD_PROGRESS`.

# 9.0.0

Added:
- `MSG_GET_FREEZE`: query freeze state without having to get notified.
- `MSG_SET_SETTINGS_INFO`: set probe settings through the api.
- `MSG_COMPLETE_EXAM`: complete exam via store, discard, or shelve.

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
