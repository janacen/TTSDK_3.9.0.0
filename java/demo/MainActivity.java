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

        //挖空屏适配
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
         * 如果不想使用更新流程，可以屏蔽checkApkUpdate函数，直接打开initEngine函数
         */
        checkApkUpdate(this);
        // initEngine();

//        TTAdManagerHolder.init(this);
        ///////////////////////////////////////////////////////////
        //step1:初始化sdk
        TTAdManager ttAdManager = TTAdManagerHolder.get();
        //step2:(可选，强烈建议在合适的时机调用):申请部分权限，如read_phone_state,防止获取不了imei时候，下载类广告没有填充的问题。
//        TTAdManagerHolder.get().requestPermissionIfNecessary(this);
        //step3:创建TTAdNative对象,用于调用广告请求接口
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
//            ConchJNI.RunJS("alert('创建banner')");
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

        //step4:创建广告请求参数AdSlot,具体参数含义参考文档
        AdSlot adSlot = new AdSlot.Builder()
                .setCodeId(codeId) //广告位id
                .setSupportDeepLink(true)
                .setAdCount(1) //请求广告数量为1到3条
                .setExpressViewAcceptedSize(expressViewWidth,expressViewHeight) //期望模板广告view的size,单位dp
                .build();
        //step5:请求广告，对请求回调的广告作渲染处理
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
                //TToast.show(mContext, "广告被点击");
            }

            @Override
            public void onAdShow(View view, int type) {
                //TToast.show(mContext, "广告展示");
            }

            @Override
            public void onRenderFail(View view, String msg, int code) {
                Log.e("ExpressView", "render fail:" + (System.currentTimeMillis() - startTime));
                // TToast.show(mContext, msg+" code:"+code);
            }

            @Override
            public void onRenderSuccess(View view, float width, float height) {
                Log.e("ExpressView", "render suc:" + (System.currentTimeMillis() - startTime));
                //返回view的宽高 单位 dp
                // TToast.show(MainActivity.this, "渲染成功");

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
        ConchJNI.RunJS("alert('创建插屏')");
        insertAdError = false;
        //step4:创建广告请求参数AdSlot,具体参数含义参考文档
        AdSlot adSlot = new AdSlot.Builder()
                .setCodeId(codeId) //广告位id
                .setSupportDeepLink(true)
                .setAdCount(1) //请求广告数量为1到3条
                .setExpressViewAcceptedSize(expressViewWidth,expressViewHeight) //期望模板广告view的size,单位dp
                .build();
        //step5:请求广告，对请求回调的广告作渲染处理
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
                //TToast.show(MainActivity.this, "广告关闭");
                create_banner();
            }

            @Override
            public void onAdClicked(View view, int type) {
                //TToast.show(MainActivity.this, "广告被点击");
            }

            @Override
            public void onAdShow(View view, int type) {
                //TToast.show(MainActivity.this, "广告展示");

            }

            @Override
            public void onRenderFail(View view, String msg, int code) {
                Log.e("ExpressView","render fail:"+(System.currentTimeMillis() - startTime));
                // TToast.show(MainActivity.this, msg+" code:"+code);
            }

            @Override
            public void onRenderSuccess(View view, float width, float height) {
                Log.e("ExpressView","render suc:"+(System.currentTimeMillis() - startTime));
                //返回view的宽高 单位 dp
                // TToast.show(MainActivity.this, "渲染成功");
                mTTAd.showInteractionExpressAd(MainActivity.this);

            }
        });

        if (ad.getInteractionType() != TTAdConstant.INTERACTION_TYPE_DOWNLOAD){
            return;
        }
    }

    ////////////////////////新插屏 自渲染////////////////////////
    private TTFullScreenVideoAd mttFullVideoAd = null;
    public void loadNewInsertAd(String codeId, int expressViewWidth, int expressViewHeight){
        AdSlot adSlot = new AdSlot.Builder()
                .setCodeId(codeId) //广告位id
                .setSupportDeepLink(true)
                .setAdCount(1) //请求广告数量为1到3条
                .setExpressViewAcceptedSize(expressViewWidth,expressViewHeight) //期望模板广告view的size,单位dp
                .build();


        mTTAdNative.loadFullScreenVideoAd(adSlot, new TTAdNative.FullScreenVideoAdListener() {
            //请求广告失败
            @Override
            public void onError(int code, String message) {
//                ConchJNI.RunJS("alert('err: '"+code+"','"+message+")");
            }

            //广告物料加载完成的回调
            @Override
            public void onFullScreenVideoAdLoad(TTFullScreenVideoAd ad) {
                mttFullVideoAd = ad;
            }

            //广告视频/图片加载完成的回调，接入方可以在这个回调后展示广告
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
            //广告的展示回调
            @Override
            public void onAdShow() {
                hide_banner();
            }
            //广告的下载bar点击回调
            @Override
            public void onAdVideoBarClick() {

            }
            //广告关闭的回调
            @Override
            public void onAdClose() {
                create_banner();
            }
            //视频播放完毕的回调
            @Override
            public void onVideoComplete() {

            }
            //跳过视频播放
            @Override
            public void onSkippedVideo() {

            }
        });
    }



    /////////////////// rewardVideo /////////////////////////
    private TTRewardVideoAd mttRewardVideoAd;
    public void loadRewardVideoAd(final String codeId, int orientation) {
        //step4:创建广告请求参数AdSlot,具体参数含义参考文档
        AdSlot adSlot;
        //模板广告需要设置期望个性化模板广告的大小,单位dp,代码位是否属于个性化模板广告，请在穿山甲平台查看
        adSlot = new AdSlot.Builder()
                .setCodeId(codeId)
                .setOrientation(orientation)
                .build();

        //step5:请求广告
        mTTAdNative.loadRewardVideoAd(adSlot, new TTAdNative.RewardVideoAdListener() {
            @Override
            public void onError(int code, String message) {

            }

            //视频广告加载后，视频资源缓存到本地的回调，在此回调后，播放本地视频，流畅不阻塞。
            @Override
            public void onRewardVideoCached() {

            }

            @Override
            public void onRewardVideoCached(TTRewardVideoAd ad) {
                 ad.showRewardVideoAd(MainActivity.this, TTAdConstant.RitScenes.CUSTOMIZE_SCENES, "scenes_test");
            }

            //视频广告的素材加载完毕，比如视频url等，在此回调后，可以播放在线视频，网络不好可能出现加载缓冲，影响体验。
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

                    //视频播放完成回调
                    @Override
                    public void onVideoComplete() {

                    }

                    @Override
                    public void onVideoError() {
                        ConchJNI.RunJS("rewardAd_error()");
                    }

                    //视频播放完成后，奖励验证回调，rewardVerify：是否有效，rewardAmount：奖励梳理，rewardName：奖励名称
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
//                    //视频播放完成回调
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
//                    //视频播放完成后，奖励验证回调，rewardVerify：是否有效，rewardAmount：奖励梳理，rewardName：奖励名称
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
////                        TToast.show(RewardVideoActivity.this, "下载失败，点击下载区域重新下载", Toast.LENGTH_LONG);
//                    }
//
//                    @Override
//                    public void onDownloadFinished(long totalBytes, String fileName, String appName) {
//                        Log.d("DML", "onDownloadFinished==totalBytes=" + totalBytes + ",fileName=" + fileName + ",appName=" + appName);
////                        TToast.show(RewardVideoActivity.this, "下载完成，点击下载区域重新下载", Toast.LENGTH_LONG);
//                    }
//
//                    @Override
//                    public void onInstalled(String fileName, String appName) {
//                        Log.d("DML", "onInstalled==" + ",fileName=" + fileName + ",appName=" + appName);
////                        TToast.show(RewardVideoActivity.this, "安装完成，点击下载区域打开", Toast.LENGTH_LONG);
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
            //请求广告失败
            @Override
            public void onError(int code, String message) {
                //开发者处理跳转到APP主页面逻辑
                ConchJNI.RunJS("alert('err: '"+code+"','"+message+")");
            }

            //请求广告超时
            @Override
            public void onTimeout() {
                //开发者处理跳转到APP主页面逻辑
            }

            //请求广告成功
            @Override
            public void onSplashAdLoad(TTSplashAd ad) {
                if (ad == null) {
                    return;
                }
                //获取SplashView
                splashAdView = ad.getSplashView();
                mAdContainer.addView(splashAdView);
                bindSplashAdListener(ad);
            }
        }, 3500);
    }

    private void bindSplashAdListener(TTSplashAd ad){
        ad.setSplashInteractionListener(new TTSplashAd.AdInteractionListener() {

            //点击回调
            @Override
            public void onAdClicked(View view, int type) {

            }

            //展示回调
            @Override
            public void onAdShow(View view, int type) {

            }

            //跳过回调
            @Override
            public void onAdSkip() {
                //开发者处理跳转到APP主页面逻辑
                splashAdView.setVisibility(View.INVISIBLE);
            }

            //超时倒计时结束
            @Override
            public void onAdTimeOver() {
                //开发者处理跳转到APP主页面逻辑
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
        pBuilder.setTitle("连接失败，请检查网络或与开发商联系").setMessage("是否对网络进行设置?");
        // 退出按钮
        pBuilder.setPositiveButton("是", new DialogInterface.OnClickListener() {
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
        pBuilder.setNegativeButton("否", new DialogInterface.OnClickListener() {
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
            // 自动版本更新
            if ( "0".equals(config.GetInstance().getProperty("IsHandleUpdateAPK","0")) == false ) {
                Log.e("0", "==============Java流程 checkApkUpdate");
                new AutoUpdateAPK(context, new ValueCallback<Integer>() {
                    @Override
                    public void onReceiveValue(Integer integer) {
                        Log.e("",">>>>>>>>>>>>>>>>>>");
                        callback.onReceiveValue(integer);
                    }
                });
            } else {
                Log.e("0", "==============Java流程 checkApkUpdate 不许要自己管理update");
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