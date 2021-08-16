package demo;

import android.content.Context;
import android.support.multidex.MultiDex;
import android.support.multidex.MultiDexApplication;

import config.TTAdManagerHolder;


/**
 * Create by hanweiwei on 11/07/2018
 */
@SuppressWarnings("unused")
public class DemoApplication extends MultiDexApplication {

    public static String PROCESS_NAME_XXXX = "process_name_xxxx";
    private static Context context;

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(base);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        //穿山甲SDK初始化
        //强烈建议在应用对应的Application#onCreate()方法中调用，避免出现content为null的异常
        TTAdManagerHolder.init(this);
        DemoApplication.context = getApplicationContext();

    }

    public static Context getAppContext() {
        return DemoApplication.context;
    }

}
