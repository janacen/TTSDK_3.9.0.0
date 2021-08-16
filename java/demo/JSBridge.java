package demo;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.ActivityCompat;

import com.bytedance.sdk.openadsdk.TTAdConstant;

import org.json.JSONArray;
import org.json.JSONException;


public class JSBridge {
    public static Handler m_Handler = new Handler(Looper.getMainLooper());
    public static MainActivity mMainActivity = null;

    public static void hideSplash() {
        m_Handler.post(
                new Runnable() {
                    public void run() {
                        MainActivity.mSplashDialog.dismissSplash();
                    }
                });
    }

    public static void setFontColor(final String color) {
        m_Handler.post(
                new Runnable() {
                    public void run() {
                        MainActivity.mSplashDialog.setFontColor(Color.parseColor(color));
                    }
                });
    }

    public static void setTips(final JSONArray tips) {
        m_Handler.post(
                new Runnable() {
                    public void run() {
                        try {
                            String[] tipsArray = new String[tips.length()];
                            for (int i = 0; i < tips.length(); i++) {
                                tipsArray[i] = tips.getString(i);
                            }
                            MainActivity.mSplashDialog.setTips(tipsArray);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
    }

    public static void bgColor(final String color) {
        m_Handler.post(
                new Runnable() {
                    public void run() {
                        MainActivity.mSplashDialog.setBackgroundColor(Color.parseColor(color));
                    }
                });
    }

    public static void loading(final double percent) {
        m_Handler.post(
                new Runnable() {
                    public void run() {
                        MainActivity.mSplashDialog.setPercent((int)percent);
                    }
                });
    }

    public static void showTextInfo(final boolean show) {
        m_Handler.post(
                new Runnable() {
                    public void run() {
                        MainActivity.mSplashDialog.showTextInfo(show);
                    }
                });
    }


    static boolean hasNecessaryPMSGranted() {
        if (PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(mMainActivity, Manifest.permission.READ_PHONE_STATE)) {
            if (PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(mMainActivity, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                return true;
            }
        }
        return false;
    }

    public static void show_banner(){
        if(hasNecessaryPMSGranted()){
            mMainActivity.create_banner();
        }

    }
    public static void destroy_banner(){
        if(hasNecessaryPMSGranted()){
           mMainActivity.banner_destroy();
        }
    }

    public static void hide_banner(){
        mMainActivity.hide_banner();
    }

    public static void show_video(){
        if(hasNecessaryPMSGranted()){
//            Intent intent = new Intent(mMainActivity, RewardVideoActivity.class);
//            mMainActivity.startActivity(intent);
             mMainActivity.loadRewardVideoAd("945045377", TTAdConstant.HORIZONTAL);
        }

    }

    public static void show_insertAd(){
        if(hasNecessaryPMSGranted()){
            mMainActivity.create_insertAd();
        }

    }
}
