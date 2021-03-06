package demo;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.ValueCallback;
import android.widget.AbsoluteLayout;
import android.widget.FrameLayout;

import com.bytedance.sdk.openadsdk.AdSlot;
import com.bytedance.sdk.openadsdk.TTAdConstant;
import com.bytedance.sdk.openadsdk.TTAdManager;
import com.bytedance.sdk.openadsdk.TTAdNative;
import com.bytedance.sdk.openadsdk.TTFullScreenVideoAd;
import com.bytedance.sdk.openadsdk.TTNativeExpressAd;
import com.bytedance.sdk.openadsdk.TTRewardVideoAd;
import com.bytedance.sdk.openadsdk.TTSplashAd;
import com.totgames.wcjz6.meta.R;

import java.io.InputStream;
import java.util.List;

import config.TTAdManagerHolder;
import layaair.autoupdateversion.AutoUpdateAPK;
import layaair.game.IMarket.IPlugin;
import layaair.game.IMarket.IPluginRuntimeProxy;
import layaair.game.Market.GameEngine;
import layaair.game.browser.ConchJNI;
import layaair.game.config.config;
//import utils.TToast;


public class MainActivity extends Activity{
    public static final int AR_CHECK_UPDATE = 1;
    private IPlugin mPlugin = null;
    private IPluginRuntimeProxy mProxy = null;
    boolean isLoad=false;
    boolean isExit=false;
    public static SplashDialog mSplashDialog;
    private TTAdNative mTTAdNative;
    // private TTAdNative mTTAdNative_banner;
    private FrameLayout mGameContainer;
    private FrameLayout mBannerContainer;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        //???????????????
        WindowManager.LayoutParams lp = this.getWindow().getAttributes();
        if (Build.VERSION.SDK_INT >= 28) {
            lp.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
        }
        this.getWindow().setAttributes(lp);

        View decorView = getWindow().getDecorView();
        int systemUiVisibility = decorView.getSystemUiVisibility();
        int flags = 0;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
            flags = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_FULLSCREEN;
            systemUiVisibility |= flags;
            getWindow().getDecorView().setSystemUiVisibility(systemUiVisibility);
        }

        JSBridge.mMainActivity = this;
        mSplashDialog = new SplashDialog(this);
        mSplashDialog.showSplash();
        /*
         * ?????????????????????????????????????????????checkApkUpdate?????????????????????initEngine??????
         */
        checkApkUpdate(this);
        // initEngine();

//        TTAdManagerHolder.init(this);
        ///////////////////////////////////////////////////////////
        //step1:?????????sdk
        TTAdManager ttAdManager = TTAdManagerHolder.get();
        //step2:(?????????????????????????????????????????????):????????????????????????read_phone_state,??????????????????imei????????????????????????????????????????????????
//        TTAdManagerHolder.get().requestPermissionIfNecessary(this);
        //step3:??????TTAdNative??????,??????????????????????????????
        mTTAdNative = ttAdManager.createAdNative(getApplicationContext());

        mAdContainer = (GameEngine.getInstance().mLayaGameEngine.getAbsLayout()) ;

//        loadSplashAd("887537749");

    }


    //banner--
    private TTNativeExpressAd banner;
    private long startTime_banner = 0;
    private AbsoluteLayout mAdContainer;
//    private FrameLayout mFrameLayout;
//    private RelativeLayout mExpressContainer;
//    private RelativeLayout mMainLayout;
//    private View gameView;
    View bannerView;
    private boolean canShowBanner = true;
    public void create_banner(){
        if(canShowBanner){
//            ConchJNI.RunJS("alert('??????banner')");
            DisplayMetrics displayMetrics = new DisplayMetrics();
            float density = displayMetrics.density;

            loadExpressAd_banner("945045375", 400, 55);
            canShowBanner = false;
        }
        else {
            mBannerContainer.setAlpha(1);
        }
    }

    public void banner_destroy(){
//        banner.destroy();
//        canShowBanner = true;
        this.hide_banner();
    }

    public void hide_banner(){
        mBannerContainer.setAlpha(0);
    }

    private void loadExpressAd_banner(String codeId, float expressViewWidth, float expressViewHeight) {

        //step4:????????????????????????AdSlot,??????????????????????????????
        AdSlot adSlot = new AdSlot.Builder()
                .setCodeId(codeId) //?????????id
                .setSupportDeepLink(true)
                .setAdCount(1) //?????????????????????1???3???
                .setExpressViewAcceptedSize(expressViewWidth,expressViewHeight) //??????????????????view???size,??????dp
                .build();
        //step5:??????????????????????????????????????????????????????
        mTTAdNative.loadBannerExpressAd(adSlot, new TTAdNative.NativeExpressAdListener() {

            @Override
            public void onError(int code, String message) {
                //  TToast.show(MainActivity.this, "load error : " + code + ", " + message);
                canShowBanner = true;
//                ConchJNI.RunJS("show_banner_again()");
//                ConchJNI.RunJS("alert('err: '"+code+"','"+message+")");
            }

            @Override
            public void onNativeExpressAdLoad(List<TTNativeExpressAd> ads) {
                if (ads == null || ads.size() == 0){
                    return;
                }

                banner = ads.get(0);
                bindAdListener_banner(banner);
                startTime_banner = System.currentTimeMillis();

                bannerView = banner.getExpressAdView();

                if(null!=bannerView){
                    mBannerContainer.addView(bannerView);
                    mBannerContainer.setVisibility(View.VISIBLE);
                }
                banner.render();
            }
        });
    }

    private void bindAdListener_banner(TTNativeExpressAd ad) {
        ad.setExpressInteractionListener(new TTNativeExpressAd.ExpressAdInteractionListener() {
            @Override
            public void onAdClicked(View view, int type) {
                //TToast.show(mContext, "???????????????");
            }

            @Override
            public void onAdShow(View view, int type) {
                //TToast.show(mContext, "????????????");
            }

            @Override
            public void onRenderFail(View view, String msg, int code) {
                Log.e("ExpressView", "render fail:" + (System.currentTimeMillis() - startTime));
                // TToast.show(mContext, msg+" code:"+code);
            }

            @Override
            public void onRenderSuccess(View view, float width, float height) {
                Log.e("ExpressView", "render suc:" + (System.currentTimeMillis() - startTime));
                //??????view????????? ?????? dp
                // TToast.show(MainActivity.this, "????????????");

                DisplayMetrics displayMetrics = new DisplayMetrics();
                getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
                int height_display = displayMetrics.heightPixels;
                int width_display = displayMetrics.widthPixels;
                float density = displayMetrics.density;
                float densityDpi = displayMetrics.densityDpi;

//                bannerView.setY(height_display-height * density);
//                bannerView.setX(width_display/2-width/2*density+15*density);

                //mExpressContainer.addView(view);
            }
        });
    }
    //--

    //--insertAd
    private TTNativeExpressAd mTTAd;
    private long startTime = 0;
    private boolean mHasShowDownloadActive = false;

    public void create_insertAd(){
        loadNewInsertAd("946532811",250,250);
//        loadExpressAd("946532811", 250, 250);
    }


    private boolean insertAdError = false;
    private void loadExpressAd(String codeId, int expressViewWidth, int expressViewHeight) {
        ConchJNI.RunJS("alert('????????????')");
        insertAdError = false;
        //step4:????????????????????????AdSlot,??????????????????????????????
        AdSlot adSlot = new AdSlot.Builder()
                .setCodeId(codeId) //?????????id
                .setSupportDeepLink(true)
                .setAdCount(1) //?????????????????????1???3???
                .setExpressViewAcceptedSize(expressViewWidth,expressViewHeight) //??????????????????view???size,??????dp
                .build();
        //step5:??????????????????????????????????????????????????????
        mTTAdNative.loadInteractionExpressAd(adSlot, new TTAdNative.NativeExpressAdListener() {
            @Override
            public void onError(int code, String message) {
                //TToast.show(MainActivity.this, "load error : " + code + ", " + message);
                insertAdError = true;
                ConchJNI.RunJS("alert('err: '"+code+"','"+message+")");
            }

            @Override
            public void onNativeExpressAdLoad(List<TTNativeExpressAd> ads) {
                if (ads == null || ads.size() == 0){
                    return;
                }
                mTTAd = ads.get(0);
                if(insertAdError==false){
                    banner_destroy();
                }
                bindAdListener(mTTAd);
                startTime = System.currentTimeMillis();
                mTTAd.render();
            }
        });
    }

    private void bindAdListener(TTNativeExpressAd ad) {
        ad.setExpressInteractionListener(new TTNativeExpressAd.AdInteractionListener() {
            @Override
            public void onAdDismiss() {
                //TToast.show(MainActivity.this, "????????????");
                create_banner();
            }

            @Override
            public void onAdClicked(View view, int type) {
                //TToast.show(MainActivity.this, "???????????????");
            }

            @Override
            public void onAdShow(View view, int type) {
                //TToast.show(MainActivity.this, "????????????");

            }

            @Override
            public void onRenderFail(View view, String msg, int code) {
                Log.e("ExpressView","render fail:"+(System.currentTimeMillis() - startTime));
                // TToast.show(MainActivity.this, msg+" code:"+code);
            }

            @Override
            public void onRenderSuccess(View view, float width, float height) {
                Log.e("ExpressView","render suc:"+(System.currentTimeMillis() - startTime));
                //??????view????????? ?????? dp
                // TToast.show(MainActivity.this, "????????????");
                mTTAd.showInteractionExpressAd(MainActivity.this);

            }
        });

        if (ad.getInteractionType() != TTAdConstant.INTERACTION_TYPE_DOWNLOAD){
            return;
        }
    }

    ////////////////////////????????? ?????????////////////////////////
    private TTFullScreenVideoAd mttFullVideoAd = null;
    public void loadNewInsertAd(String codeId, int expressViewWidth, int expressViewHeight){
        AdSlot adSlot = new AdSlot.Builder()
                .setCodeId(codeId) //?????????id
                .setSupportDeepLink(true)
                .setAdCount(1) //?????????????????????1???3???
                .setExpressViewAcceptedSize(expressViewWidth,expressViewHeight) //??????????????????view???size,??????dp
                .build();


        mTTAdNative.loadFullScreenVideoAd(adSlot, new TTAdNative.FullScreenVideoAdListener() {
            //??????????????????
            @Override
            public void onError(int code, String message) {
//                ConchJNI.RunJS("alert('err: '"+code+"','"+message+")");
            }

            //?????????????????????????????????
            @Override
            public void onFullScreenVideoAdLoad(TTFullScreenVideoAd ad) {
                mttFullVideoAd = ad;
            }

            //????????????/???????????????????????????????????????????????????????????????????????????
            @Override
            public void onFullScreenVideoCached() {
                if(null!=mttFullVideoAd){
                    mttFullVideoAd.showFullScreenVideoAd(MainActivity.this, TTAdConstant.RitScenes.GAME_GIFT_BONUS, null);
                    bindNewInsertAdListener();
                }
            }

            @Override
            public void onFullScreenVideoCached(TTFullScreenVideoAd var) {

            }
        });
    }

    private void bindNewInsertAdListener(){
        mttFullVideoAd.setFullScreenVideoAdInteractionListener(new TTFullScreenVideoAd.FullScreenVideoAdInteractionListener() {
            //?????????????????????
            @Override
            public void onAdShow() {
                hide_banner();
            }
            //???????????????bar????????????
            @Override
            public void onAdVideoBarClick() {

            }
            //?????????????????????
            @Override
            public void onAdClose() {
                create_banner();
            }
            //???????????????????????????
            @Override
            public void onVideoComplete() {

            }
            //??????????????????
            @Override
            public void onSkippedVideo() {

            }
        });
    }



    /////////////////// rewardVideo /////////////////////////
    private TTRewardVideoAd mttRewardVideoAd;
    public void loadRewardVideoAd(final String codeId, int orientation) {
        //step4:????????????????????????AdSlot,??????????????????????????????
        AdSlot adSlot;
        //????????????????????????????????????????????????????????????,??????dp,????????????????????????????????????????????????????????????????????????
        adSlot = new AdSlot.Builder()
                .setCodeId(codeId)
                .setOrientation(orientation)
                .build();

        //step5:????????????
        mTTAdNative.loadRewardVideoAd(adSlot, new TTAdNative.RewardVideoAdListener() {
            @Override
            public void onError(int code, String message) {

            }

            //????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
            @Override
            public void onRewardVideoCached() {

            }

            @Override
            public void onRewardVideoCached(TTRewardVideoAd ad) {
                 ad.showRewardVideoAd(MainActivity.this, TTAdConstant.RitScenes.CUSTOMIZE_SCENES, "scenes_test");
            }

            //????????????????????????????????????????????????url?????????????????????????????????????????????????????????????????????????????????????????????????????????
            @Override
            public void onRewardVideoAdLoad(TTRewardVideoAd ad) {
                mttRewardVideoAd = ad;
                mttRewardVideoAd.setRewardAdInteractionListener(new TTRewardVideoAd.RewardAdInteractionListener() {

                    @Override
                    public void onAdShow() {

                    }

                    @Override
                    public void onAdVideoBarClick() {

                    }

                    @Override
                    public void onAdClose() {

                    }

                    //????????????????????????
                    @Override
                    public void onVideoComplete() {

                    }

                    @Override
                    public void onVideoError() {
                        ConchJNI.RunJS("rewardAd_error()");
                    }

                    //?????????????????????????????????????????????rewardVerify??????????????????rewardAmount??????????????????rewardName???????????????
                    @Override
                    public void onRewardVerify(boolean rewardVerify, int rewardAmount, String rewardName, int errorCode, String errorMsg) {
//                        String logString = "verify:" + rewardVerify + " amount:" + rewardAmount +
//                                " name:" + rewardName + " errorCode:" + errorCode + " errorMsg:" + errorMsg;
                        ConchJNI.RunJS("on_get_reward_video_reward()");
                    }

                    @Override
                    public void onSkippedVideo() {
                        ConchJNI.RunJS("rewardAd_close()");
                    }
                });
//                mttRewardVideoAd.setRewardPlayAgainInteractionListener(new TTRewardVideoAd.RewardAdInteractionListener() {
//                    @Override
//                    public void onAdShow() {
//
//                    }
//
//                    @Override
//                    public void onAdVideoBarClick() {
//
//                    }
//
//                    @Override
//                    public void onAdClose() {
//
//                    }
//
//                    //????????????????????????
//                    @Override
//                    public void onVideoComplete() {
//
//                    }
//
//                    @Override
//                    public void onVideoError() {
//
//                    }
//
//                    //?????????????????????????????????????????????rewardVerify??????????????????rewardAmount??????????????????rewardName???????????????
//                    @Override
//                    public void onRewardVerify(boolean rewardVerify, int rewardAmount, String rewardName, int errorCode, String errorMsg) {
//                        String logString = "rewardPlayAgain verify:" + rewardVerify + " amount:" + rewardAmount +
//                                " name:" + rewardName + " errorCode:" + errorCode + " errorMsg:" + errorMsg;
//                    }
//
//                    @Override
//                    public void onSkippedVideo() {
//                    }
//                });
//                mttRewardVideoAd.setDownloadListener(new TTAppDownloadListener() {
//                    @Override
//                    public void onIdle() {
//                        mHasShowDownloadActive = false;
//                    }
//
//                    @Override
//                    public void onDownloadActive(long totalBytes, long currBytes, String fileName, String appName) {
//                        Log.d("DML", "onDownloadActive==totalBytes=" + totalBytes + ",currBytes=" + currBytes + ",fileName=" + fileName + ",appName=" + appName);
//
//                        if (!mHasShowDownloadActive) {
//                            mHasShowDownloadActive = true;
//                        }
//                    }
//
//                    @Override
//                    public void onDownloadPaused(long totalBytes, long currBytes, String fileName, String appName) {
//                        Log.d("DML", "onDownloadPaused===totalBytes=" + totalBytes + ",currBytes=" + currBytes + ",fileName=" + fileName + ",appName=" + appName);
//
//                    }
//
//                    @Override
//                    public void onDownloadFailed(long totalBytes, long currBytes, String fileName, String appName) {
//                        Log.d("DML", "onDownloadFailed==totalBytes=" + totalBytes + ",currBytes=" + currBytes + ",fileName=" + fileName + ",appName=" + appName);
////                        TToast.show(RewardVideoActivity.this, "?????????????????????????????????????????????", Toast.LENGTH_LONG);
//                    }
//
//                    @Override
//                    public void onDownloadFinished(long totalBytes, String fileName, String appName) {
//                        Log.d("DML", "onDownloadFinished==totalBytes=" + totalBytes + ",fileName=" + fileName + ",appName=" + appName);
////                        TToast.show(RewardVideoActivity.this, "?????????????????????????????????????????????", Toast.LENGTH_LONG);
//                    }
//
//                    @Override
//                    public void onInstalled(String fileName, String appName) {
//                        Log.d("DML", "onInstalled==" + ",fileName=" + fileName + ",appName=" + appName);
////                        TToast.show(RewardVideoActivity.this, "???????????????????????????????????????", Toast.LENGTH_LONG);
//                    }
//                });
            }
        });
    }
    private View splashAdView = null;
    private void loadSplashAd(final String codeId){
        AdSlot adSlot = new AdSlot.Builder()
                .setCodeId(codeId)
                .setImageAcceptedSize(1080, 1920)
                .build();

        mTTAdNative.loadSplashAd(adSlot, new TTAdNative.SplashAdListener() {
            //??????????????????
            @Override
            public void onError(int code, String message) {
                //????????????????????????APP???????????????
                ConchJNI.RunJS("alert('err: '"+code+"','"+message+")");
            }

            //??????????????????
            @Override
            public void onTimeout() {
                //????????????????????????APP???????????????
            }

            //??????????????????
            @Override
            public void onSplashAdLoad(TTSplashAd ad) {
                if (ad == null) {
                    return;
                }
                //??????SplashView
                splashAdView = ad.getSplashView();
                mAdContainer.addView(splashAdView);
                bindSplashAdListener(ad);
            }
        }, 3500);
    }

    private void bindSplashAdListener(TTSplashAd ad){
        ad.setSplashInteractionListener(new TTSplashAd.AdInteractionListener() {

            //????????????
            @Override
            public void onAdClicked(View view, int type) {

            }

            //????????????
            @Override
            public void onAdShow(View view, int type) {

            }

            //????????????
            @Override
            public void onAdSkip() {
                //????????????????????????APP???????????????
                splashAdView.setVisibility(View.INVISIBLE);
            }

            //?????????????????????
            @Override
            public void onAdTimeOver() {
                //????????????????????????APP???????????????
                splashAdView.setVisibility(View.INVISIBLE);
            }
        });
    }


    //--///

    public void initEngine()
    {
        mProxy = new RuntimeProxy(this);
        mPlugin = new GameEngine(this);
        mPlugin.game_plugin_set_runtime_proxy(mProxy);
        mPlugin.game_plugin_set_option("localize","true");
        mPlugin.game_plugin_set_option("gameUrl", "http://stand.alone.version/index.js");
        mPlugin.game_plugin_init(3);
        View gameView = mPlugin.game_plugin_get_view();

//        this.setContentView(gameView);
        isLoad=true;

        this.setContentView(R.layout.activity_native_express_banner);
        mGameContainer = (FrameLayout) findViewById(R.id.game_container);
        mBannerContainer = (FrameLayout) findViewById(R.id.express_container);
        mGameContainer.addView(gameView);
        mBannerContainer.setVisibility(View.INVISIBLE);

    }
    public  boolean isOpenNetwork(Context context)
    {
        if (!config.GetInstance().m_bCheckNetwork)
            return true;
        ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return connManager.getActiveNetworkInfo() != null && (connManager.getActiveNetworkInfo().isAvailable() && connManager.getActiveNetworkInfo().isConnected());
    }
    public void settingNetwork(final Context context, final int p_nType)
    {
        AlertDialog.Builder pBuilder = new AlertDialog.Builder(context);
        pBuilder.setTitle("???????????????????????????????????????????????????").setMessage("????????????????????????????");
        // ????????????
        pBuilder.setPositiveButton("???", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface p_pDialog, int arg1) {
                Intent intent;
                try {
                    String sdkVersion = android.os.Build.VERSION.SDK;
                    if (Integer.valueOf(sdkVersion) > 10) {
                        intent = new Intent(
                                android.provider.Settings.ACTION_WIRELESS_SETTINGS);
                    } else {
                        intent = new Intent();
                        ComponentName comp = new ComponentName(
                                "com.android.settings",
                                "com.android.settings.WirelessSettings");
                        intent.setComponent(comp);
                        intent.setAction("android.intent.action.VIEW");
                    }
                    ((Activity)context).startActivityForResult(intent, p_nType);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        pBuilder.setNegativeButton("???", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                ((Activity)context).finish();
            }
        });
        AlertDialog alertdlg = pBuilder.create();
        alertdlg.setCanceledOnTouchOutside(false);
        alertdlg.show();
    }
    public void checkApkUpdate( Context context,final ValueCallback<Integer> callback)
    {
        if (isOpenNetwork(context)) {
            // ??????????????????
            if ( "0".equals(config.GetInstance().getProperty("IsHandleUpdateAPK","0")) == false ) {
                Log.e("0", "==============Java?????? checkApkUpdate");
                new AutoUpdateAPK(context, new ValueCallback<Integer>() {
                    @Override
                    public void onReceiveValue(Integer integer) {
                        Log.e("",">>>>>>>>>>>>>>>>>>");
                        callback.onReceiveValue(integer);
                    }
                });
            } else {
                Log.e("0", "==============Java?????? checkApkUpdate ?????????????????????update");
                callback.onReceiveValue(1);
            }
        } else {
            settingNetwork(context,AR_CHECK_UPDATE);
        }
    }
    public void checkApkUpdate(Context context) {
        InputStream inputStream = getClass().getResourceAsStream("/assets/config.ini");
        config.GetInstance().init(inputStream);
        checkApkUpdate(context,new ValueCallback<Integer>() {
            @Override
            public void onReceiveValue(Integer integer) {
                if (integer.intValue() == 1) {
                    initEngine();
                } else {
                    finish();
                }
            }
        });
    }
    public void onActivityResult(int requestCode, int resultCode,Intent intent) {
        if (requestCode == AR_CHECK_UPDATE) {
            checkApkUpdate(this);
        }
    }
    protected void onPause()
    {
        super.onPause();
        if(isLoad)mPlugin.game_plugin_onPause();
    }
    //------------------------------------------------------------------------------
    protected void onResume()
    {
        super.onResume();
        if(isLoad)mPlugin.game_plugin_onResume();

    }

    protected void onDestroy()
    {
        super.onDestroy();
        if(isLoad)mPlugin.game_plugin_onDestory();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        return super.onKeyDown(keyCode, event);
    }
}