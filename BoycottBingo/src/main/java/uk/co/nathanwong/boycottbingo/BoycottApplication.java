package uk.co.nathanwong.boycottbingo;

import android.app.Application;

import com.crashlytics.android.Crashlytics;

import io.fabric.sdk.android.Fabric;

/**
 * Base application class that we can use to initialise app-wide stuff
 * Created by nathan on 07/09/2015.
 */
public class BoycottApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        Fabric.with(this, new Crashlytics());

    }
}
