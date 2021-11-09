package me.clarius.mobileapi;

import android.os.Parcel;
import android.os.Parcelable;

//! Information about the current patient demographics

public class PatientInfo implements Parcelable
{
    public String id;       //!< patient id
    public String name;     //!< patient name, csv format of: [lastName, firstName]

    //! Default constructor sets everything to zero.
    //! Note: required for JNI for Android 8 API 26.
    public PatientInfo()
    {
        id = "";
        name = "";
    }

    // Parcelable interface

    public static final Parcelable.Creator<PatientInfo> CREATOR = new Parcelable.Creator<PatientInfo>()
    {
        public PatientInfo createFromParcel(Parcel in)
        {
            return new PatientInfo(in);
        }
        public PatientInfo[] newArray(int size)
        {
            return new PatientInfo[size];
        }
    };

    private PatientInfo(Parcel in)
    {
        id = in.readString();
        name  = in.readString();
    }

    @Override
    public void writeToParcel(Parcel out, int flags)
    {
        out.writeString(id);
        out.writeString(name);
    }

    @Override
    public int describeContents()
    {
        return 0;
    }
}
