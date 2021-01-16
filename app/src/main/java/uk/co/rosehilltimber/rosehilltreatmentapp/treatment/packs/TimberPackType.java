package uk.co.rosehilltimber.rosehilltreatmentapp.treatment.packs;

import android.support.annotation.NonNull;

public enum TimberPackType
{

    // The type of packs available.
    CUBOID(CuboidTimberPack.class, "Cuboid Timber Pack"),
    ROUND(RoundTimberPack.class, "Round Timber Pack");

    private Class<? extends TimberPack> mClass;
    private String mName;

    TimberPackType(@NonNull final Class<? extends TimberPack> mClass,
                   @NonNull final String mName)
    {
        this.mClass = mClass;
        this.mName = mName;
    }

    @NonNull
    public final Class<? extends TimberPack> getClassType()
    {
        return mClass;
    }

    @NonNull
    public final String getName()
    {
        return mName;
    }

}
