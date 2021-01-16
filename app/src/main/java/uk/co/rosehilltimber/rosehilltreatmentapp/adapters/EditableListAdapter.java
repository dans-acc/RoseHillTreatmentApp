package uk.co.rosehilltimber.rosehilltreatmentapp.adapters;

import android.app.Activity;
import android.support.annotation.LayoutRes;
import android.view.View;

import java.util.List;

public class EditableListAdapter<T> extends BaseListAdapter<T>
{

    private static final boolean DEFAULT_EDITABLE = true;

    protected boolean mEditable;

    protected EditableListAdapter(final Activity mActivity,
                           final View.OnClickListener mOnClickListener,
                           final List<T> mItems,
                           @LayoutRes int mResource,
                           final boolean mEditable)
    {
        super(mActivity, mOnClickListener, mItems, mResource);

        this.mEditable = mEditable;
    }

    protected EditableListAdapter(final Activity mActivity,
                               final View.OnClickListener mOnClickListener,
                               final List<T> mItems,
                               @LayoutRes int mResource)
    {
        this(
                mActivity,
                mOnClickListener,
                mItems,
                mResource,
                DEFAULT_EDITABLE
        );
    }

    public boolean isEditable()
    {
        return mEditable;
    }

    public void setEditable(final boolean mEditable)
    {
        this.mEditable = mEditable;
    }

}
