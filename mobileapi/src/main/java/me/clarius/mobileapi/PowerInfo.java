package me.clarius.mobileapi;

import android.os.Parcel;
import android.os.Parcelable;

//! Information about a power event.

public class PowerInfo implements Parcelable
{
    public static final int POWER_OFF_IDLE = 1;         //!< probe idle threshold triggered power off
    public static final int POWER_OFF_BATTERY = 2;      //!< low battery triggered power off
    public static final int POWER_OFF_TEMPERATURE = 3;  //!< high temperature triggered power off
    public static final int POWER_OFF_BUTTON = 4;       //!< probe button press triggered power off

    public int type;    //!< power off type (see above)
    public int time;    //!< used when idle or temperature type sent for the first time
                        //!< to denote number of seconds that the probe will power off

    //! Default constructor sets everything to zero.
    //! Note: required for JNI for Android 8 API 26.
    public PowerInfo()
    {
        type = 0;
        time = 0;
    }

    // Parcelable interface
    public static final Parcelable.Creator<PowerInfo> CREATOR = new Parcelable.Creator<PowerInfo>()
    {
        public PowerInfo createFromParcel(Parcel in)
        {
            return new PowerInfo(in);
        }
        public PowerInfo[] newArray(int size)
        {
            return new PowerInfo[size];
        }
    };

    private PowerInfo(Parcel in)
    {
        type = in.readInt();
        time = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel out, int flags)
    {
        out.writeInt(type);
        out.writeInt(time);
    }

    @Override
    public int describeContents()
    {
        return 0;
    }
}
