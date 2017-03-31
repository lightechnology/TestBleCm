package org.adol.blecm.testblecm;

import android.app.Application;

import org.xutils.x;

/**
 * Created by adolp on 2017/3/30.
 */

public class TbApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        x.Ext.init(this);
        x.Ext.setDebug(true);
    }

}
