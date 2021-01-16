package uk.co.rosehilltimber.rosehilltreatmentapp;

import android.app.Application;
import android.support.annotation.NonNull;

public class RosehillTreatments extends Application
{

    private static RosehillTreatments sInstance;

    @Override
    public void onCreate()
    {
        super.onCreate();
        sInstance = this;
    }

    @NonNull
    public static RosehillTreatments getInstance()
    {
        return sInstance;
    }
}
