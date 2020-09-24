package me.clarius.mobileapi;

import android.os.Parcel;
import android.os.Parcelable;

//! Information about the currently connected probe.

public class ProbeInfo implements Parcelable
{

    public int version;             //!< version (1 = Clarius 1st Generation, 2 = Clarius HD)
    public int elements;            //!< number of probe elements
    public int pitch;               //!< element pitch
    public int radius;              //!< radius in mm

    //! Default constructor sets everything to zero.
    //! Note: required for JNI for Android 8 API 26.
    public ProbeInfo()
    {
        version = 0;
        elements = 0;
        pitch = 0;
        radius = 0;
    }

    // Parcelable interface

    public static final Parcelable.Creator<ProbeInfo> CREATOR = new Parcelable.Creator<ProbeInfo>()
    {
        public ProbeInfo createFromParcel(Parcel in)
        {
            return new ProbeInfo(in);
        }
        public ProbeInfo[] newArray(int size)
        {
            return new ProbeInfo[size];
        }
    };

    private ProbeInfo(Parcel in)
    {
        version = in.readInt();
        elements = in.readInt();
        pitch = in.readInt();
        radius = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel out, int flags)
    {
        out.writeInt(version);
        out.writeInt(elements);
        out.writeInt(pitch);
        out.writeInt(radius);
    }

    @Override
    public int describeContents()
    {
        return 0;
    }
}
