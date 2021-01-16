package uk.co.rosehilltimber.rosehilltreatmentapp.async.loaders;

import android.content.ContentResolver;
import android.content.Context;
import android.util.Log;
import uk.co.rosehilltimber.rosehilltreatmentapp.treatment.Treat;
import uk.co.rosehilltimber.rosehilltreatmentapp.treatment.Week;
import uk.co.rosehilltimber.rosehilltreatmentapp.utils.DatabaseUtility;
import uk.co.rosehilltimber.rosehilltreatmentapp.utils.TreatUtility;

import java.util.List;

public class AsyncWeekLoader extends AsyncLoader<List<Week>>
{

    public AsyncWeekLoader(final Context context)
    {
        super(context);
        super.onContentChanged();
    }

    public List<Week> loadInBackground()
    {
        final ContentResolver contentResolver = super.getContext().getContentResolver();
        if (contentResolver == null) {
            return null;
        }

        final List<Treat> treats = DatabaseUtility.selectAllTreats(contentResolver);
        TreatUtility.sortTreatsIntoDescendingOrder(treats);
        final List<Week> weeks = TreatUtility.intoWeeks(treats);
        TreatUtility.sortWeeksIntoDescendingOrder(weeks);

        return weeks;
    }

}
