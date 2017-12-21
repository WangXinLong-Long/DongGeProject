package com.invengo.app;

import android.app.Application;
import android.content.Context;

/**
 * @author wxl
 * @date on 2017/12/21.
 * @describe:
 */

public class Appli extends Application {

    private static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
    }

    public static Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }
}
