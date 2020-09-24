package me.clarius.mobileapi;

import android.os.Parcel;
import android.os.Parcelable;

//! Processed image information supplied with each frame.

public class ProcessedImageInfo implements Parcelable
{
    public int width;               //!< width of the image in pixels
    public int height;              //!< height of the image in pixels
    public int bitsPerPixel;        //!< bits per pixel of the image
    public double micronsPerPixel;  //!< microns per pixel (always 1:1 aspect ratio axially/laterally)
    public long tm;                 //!< timestamp of image

    //! Default constructor sets everything to zero.
    //! Note: required for JNI for Android 8 API 26.
    public ProcessedImageInfo()
    {
        width = 0;
        height = 0;
        bitsPerPixel = 0;
        micronsPerPixel = 0;
        tm = 0;
    }

    // Parcelable interface

    public static final Parcelable.Creator<ProcessedImageInfo> CREATOR = new Parcelable.Creator<ProcessedImageInfo>()
    {
        public ProcessedImageInfo createFromParcel(Parcel in)
        {
            return new ProcessedImageInfo(in);
        }
        public ProcessedImageInfo[] newArray(int size)
        {
            return new ProcessedImageInfo[size];
        }
    };

    private ProcessedImageInfo(Parcel in)
    {
        width = in.readInt();
        height = in.readInt();
        bitsPerPixel = in.readInt();
        micronsPerPixel = in.readDouble();
        tm = in.readLong();
    }

    @Override
    public void writeToParcel(Parcel out, int flags)
    {
        out.writeInt(width);
        out.writeInt(height);
        out.writeInt(bitsPerPixel);
        out.writeDouble(micronsPerPixel);
        out.writeLong(tm);
    }

    @Override
    public int describeContents()
    {
        return 0;
    }
}
