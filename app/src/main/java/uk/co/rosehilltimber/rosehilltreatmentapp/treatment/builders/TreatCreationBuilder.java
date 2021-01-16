package uk.co.rosehilltimber.rosehilltreatmentapp.treatment.builders;

import android.content.ContentProviderOperation;
import android.net.Uri;
import uk.co.rosehilltimber.rosehilltreatmentapp.treatment.TreatType;
import uk.co.rosehilltimber.rosehilltreatmentapp.treatment.database.TreatContract;
import uk.co.rosehilltimber.rosehilltreatmentapp.utils.DateUtility;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.UUID;

public class TreatCreationBuilder extends TreatBuilder
{

    private int mInitialTreatNumber;

    public TreatCreationBuilder(final LocalDate mWeekDate,
                                final TreatType mTreatType,
                                final float mMaximumTankVolume,
                                final int mInitialTreatNumber)
    {
        super(
                UUID.randomUUID(),
                mWeekDate,
                mTreatType,
                new ArrayList<>(),
                mMaximumTankVolume
        );

        this.mInitialTreatNumber = mInitialTreatNumber;
    }


    public int getInitialTreatNumber()
    {
        return mInitialTreatNumber;
    }

    public void setInitialTreatNumber(final int mInitialTreatNumber)
    {
        this.mInitialTreatNumber = mInitialTreatNumber;
    }

    @Override
    public ArrayList<ContentProviderOperation> buildContentProviderOperations()
    {
        final ArrayList<ContentProviderOperation> contentProviderOperations = new ArrayList<>();

        final Uri newTreatmentUri = TreatContract.TreatEntry.buildTreatUri(
                super.mUUID,
                true,
                mInitialTreatNumber
        );

        final ContentProviderOperation newTreatmentOperation = ContentProviderOperation.newInsert(newTreatmentUri)
                .withValue(TreatContract.TreatEntry._ID, super.mUUID.toString())
                .withValue(TreatContract.TreatEntry.COLUMN_NUMBER, mInitialTreatNumber)
                .withValue(TreatContract.TreatEntry.COLUMN_DATE, super.mWeekDate.format(DateUtility.DATABASE_DATE_FORMATTER))
                .withValue(TreatContract.TreatEntry.COLUMN_TYPE, super.mType.name())
                .build();

        contentProviderOperations.add(newTreatmentOperation);
        contentProviderOperations.addAll(super.buildContentProviderOperations());

        return contentProviderOperations;
    }

}
