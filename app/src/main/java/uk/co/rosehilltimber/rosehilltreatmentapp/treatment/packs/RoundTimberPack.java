package uk.co.rosehilltimber.rosehilltreatmentapp.treatment.packs;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import java.util.UUID;

public class RoundTimberPack extends TimberPack
{

    public static final Parcelable.Creator<RoundTimberPack> CREATOR = new Parcelable.Creator<RoundTimberPack>() {
        @Override
        public RoundTimberPack createFromParcel(@NonNull final Parcel parcel) {
            return new RoundTimberPack(parcel);
        }
        @NonNull
        @Override
        public RoundTimberPack[] newArray(final int length) {
            return new RoundTimberPack[length];
        }
    };

    private double mLengthM;
    private double mRadiusM;

    public RoundTimberPack(final UUID mUUID,
                           final int mQuantity,
                           final double mLengthM,
                           final double mRadiusM)
    {
        super(
                mUUID,
                TimberPackType.ROUND,
                mQuantity
        );

        this.mLengthM = mLengthM;
        this.mRadiusM = mRadiusM;
    }

    public RoundTimberPack(final int mQuantity,
                           final double mLengthM,
                           final double mRadiusM)
    {
        this(
                UUID.randomUUID(),
                mQuantity,
                mLengthM,
                mRadiusM
        );
    }

    private RoundTimberPack(final Parcel parcel)
    {
        super(parcel);

        mLengthM = parcel.readDouble();
        mRadiusM = parcel.readDouble();
    }

    public double getLengthM()
    {
        return mLengthM;
    }

    public void setLengthM(final double mLengthM)
    {
        this.mLengthM = mLengthM;
    }

    public double getRadiusM()
    {
        return mRadiusM;
    }

    public void setRadiusM(final double mRadiusM)
    {
        this.mRadiusM = mRadiusM;
    }

    @Override
    public double getPackVolume()
    {
        return Math.PI * (mRadiusM * mRadiusM) * mLengthM * super.getQuantity();
    }

    @Override
    public void writeToParcel(@NonNull final Parcel parcel, final int flags)
    {
        // Writes the quantity of the pack.
        super.writeToParcel(parcel, flags);

        // Write the dimensions of the pack.
        parcel.writeDouble(mLengthM);
        parcel.writeDouble(mRadiusM);
    }
}
