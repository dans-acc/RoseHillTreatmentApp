package uk.co.rosehilltimber.rosehilltreatmentapp.treatment.packs;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import java.util.UUID;

public class CuboidTimberPack extends TimberPack
{

    // Used for 'serialising' the object in order to pass it to the next fragment / activity.
    public static final Parcelable.Creator<CuboidTimberPack> CREATOR = new Parcelable.Creator<CuboidTimberPack>() {
        @Override
        public CuboidTimberPack createFromParcel(@NonNull final Parcel parcel) {
            return new CuboidTimberPack(parcel);
        }
        @NonNull
        @Override
        public CuboidTimberPack[] newArray(final int length) {
            return new CuboidTimberPack[length];
        }
    };

    private double mLengthM;
    private int mBreadthMM;
    private int mHeightMM;

    public CuboidTimberPack(final UUID mUUID,
                            final int mQuantity,
                            final double mLengthM,
                            final int mBreadthMM,
                            final int mHeightMM)
    {
        super(
                mUUID,
                TimberPackType.CUBOID,
                mQuantity
        );

        this.mLengthM = mLengthM;
        this.mBreadthMM = mBreadthMM;
        this.mHeightMM = mHeightMM;
    }

    public CuboidTimberPack(final int mQuantity,
                            final double mLengthM,
                            final int mBreadthMM,
                            final int mHeightMM)
    {
        this(
                UUID.randomUUID(),
                mQuantity,
                mLengthM,
                mBreadthMM,
                mHeightMM
        );
    }

    private CuboidTimberPack(final Parcel parcel)
    {
        super(parcel);

        mLengthM = parcel.readDouble();
        mBreadthMM = parcel.readInt();
        mHeightMM = parcel.readInt();
    }

    public double getLengthM()
    {
        return mLengthM;
    }

    public void setLengthM(final double mLengthM)
    {
        this.mLengthM = mLengthM;
    }

    public int getBreadthMM()
    {
        return mBreadthMM;
    }

    public void setBreadthMM(final int mBreadthMM)
    {
        this.mBreadthMM = mBreadthMM;
    }

    public int getHeightMM()
    {
        return mHeightMM;
    }

    public void setHeightMM(final int mHeightMM)
    {
        this.mHeightMM = mHeightMM;
    }

    @Override
    public double getPackVolume()
    {
        return mLengthM * mBreadthMM * mHeightMM / 1000 / 1000 * super.getQuantity();
    }

    @Override
    public void writeToParcel(@NonNull final Parcel parcel, final int flags)
    {
        // Write the quantity of the pack.
        super.writeToParcel(parcel, flags);

        // Write the dimensions of the round pack.
        parcel.writeDouble(mLengthM);
        parcel.writeInt(mBreadthMM);
        parcel.writeInt(mHeightMM);
    }
}
