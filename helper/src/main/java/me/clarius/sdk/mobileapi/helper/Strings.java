package me.clarius.sdk.mobileapi.helper;

import me.clarius.sdk.mobileapi.ButtonInfo;
import me.clarius.sdk.mobileapi.PatientInfo;
import me.clarius.sdk.mobileapi.PowerInfo;
import me.clarius.sdk.mobileapi.ProbeInfo;

/**
 * Helpers to convert structs to strings.
 */

public class Strings {
    private static String buttonName(int buttonId) {
        switch (buttonId) {
            case ButtonInfo.BUTTON_UP:
                return "up";
            case ButtonInfo.BUTTON_DOWN:
                return "down";
        }
        return "???";
    }

    public static String toString(ButtonInfo info) {
        return "Button: " + buttonName(info.id) + " x" + info.clicks + " long? " + info.longPress;
    }

    public static String toString(ProbeInfo probeInfo) {
        StringBuilder builder = new StringBuilder();
        builder.append("Probe info: ");
        if (null != probeInfo) {
            builder.append("v").append(probeInfo.version).append(", elements: ").append(probeInfo.elements).append(", pitch: ").append(probeInfo.pitch).append(" radius: ").append(probeInfo.radius);
        } else {
            builder.append("not connected");
        }
        return builder.toString();
    }

    public static String toString(PatientInfo patientInfo) {
        return "Patient info: " + patientInfo.id + " - " + patientInfo.name;
    }

    private static String powerEventName(int type) {
        switch (type) {
            case PowerInfo.POWER_OFF_IDLE:
                return "Idle";
            case PowerInfo.POWER_OFF_BATTERY:
                return "Battery";
            case PowerInfo.POWER_OFF_TEMPERATURE:
                return "Temperature";
            case PowerInfo.POWER_OFF_BUTTON:
                return "Button";
        }
        return "???";
    }

    public static String toString(PowerInfo powerInfo) {
        return "Power event: " + powerEventName(powerInfo.type);
    }
}
