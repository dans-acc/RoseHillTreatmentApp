package uk.co.rosehilltimber.rosehilltreatmentapp.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.util.Log;
import uk.co.rosehilltimber.rosehilltreatmentapp.R;
import uk.co.rosehilltimber.rosehilltreatmentapp.adapters.SectionsPagerAdapter;
import uk.co.rosehilltimber.rosehilltreatmentapp.fragments.FileLogFragment;
import uk.co.rosehilltimber.rosehilltreatmentapp.fragments.WeekLogFragment;
import uk.co.rosehilltimber.rosehilltreatmentapp.treatment.Week;

public class LogActivity extends AppCompatActivity
{

    @SuppressWarnings("WeakerAccess")
    protected static class IntentKey
    {
        protected static final String CURRENT_WEEK_INSTANCE = "CURRENT_WEEK_INSTANCE";
    }

    public static class RequestCode
    {
        public static final int UPDATE_CURRENT_WEEK = 5;
    }

    private Toolbar mToolbar;

    private ViewPager mViewPager;
    private TabLayout mTabLayout;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        super.setContentView(R.layout.activity_log);

        mToolbar = super.findViewById(R.id.activity_log_toolbar);
        mToolbar.setTitle(super.getTitle());
        super.setSupportActionBar(mToolbar);

        if (super.getSupportActionBar()!= null) {
            super.getSupportActionBar().setDisplayShowTitleEnabled(true);
            super.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        final Intent intent = super.getIntent();

        final Week currentWeek = intent.getParcelableExtra(IntentKey.CURRENT_WEEK_INSTANCE);
        final WeekLogFragment weekLogFragment = currentWeek != null
                ? WeekLogFragment.newInstance(currentWeek)
                : WeekLogFragment.newInstance();

        final SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(
                super.getSupportFragmentManager(),
                new String[] {
                        super.getString(R.string.tab_database),
                        super.getString(R.string.tab_exported_files)
                },
                new Fragment[] {
                        weekLogFragment,
                        FileLogFragment.newInstance()
                }
        );

        mViewPager = super.findViewById(R.id.activity_log_view_pager);
        mViewPager.setAdapter(sectionsPagerAdapter);

        mTabLayout = super.findViewById(R.id.activity_log_tab_layout);
        mTabLayout.setupWithViewPager(mViewPager);
    }

    public Toolbar getToolbar()
    {
        return mToolbar;
    }

    public ViewPager getViewPager()
    {
        return mViewPager;
    }

    public TabLayout getTabLayout()
    {
        return mTabLayout;
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data)
    {
        if (requestCode == Activity.RESULT_OK) {
            super.setResult(Activity.RESULT_OK);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

}
