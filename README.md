Clarius Mobile API
==================

The mobile API allows third party mobile applications to obtain the B-mode images produced by a Clarius Ultrasound Scanner in real time.
It also supports sending user commands such as changing the imaging depth or taking a capture.

# Prerequisites

- [Clarius Ultrasound Scanner](https://clarius.com/)
- [Clarius App for Android](https://play.google.com/store/apps/details?id=me.clarius.clarius)
- The Clarius 3rd party mobile license (contact [Clarius](https://clarius.com/ca/contact/) to obtain it)

# Architecture

The Mobile API communicates with the _Clarius App_ rather than directly with the scanner.
Therefore, the Clarius App must be installed and running on the same Android device.
The Clarius App takes care of connecting to the probe and pre-processing the images before serving them with the Mobile API.

                            +-----------------------+
                            |    Android Device     |
                            |                       |
    +---------+             |    +-------------+    |
    |         |  U/S images |    |             |    |
    |  Probe  +----------------->+ Clarius App |    |
    |         |             |    |             |    |
    +---------+             |    +-------------+    |
                            |    | Mobile API  |    |
                            |    +----+---+----+    |
                            |         |   ^         |
                            | B-images|   |Commands |
                            |         v   |         |
                            |    +----+---+----+    |
                            |    | Third Party |    |
                            |    |     App     |    |
                            |    +-------------+    |
                            |                       |
                            +-----------------------+

# API Description

On Android, the Mobile API is implemented as an Android _Bound Service_ running in the Clarius App itself.
Refer to the Android developer guide for details about bound services.
https://developer.android.com/guide/components/bound-services

The interface to the bound service is provided by an Android _Messenger_.
Android Messengers allow interprocess communications (IPC) by exchanging `Message` objects containing an action code and a payload.
The sequence of messages and their content constitute the communication protocol which is described below.

# Protocol

1. The client binds to the service by calling `Context.bindService()` and receives the server's `IBinder`. This `IBinder` is used to create a Messenger that can send messages _to the server_.
2. The client sends its own Messenger to the server in the `replyTo` field of a `Message` object with code `MSG_REGISTER_CLIENT`. This Messenger is used by the server to send messages _to the client_.
3. The client sends the image configuration to the server (`MSG_CONFIGURE_IMAGE`).
4. During operation, the server will send the image data to the client (`MSG_NEW_PROCESSED_IMAGE`) and other notable events such as button presses, freeze state, etc.
5. The client can request the execution of predefined functions such as "increase depth" or "capture image" (`MSG_USER_FN`).

All messages and their associated payload are described in the [MobileApi.java](mobileapi/src/main/java/MobileApi.java) file.

# Licensing

The service operates only when the Clarius App is connected to a scanner with the appropriate 3rd party application license.

However, the service accepts binding requests from clients even when no proper license is active to accommodate workflows where the license is removed for legitimate reasons, for example when a probe is disconnected to save battery. In that case, the service enters a restricted mode where it stops handling requests and sending update, but will resume normal operation as soon as a licensed scanner is connected again.

The license check workflow is as follows:

1. The client binds to the Listen Service.
2. The service starts and accepts the bind request, regardless of the license status.
3. Depending on the current license status:
    - if active, the service operates normally: all client requests are handled and all updates are sent to the clients;
    - if inactive, the service enters restricted mode: no update is sent (except `MSG_LICENSE_UPDATE`) and no client request is handled (except `MSG_REGISTER_CLIENT` and `MSG_UNREGISTER_CLIENT`). The service will reply `MSG_NO_LICENSE` to any other request.
4. If the license status changes during operation, the service sends `MSG_LICENSE_UPDATE` and changes its mode of operation:
    - if the license becomes inactive: the service clears the image configuration and enter restricted mode;
    - if the license becomes active: the service resumes normal operations, but the client must re-send the image configuration.

# Example App

The Android app provided in this repository demonstrates all features of the Mobile API.
It uses Gradle as its build tool so it can be opened directly in Android Studio.
