package me.clarius.mobileapi;

import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;

//! Processed image information supplied with each frame.

public class ProcessedImageInfo implements Parcelable
{
    public int width;               //!< width of the image in pixels
    public int height;              //!< height of the image in pixels
    public int bitsPerPixel;        //!< bits per pixel of the image
    public double micronsPerPixel;  //!< microns per pixel (always 1:1 aspect ratio axially/laterally)
    public double originX;          ///< image origin in microns in the horizontal axis
    public double originY;          ///< image origin in microns in the vertical axis
    public long tm;                 //!< timestamp of image
    public boolean overlay;         //!< the image is an overlay

    //! Default constructor sets everything to zero.
    //! Note: required for JNI for Android 8 API 26.
    public ProcessedImageInfo()
    {
        width = 0;
        height = 0;
        bitsPerPixel = 0;
        micronsPerPixel = 0;
        originX = 0;
        originY = 0;
        tm = 0;
        overlay = false;
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
        // Added in version 8.1.0:
        if (Build.VERSION.SDK_INT >= 29) {
            overlay = in.readBoolean(); // boolean introduced in API 29
        } else {
            overlay = in.readInt() != 0;
        }
        originX = in.readDouble();
        originY = in.readDouble();
        // ## Add new members last to retain back compatibility ##
    }

    @Override
    public void writeToParcel(Parcel out, int flags)
    {
        out.writeInt(width);
        out.writeInt(height);
        out.writeInt(bitsPerPixel);
        out.writeDouble(micronsPerPixel);
        out.writeLong(tm);
        // Added in version 8.1.0:
        if (Build.VERSION.SDK_INT >= 29) {
            out.writeBoolean(overlay); // boolean introduced in API 29
        } else {
            out.writeInt(overlay ? 1 : 0);
        }
        out.writeDouble(originX);
        out.writeDouble(originY);
        // ## Add new members last to retain back compatibility ##
    }

    @Override
    public int describeContents()
    {
        return 0;
    }
}
