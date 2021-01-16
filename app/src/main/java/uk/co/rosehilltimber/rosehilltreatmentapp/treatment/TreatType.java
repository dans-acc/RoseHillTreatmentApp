package uk.co.rosehilltimber.rosehilltreatmentapp.treatment;

import android.support.annotation.NonNull;

import uk.co.rosehilltimber.rosehilltreatmentapp.treatment.packs.TimberPackType;

public enum TreatType
{

    // The different types of treatment combinations available.
    GREEN("Green", TimberPackType.CUBOID),
    ROUND_GREEN("Round Green", TimberPackType.CUBOID, TimberPackType.ROUND),
    BROWN("Brown", TimberPackType.CUBOID);

    private final String mName;
    private final TimberPackType[] mTimberPackTypes;

    TreatType(@NonNull final String mName, @NonNull final TimberPackType... timberPackTypes)
    {
        this.mName = mName;
        this.mTimberPackTypes = timberPackTypes;
    }

    @NonNull
    public final String getName()
    {
        return mName;
    }

    @NonNull
    public final TimberPackType[] getTimberPackTypes()
    {
        return mTimberPackTypes;
    }

    @NonNull
    public final CharSequence[] getTimberPackTypeNames()
    {
        final CharSequence[] treatTypeNames = new CharSequence[mTimberPackTypes.length];
        for (int i = 0; i < treatTypeNames.length; ++i) {
            treatTypeNames[i] = mTimberPackTypes[i].getName();
        }
        return treatTypeNames;
    }

    @NonNull
    public static CharSequence[] getTreatTypeNames()
    {
        final CharSequence[] treatTypeNames = new CharSequence[values().length];
        for (int i = 0; i < treatTypeNames.length; ++i) {
            treatTypeNames[i] = values()[i].mName;
        }
        return treatTypeNames;
    }
}
