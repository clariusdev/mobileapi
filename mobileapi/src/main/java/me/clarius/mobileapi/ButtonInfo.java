package me.clarius.mobileapi;

import android.os.Parcel;
import android.os.Parcelable;

//! Information about a button event.

public class ButtonInfo implements Parcelable
{

    public static final int BUTTON_UP = 1;      //!< probe's up button identifier
    public static final int BUTTON_DOWN= 2;     //!< probe's bottom button identifier

    public int id;                              //!< button identifier
    public int clicks;                          //!< number of clicks
    public int longPress;                       //!< 0 = short click, 1 = long press

    //! Default constructor sets everything to zero.
    //! Note: required for JNI for Android 8 API 26.
    public ButtonInfo()
    {
        id = 0;
        clicks = 0;
        longPress = 0;
    }

    // Parcelable interface

    public static final Parcelable.Creator<ButtonInfo> CREATOR = new Parcelable.Creator<ButtonInfo>()
    {
        public ButtonInfo createFromParcel(Parcel in)
        {
            return new ButtonInfo(in);
        }
        public ButtonInfo[] newArray(int size)
        {
            return new ButtonInfo[size];
        }
    };

    private ButtonInfo(Parcel in)
    {
        id = in.readInt();
        clicks = in.readInt();
        longPress = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel out, int flags)
    {
        out.writeInt(id);
        out.writeInt(clicks);
        out.writeInt(longPress);
    }

    @Override
    public int describeContents()
    {
        return 0;
    }
}
