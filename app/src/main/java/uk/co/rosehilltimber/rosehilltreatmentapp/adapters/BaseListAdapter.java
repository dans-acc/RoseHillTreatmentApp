package uk.co.rosehilltimber.rosehilltreatmentapp.adapters;

import android.app.Activity;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ArrayAdapter;
import uk.co.rosehilltimber.rosehilltreatmentapp.treatment.Treat;
import uk.co.rosehilltimber.rosehilltreatmentapp.treatment.packs.TimberPack;

import java.util.ArrayList;
import java.util.List;

public abstract class BaseListAdapter<T> extends ArrayAdapter<T>
{

    protected static final boolean DEFAULT_ATTACH_TO_ROOT = false;

    protected final Activity mActivity;
    protected final View.OnClickListener mOnClickListener;

    protected final List<T> mItems;

    protected final int mResource;

    protected BaseListAdapter(final Activity mActivity,
                           final View.OnClickListener mOnClickListener,
                           final List<T> mItems,
                           @LayoutRes int mResource)
    {
        super(mActivity, mResource, mItems);

        this.mActivity = mActivity;
        this.mOnClickListener = mOnClickListener;
        this.mItems = mItems;
        this.mResource = mResource;
    }

    public final Activity getActivity() {
        return mActivity;
    }

    public final View.OnClickListener getOnClickListener() {
        return mOnClickListener;
    }

    public final List<T> getItems()
    {
        return mItems;
    }

    public final int getResource()
    {
        return mResource;
    }

    public void updateListAdapter()
    {
        List<T> items = new ArrayList<>(mItems);
        super.clear();
        super.addAll(items);
        super.notifyDataSetChanged();
    }

    @Nullable
    public T getItemFromViewTagPosition(final View view)
    {
        if (view == null) {
            return null;
        }
        try {
            final Object tag = view.getTag();
            if (tag == null) {
                return null;
            }
            int position = (int) tag;
            if (position < 0 || position >= mItems.size()) {
                return null;
            }
            return mItems.get(position);
        } catch (final ClassCastException | NullPointerException
                | ArrayIndexOutOfBoundsException e) {
            e.printStackTrace();
        }
        return null;
    }
}
