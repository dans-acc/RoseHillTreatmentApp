package uk.co.rosehilltimber.rosehilltreatmentapp.adapters;

import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

public class SectionsPagerAdapter extends FragmentPagerAdapter
{

    private final String[] mTitles;
    private final Fragment[] mFragments;

    public SectionsPagerAdapter(@NonNull final FragmentManager mFragmentManager, @NonNull final String[] mTitles,
                                @NonNull final Fragment[] mFragments)
    {
        super(mFragmentManager);

        // There must be a 1-1 relationship between the titles and the fragments.
        if (mTitles.length != mFragments.length) {
            throw new IllegalArgumentException(String.format(
                    "Titles fail to match up with fragments. Check mTitle (%d) and mFragments (%d) array sizes.",
                    mTitles.length, mFragments.length
            ));
        }

        this.mTitles = mTitles;
        this.mFragments = mFragments;
    }

    @NonNull
    @Override
    public CharSequence getPageTitle(final int position)
    {
        return mTitles[position];
    }

    @NonNull
    @Override
    public Fragment getItem(final int position)
    {
        return mFragments[position];
    }

    @Override
    public int getCount()
    {
        return mFragments.length;
    }

    @NonNull
    public String[] getTitles()
    {
        return mTitles;
    }

    @NonNull
    public Fragment[] getFragments()
    {
        return mFragments;
    }
}
