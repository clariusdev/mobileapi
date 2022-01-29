package me.clarius.mobileapi;

import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;

//! Information about the Clarius App settings

public class SettingsInfo implements Parcelable
{
    public static final int BUTTON_DISABLED = 0;    //!< disable button
    public static final int BUTTON_FREEZE = 1;      //!< button will perform on-probe freeze
    public static final int BUTTON_USER = 2;        //!< button will initiate event through api

    public boolean autoConnect;     //!< automatically connect to the last used probe when Clarius App is launched
    public boolean autoScan;        //!< start on the scanning screen when the Clarius App is launched
    public int cineLength;          //!< cine capture length in seconds (valid values of 1 - 30)
    public int autoFreeze;          //!< # of seconds to freeze after no contact mode is initiated
    public int keepAwake;           //!< # of minutes to keep the scanner powered after not imaging
    public int buttonUp;            //!< button up function (see above)
    public int buttonDown;          //!< button down function (see above)

    //! Default constructor sets everything to zero.
    //! Note: required for JNI for Android 8 API 26.
    public SettingsInfo()
    {
        autoConnect = true;
        autoScan = true;
        cineLength = 3;
        autoFreeze = 30;
        keepAwake = 15;
        buttonUp = BUTTON_USER;
        buttonDown = BUTTON_FREEZE;
    }

    // Parcelable interface
    public static final Parcelable.Creator<SettingsInfo> CREATOR = new Parcelable.Creator<SettingsInfo>()
    {
        public SettingsInfo createFromParcel(Parcel in)
        {
            return new SettingsInfo(in);
        }
        public SettingsInfo[] newArray(int size)
        {
            return new SettingsInfo[size];
        }
    };

    private SettingsInfo(Parcel in)
    {
        if (Build.VERSION.SDK_INT >= 29) {
            autoConnect = in.readBoolean(); // boolean introduced in API 29
            autoScan = in.readBoolean();
        } else {
            autoConnect = in.readInt() != 0;
            autoScan = in.readInt() != 0;
        }

        cineLength = in.readInt();
        autoFreeze = in.readInt();
        keepAwake = in.readInt();
        buttonUp = in.readInt();
        buttonDown = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel out, int flags)
    {
        if (Build.VERSION.SDK_INT >= 29) {
            out.writeBoolean(autoConnect); // boolean introduced in API 29
            out.writeBoolean(autoScan);
        } else {
            out.writeInt(autoConnect ? 1 : 0);
            out.writeInt(autoScan ? 1 : 0);
        }
        out.writeInt(cineLength);
        out.writeInt(autoFreeze);
        out.writeInt(keepAwake);
        out.writeInt(buttonUp);
        out.writeInt(buttonDown);
    }

    @Override
    public int describeContents()
    {
        return 0;
    }
}
