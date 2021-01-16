package uk.co.rosehilltimber.rosehilltreatmentapp.async.loaders;

import android.content.ContentResolver;
import android.content.Context;
import uk.co.rosehilltimber.rosehilltreatmentapp.treatment.Treat;
import uk.co.rosehilltimber.rosehilltreatmentapp.utils.DatabaseUtility;
import uk.co.rosehilltimber.rosehilltreatmentapp.utils.TreatUtility;

import java.time.LocalDate;
import java.util.List;

public class AsyncTreatLoader extends AsyncLoader<List<Treat>>
{

    public static class BundleKey
    {
        public static final String WEEK_DATE_TIMESTAMP = "WEEK_DATE_TIMESTAMP";
    }

    private LocalDate mWeekDate;

    public AsyncTreatLoader(final Context context,
                            final LocalDate mWeekDate)
    {
        super(context);

        this.mWeekDate = mWeekDate;
        super.onContentChanged();
    }

    private LocalDate getWeekDate()
    {
        return mWeekDate;
    }

    @Override
    public List<Treat> loadInBackground()
    {
        final ContentResolver contentResolver = super.getContext().getContentResolver();
        if (contentResolver == null) {
            return null;
        }
        final List<Treat> treats = DatabaseUtility.selectAllTreats(contentResolver, mWeekDate);
        TreatUtility.sortTreatsIntoDescendingOrder(treats);
        return treats;
    }

}
