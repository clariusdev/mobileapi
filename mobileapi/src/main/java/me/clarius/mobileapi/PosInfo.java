package me.clarius.mobileapi;

import android.os.Parcel;
import android.os.Parcelable;

//! Positional data information structure.

public class PosInfo implements Parcelable
{
    public long tm;         //!< timestamp in nanoseconds
    public double gx;       //!< gyroscope x; angular velocity is given in radians per second (rps)
    public double gy;       //!< gyroscope y
    public double gz;       //!< gyroscope z
    public double ax;       //!< accelerometer x; acceleration is normalized to gravity [~9.81m/s^2] (m/s^2)/(m/s^2)
    public double ay;       //!< accelerometer y
    public double az;       //!< accelerometer z
    public double mx;       //!< magnetometer x; magnetic flux density is normalized to the earth's field [~50 mT] (T/T)
    public double my;       //!< magnetometer y
    public double mz;       //!< magnetometer z
    public double qw;       //!< w component (real) of the orientation quaternion
    public double qx;       //!< x component (imaginary) of the orientation quaternion
    public double qy;       //!< y component (imaginary) of the orientation quaternion
    public double qz;       //!< z component (imaginary) of the orientation quaternion

    //! Default constructor sets everything to zero.
    //! Note: required for JNI for Android 8 API 26.
    public PosInfo()
    {
        long tm = 0;
        double gx = 0;
        double gy = 0;
        double gz = 0;
        double ax = 0;
        double ay = 0;
        double az = 0;
        double mx = 0;
        double my = 0;
        double mz = 0;
        double qw = 0;
        double qx = 0;
        double qy = 0;
        double qz = 0;
    }

    // Parcelable interface

    public static final Parcelable.Creator<PosInfo> CREATOR = new Parcelable.Creator<PosInfo>()
    {
        public PosInfo createFromParcel(Parcel in)
        {
            return new PosInfo(in);
        }
        public PosInfo[] newArray(int size)
        {
            return new PosInfo[size];
        }
    };

    private PosInfo(Parcel in)
    {
        tm = in.readLong();
        gx = in.readDouble();
        gy = in.readDouble();
        gz = in.readDouble();
        ax = in.readDouble();
        ay = in.readDouble();
        az = in.readDouble();
        mx = in.readDouble();
        my = in.readDouble();
        mz = in.readDouble();
        qw = in.readDouble();
        qx = in.readDouble();
        qy = in.readDouble();
        qz = in.readDouble();
    }

    @Override
    public void writeToParcel(Parcel out, int flags)
    {
        out.writeLong(tm);
        out.writeDouble(gx);
        out.writeDouble(gy);
        out.writeDouble(gz);
        out.writeDouble(ax);
        out.writeDouble(ay);
        out.writeDouble(az);
        out.writeDouble(mx);
        out.writeDouble(my);
        out.writeDouble(mz);
        out.writeDouble(qw);
        out.writeDouble(qx);
        out.writeDouble(qy);
        out.writeDouble(qz);
    }

    @Override
    public int describeContents()
    {
        return 0;
    }
}
