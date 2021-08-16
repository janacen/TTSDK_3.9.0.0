package demo;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;

import com.bytedance.sdk.openadsdk.AdSlot;
import com.bytedance.sdk.openadsdk.ISplashClickEyeListener;
import com.bytedance.sdk.openadsdk.TTAdNative;
import com.bytedance.sdk.openadsdk.TTSplashAd;
import com.totgames.wcjz6.meta.R;

import java.lang.ref.SoftReference;

import config.TTAdManagerHolder;


public class SplashActivity extends Activity  {
    private static final String TAG = "SplashActivity";
    /**
     * 从请求广告到广告展示出来最大耗时时间，只能在[3000,5000]ms之内。
     */
    private static final int FETCH_TIME_OUT = 3500;
    private FrameLayout mSplashContainer;
//    private LinearLayout mSplashHalfSizeLayout;
    //是否强制跳转到主页面
    private boolean mForceGoMain;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

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

        mSplashContainer = (FrameLayout) findViewById(R.id.splash_container);
//        mSplashHalfSizeLayout = (LinearLayout) findViewById(R.id.splash_half_size_layout);

        TTAdManagerHolder.init(this);
        //step2:(可选，强烈建议在合适的时机调用):申请部分权限，如read_phone_state,防止获取不了imei时候，下载类广告没有填充的问题。
        TTAdManagerHolder.get().requestPermissionIfNecessary(SplashActivity.this);
        mTTAdNative = TTAdManagerHolder.get().createAdNative(SplashActivity.this);

        loadSplashAd("887537749");
    }

    @Override
    protected void onResume() {
        //判断是否该跳转到主页面
        if (mForceGoMain) {
            goToMainActivity();
        }
        super.onResume();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mForceGoMain = true;
    }

    private TTAdNative mTTAdNative;
    private View splashAdView = null;
    private void loadSplashAd(final String codeId){
//        SplashClickEyeManager.getInstance().setSupportSplashClickEye(true);

        AdSlot adSlot = new AdSlot.Builder()
                .setCodeId(codeId)
                .setImageAcceptedSize(1080, 1920)
                .build();

        mTTAdNative.loadSplashAd(adSlot, new TTAdNative.SplashAdListener() {
            //请求广告失败
            @Override
            public void onError(int code, String message) {
                //开发者处理跳转到APP主页面逻辑
//                ConchJNI.RunJS("alert('err: '"+code+"','"+message+")");
                goToMainActivity();
            }

            //请求广告超时
            @Override
            public void onTimeout() {
                //开发者处理跳转到APP主页面逻辑
                goToMainActivity();
            }

            //请求广告成功
            @Override
            public void onSplashAdLoad(TTSplashAd ad) {
                if (ad == null) {
                    return;
                }
                //获取SplashView
                splashAdView = ad.getSplashView();

                //初始化开屏点睛相关数据
//                initSplashClickEyeData(ad, splashAdView);

                if (splashAdView != null && mSplashContainer != null ) {//&& !SplashActivity.this.isFinishing()
                    mSplashContainer.setVisibility(View.VISIBLE);
//                    if (mSplashHalfSizeLayout != null) {
//                        mSplashHalfSizeLayout.setVisibility(View.GONE);
//                    }

                    mSplashContainer.removeAllViews();
                    //把SplashView 添加到ViewGroup中,注意开屏广告view：width >=70%屏幕宽；height >=50%屏幕高
                    mSplashContainer.addView(splashAdView);
                    /**
                     * 设置是否开启开屏广告倒计时功能以及不显示跳过按钮,如果设置为true，您需要自定义倒计时逻辑，
                     * 参考样例请看：
                     * @see SplashActivity#useCustomCountdownButton
                     */
                    //useCustomCountdownButton(false,ad);

                } else {
                    goToMainActivity();
                }

                bindSplashAdListener(ad);
            }
        }, FETCH_TIME_OUT);
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
                goToMainActivity();
            }

            //超时倒计时结束
            @Override
            public void onAdTimeOver() {
                //开发者处理跳转到APP主页面逻辑
                goToMainActivity();
            }
        });
    }

    /**
     * 跳转到主页面
     */
    private void goToMainActivity() {
//        boolean isSupport = SplashClickEyeManager.getInstance().isSupportSplashClickEye();
//        if (isSupport) {
//            return;
//        } else {
//            TToast.show(this, "物料不支持点睛，直接返回到主界面");
//            SplashClickEyeManager.getInstance().clearSplashStaticData();
//        }

        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
//        if (mSplashContainer != null) {
//            mSplashContainer.removeAllViews();
//        }
        this.finish();
    }

    /**
     *初始化开屏点睛相关数据
     */
    private SplashClickEyeListener mSplashClickEyeListener;
    private SplashClickEyeManager mSplashClickEyeManager;
    private void initSplashClickEyeData(TTSplashAd splashAd, View splashView) {
        if (splashAd == null || splashView == null) {
            return;
        }
        mSplashClickEyeListener = new SplashClickEyeListener(SplashActivity.this, splashAd, mSplashContainer, true);

        splashAd.setSplashClickEyeListener(mSplashClickEyeListener);
        mSplashClickEyeManager = SplashClickEyeManager.getInstance();
        mSplashClickEyeManager.setSplashInfo(splashAd, splashView, getWindow().getDecorView());
    }


    /**
     * 判断应用是否已经获得SDK运行必须的READ_PHONE_STATE、WRITE_EXTERNAL_STORAGE两个权限。
     *
     * @return
     */
    private boolean hasNecessaryPMSGranted() {
        if (PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE)) {
            return true;
        }
        return false;
    }





    public static class SplashClickEyeListener implements ISplashClickEyeListener {
        private SoftReference<Activity> mActivity;
        private TTSplashAd mSplashAd;
        private View mSplashContainer;
        private boolean mIsFromSplashClickEye = false;

        public SplashClickEyeListener(Activity activity, TTSplashAd splashAd, View splashContainer, boolean isFromSplashClickEye) {
            mActivity = new SoftReference<>(activity);
            mSplashAd = splashAd;
            mSplashContainer = splashContainer;
            mIsFromSplashClickEye = isFromSplashClickEye;
        }

        @Override
        public void onSplashClickEyeAnimationStart() {
            //开始执行开屏点睛动画
            if (mIsFromSplashClickEye) {
                startSplashAnimationStart();
            }
        }

        @Override
        public void onSplashClickEyeAnimationFinish() {
            //sdk关闭了了点睛悬浮窗
            SplashClickEyeManager splashClickEyeManager = SplashClickEyeManager.getInstance();
            boolean isSupport = splashClickEyeManager.isSupportSplashClickEye();
            if (mIsFromSplashClickEye && isSupport) {
                finishActivity();
            }
            splashClickEyeManager.clearSplashStaticData();
        }

        @Override
        public boolean isSupportSplashClickEye(boolean isSupport) {
            SplashClickEyeManager splashClickEyeManager = SplashClickEyeManager.getInstance();
            splashClickEyeManager.setSupportSplashClickEye(isSupport);
            return false;
        }

        private void finishActivity() {
            if (mActivity.get() == null) {
                return;
            }
            mActivity.get().finish();
        }

        private void startSplashAnimationStart() {
            if (mActivity.get() == null || mSplashAd == null || mSplashContainer == null) {
                return;
            }
            SplashClickEyeManager splashClickEyeManager = SplashClickEyeManager.getInstance();
            ViewGroup content = mActivity.get().findViewById(android.R.id.content);
            splashClickEyeManager.startSplashClickEyeAnimation(mSplashContainer, content, content, new SplashClickEyeManager.AnimationCallBack() {
                @Override
                public void animationStart(int animationTime) {
                }

                @Override
                public void animationEnd() {
                    if (mSplashAd != null) {
                        mSplashAd.splashClickEyeAnimationFinish();
                    }
                }
            });
        }
    }
}