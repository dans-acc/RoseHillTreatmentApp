package uk.co.rosehilltimber.rosehilltreatmentapp.treatment;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import uk.co.rosehilltimber.rosehilltreatmentapp.treatment.packs.TimberPack;
import uk.co.rosehilltimber.rosehilltreatmentapp.treatment.packs.TimberPackType;
import uk.co.rosehilltimber.rosehilltreatmentapp.utils.DateUtility;

public class Treat implements Parcelable
{

    public static final Parcelable.Creator<Treat> CREATOR = new Parcelable.Creator<Treat>() {
        @Override
        public Treat createFromParcel(@NonNull final Parcel parcel) {
            return new Treat(parcel);
        }
        @NonNull
        @Override
        public Treat[] newArray(final int length) {
            return new Treat[length];
        }
    };

    private static final int INIT_UNDEFINED_WEEK_NUMBER = -1;

    private UUID mUUID;
    private int mNumber;

    private LocalDate mWeekDate;

    private TreatType mType;
    private List<TimberPack> mTimberPacks;

    public Treat(final UUID mUUID,
                 final int mNumber,
                 final LocalDate mWeekDate,
                 final TreatType mType,
                 final List<TimberPack> mTimberPacks)
    {
        this.mUUID = mUUID;
        this.mNumber = mNumber;

        this.mWeekDate = mWeekDate;

        this.mType = mType;
        this.mTimberPacks = mTimberPacks;
    }

    public Treat(final UUID mUUID,
                 final int mNumber,
                 final LocalDate mWeekDate,
                 final TreatType mType)
    {
        this(
                mUUID,
                mNumber,
                mWeekDate,
                mType,
                new ArrayList<>()
        );
    }

    public Treat(final LocalDate mWeekDate,
                 final TreatType mType)
    {
        this(
                UUID.randomUUID(),
                INIT_UNDEFINED_WEEK_NUMBER,
                mWeekDate,
                mType,
                new ArrayList<>()
        );
    }

    private Treat(final Parcel parcel)
    {
        this(
                UUID.fromString(parcel.readString()),
                parcel.readInt(),
                DateUtility.fromEpochMilli(parcel.readLong()),
                TreatType.values()[parcel.readInt()]
        );

        // Read the list of timber packs into the mTimberPacks list instance.
        parcel.readList(mTimberPacks, TimberPack.class.getClassLoader());
    }

    public UUID getUUID()
    {
        return mUUID;
    }

    public void setUUID(final UUID mUUID)
    {
        this.mUUID = mUUID;
    }

    public LocalDate getWeekDate()
    {
        return mWeekDate;
    }

    public void setDate(@NonNull final LocalDate mDate)
    {
        this.mWeekDate = mDate;
    }

    public int getNumber()
    {
        return mNumber;
    }

    public void setNumber(final int mNumber)
    {
        this.mNumber = mNumber;
    }

    public TreatType getType()
    {
        return mType;
    }

    @NonNull
    public List<TimberPack> getTimberPacks()
    {
        return mTimberPacks;
    }

    public double getTotalTreatVolume()
    {
        // Compute the total treat volume.
        double volume = 0;
        for (final TimberPack timberPack : mTimberPacks) {
            volume += timberPack.getPackVolume();
        }
        return volume;
    }

    public double getTotalTreatTypeVolume(@NonNull final TimberPackType timberPackType)
    {
        // Compute the total treat type volume for a given pack type.
        double volume = 0;
        for (final TimberPack timberPack : mTimberPacks) {
            if (timberPack.getClass().equals(timberPackType.getClassType())) {
                volume += timberPack.getPackVolume();
            }
        }
        return volume;
    }

    @Override
    public int describeContents()
    {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull final Parcel parcel, final int flags)
    {
        // Write the parcel data based on the current values.
        parcel.writeString(mUUID.toString());
        parcel.writeInt(mNumber);
        parcel.writeLong(DateUtility.toEpochMilli(mWeekDate));
        parcel.writeInt(mType.ordinal());
        parcel.writeList(mTimberPacks);
    }
}
