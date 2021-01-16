package uk.co.rosehilltimber.rosehilltreatmentapp.treatment.builders;

import uk.co.rosehilltimber.rosehilltreatmentapp.treatment.Treat;

import java.util.ArrayList;

public class TreatUpdateBuilder extends TreatBuilder
{

    private int mNumber;

    public TreatUpdateBuilder(final Treat treat, final float mMaximumTankVolume)
    {
        super(
                treat.getUUID(),
                treat.getWeekDate(),
                treat.getType(),
                treat.getTimberPacks(),
                mMaximumTankVolume
        );

        mNumber = treat.getNumber();
    }

    public int getTreatNumber()
    {
        return mNumber;
    }

    public void setNumber(final int mNumber)
    {
        this.mNumber = mNumber;
    }

    public Treat buildTreat()
    {
        return new Treat(
                super.mUUID,
                mNumber,
                super.mWeekDate,
                super.mType,
                new ArrayList<>(super.mTimberPacks)
        );
    }
}
