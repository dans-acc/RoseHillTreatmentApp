package uk.co.rosehilltimber.rosehilltreatmentapp.treatment.packs;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import java.util.UUID;

public abstract class TimberPack implements Parcelable
{

    private UUID mUUID;
    private TimberPackType mTimberPackType;

    private int mQuantity;

    protected TimberPack(final UUID mUUID,
                         final TimberPackType mTimberPackType,
                         final int mQuantity)
    {
        this.mUUID = mUUID;
        this.mTimberPackType = mTimberPackType;
        this.mQuantity = mQuantity;
    }

    protected TimberPack(final Parcel parcel)
    {
        mUUID = UUID.fromString(parcel.readString());
        mTimberPackType = TimberPackType.values()[parcel.readInt()];
        mQuantity = parcel.readInt();
    }

    public UUID getUUID()
    {
        return mUUID;
    }

    public void setUUID(final UUID mUUID)
    {
        this.mUUID = mUUID;
    }

    public TimberPackType getTimberPackType()
    {
        return mTimberPackType;
    }

    public void setTimberPackType(final TimberPackType mTimberPackType)
    {
        this.mTimberPackType = mTimberPackType;
    }

    public int getQuantity()
    {
        return mQuantity;
    }

    public void setQuantity(final int mQuantity)
    {
        this.mQuantity = mQuantity;
    }

    @Override
    public int describeContents()
    {
        return 0;
    }

    public abstract double getPackVolume();

    @Override
    public void writeToParcel(@NonNull final Parcel parcel, final int flags)
    {
        parcel.writeString(mUUID.toString());
        parcel.writeInt(mTimberPackType.ordinal());
        parcel.writeInt(mQuantity);
    }
}
