package uk.co.rosehilltimber.rosehilltreatmentapp.treatment;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import uk.co.rosehilltimber.rosehilltreatmentapp.utils.DateUtility;

public class Week implements Parcelable, Comparable<Week>
{

    // Serialise the week instance, permitting to to be passed via a bundle.
    public static final Parcelable.Creator<Week> CREATOR = new Parcelable.Creator<Week>() {
        @Override
        public Week createFromParcel(@NonNull final Parcel parcel) {
            return new Week(parcel);
        }
        @NonNull
        @Override
        public Week[] newArray(final int length) {
            return new Week[length];
        }
    };

    private final LocalDate mDate;
    private final List<Treat> mTreats;

    public Week(final LocalDate mDate,
                final List<Treat> mTreats)
    {
        this.mDate = mDate;
        this.mTreats = mTreats;
    }

    public Week(final LocalDate mDate)
    {
        this(mDate, new ArrayList<Treat>());
    }

    private Week(final Parcel parcel)
    {
        this(DateUtility.fromEpochMilli(parcel.readLong()));
        parcel.readList(mTreats, Treat.class.getClassLoader());
    }

    @NonNull
    public LocalDate getDate()
    {
        return mDate;
    }

    @NonNull
    public List<Treat> getTreatments()
    {
        return mTreats;
    }

    public double getTotalWeekVolume()
    {
        double volume = 0;
        for (final Treat treat : mTreats) {
            volume += treat.getTotalTreatVolume();
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
        parcel.writeLong(DateUtility.toEpochMilli(mDate));
        parcel.writeList(mTreats);
    }

    @Override
    public int compareTo(@NonNull final Week week)
    {
        return mDate.compareTo(week.getDate());
    }
}
