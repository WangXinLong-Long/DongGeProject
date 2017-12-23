package com.invengo.base.ui;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SlidingPaneLayout;
import android.support.v7.widget.ActionMenuView;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.invengo.app.Appli;
import com.invengo.base.presenter.impl.BasePresenterImpl;
import com.invengo.sample.R;
import com.invengo.util.okHttp.Rxjava.RxBus;
import com.invengo.util.okHttp.Rxjava.Utils;
import com.invengo.util.ui.Constant;
import com.invengo.util.ui.EToast;
import com.invengo.util.ui.WeakHandler;
import com.trello.rxlifecycle2.components.support.RxAppCompatActivity;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import static com.invengo.util.okHttp.Rxjava.Utils.screenshots;
import static com.invengo.util.ui.Constant.LAST_ACTIVITY_NAME;


/**
 * TODO 基础activity 默认开启透明导航条
 * Created by user on 2016/11/4.
 */

public abstract class BaseActivity<P extends BasePresenterImpl> extends RxAppCompatActivity implements Constant, /*RxConstant, */SlidingPaneLayout.PanelSlideListener {
    private Appli mAppli;//applicaiton
    private WeakHandler mBaseHandler;//handler
//    private DaoSession mDaoSession;//数据库
//    private Unbinder mUnbinder;//用于butterKnife解绑
    private P mPresenter;//功能调用
    private boolean mIsNeedAdapterPhone = true;
    private boolean mIsNeedGoneNavigationBar;
    private boolean mIsUsedSlidingBack = false;//是否使用侧滑返回

    private long mExitPressedTime = 0;

    private final static String TAG = BaseActivity.class.getSimpleName();
    private String WINDOWBITMAP;
//    private File mFileTemp;
    private SlidingPaneLayout slidingPaneLayout;
    private FrameLayout frameLayout;
    private ImageView behindImageView;
    private ImageView shadowImageView;
    private int defaultTranslationX = 100;
    private int shadowWidth = 20;

// 使用侧滑返回
//    @Override
//    protected void beforeOnCreate() {
//        super.setIsUsedSlidingBack(true);
//        super.beforeOnCreate();
//        this.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right);
//    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        beforeOnCreate();
        super.onCreate(savedInstanceState);
        before();
        if (layoutID() > 0)
            setContentView(layoutID());
        after();
        init(savedInstanceState);
        data();
    }

    @Override
    protected void onResume() {
        super.onResume();
//        AppStats.addActivityStats(this);
//        BaseInfoUtils.addTopActivity(getClass().getName());
    }

    @Override
    protected void onPause() {
        super.onPause();
//        AppStats.removeActivityStats(this);
    }

    protected void beforeOnCreate() {
        if (mIsUsedSlidingBack) {
            WINDOWBITMAP = (getIntent() != null ? getIntent().getStringExtra(LAST_ACTIVITY_NAME) : getClass().getName()) + ".jpg";
            //通过反射来改变SlidingPanelayout的值
            try {
                slidingPaneLayout = new SlidingPaneLayout(this);
                Field f_overHang = SlidingPaneLayout.class.getDeclaredField("mOverhangSize");
                f_overHang.setAccessible(true);
                f_overHang.set(slidingPaneLayout, 0);
                slidingPaneLayout.setPanelSlideListener(this);
                slidingPaneLayout.setSliderFadeColor(getResources().getColor(android.R.color.transparent));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    protected void before() {
        mAppli = (Appli) getApplication();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        if (mIsUsedSlidingBack) {
//            mFileTemp = new File(CacheManager.getSystemPicCachePath(), WINDOWBITMAP);
            defaultTranslationX = Utils.convertDipOrPx(this, defaultTranslationX);
            shadowWidth = Utils.convertDipOrPx(this, shadowWidth);
            //behindframeLayout
            FrameLayout behindframeLayout = new FrameLayout(this);
            behindImageView = new ImageView(this);
            behindImageView.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
            behindframeLayout.addView(behindImageView, 0);

            //containerLayout
            LinearLayout containerLayout = new LinearLayout(this);
            containerLayout.setOrientation(LinearLayout.HORIZONTAL);
            containerLayout.setBackgroundColor(getResources().getColor(android.R.color.transparent));
            containerLayout.setLayoutParams(new ViewGroup.LayoutParams(getWindowManager().getDefaultDisplay().getWidth() + shadowWidth, ViewGroup.LayoutParams.MATCH_PARENT));
            //you view container
            frameLayout = new FrameLayout(this);
            frameLayout.setBackgroundColor(getResources().getColor(android.R.color.white));
            frameLayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));

            //add shadow
            shadowImageView = new ImageView(this);
            shadowImageView.setBackgroundResource(R.drawable.invengo_logo);
            shadowImageView.setLayoutParams(new LinearLayout.LayoutParams(shadowWidth, LinearLayout.LayoutParams.MATCH_PARENT));
            containerLayout.addView(shadowImageView);
            containerLayout.addView(frameLayout);
            containerLayout.setTranslationX(-shadowWidth);
            //添加两个view
            slidingPaneLayout.addView(behindframeLayout, 0);
            slidingPaneLayout.addView(containerLayout, 1);
        }
    }

    protected abstract int layoutID();

    protected abstract void init(Bundle savedInstanceState);

    protected abstract P createPresenter();

    protected void after() {
//        mUnbinder = ButterKnife.bind(this);
        mBaseHandler = new WeakHandler();
        if (mPresenter == null)
            mPresenter = createPresenter();

        if (mIsNeedAdapterPhone && !isNeedAdapterPhone()) {
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {
                //透明状态栏
                //getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
                //透明导航栏
                //getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            }
        } else {
//            android4Adapter();
        }
        if (mIsNeedGoneNavigationBar) {
            toHideNav();
        }
    }

    protected void data() {

    }

    protected void userToolbar(Toolbar toolbar, View.OnClickListener clickListener) {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            if (TextUtils.isEmpty(toolbar.getTitle())) {
                getSupportActionBar().setTitle(null);
            }
        }
        if (clickListener == null) {
            toolbar.setNavigationOnClickListener(v -> finish());
        } else {
            toolbar.setNavigationOnClickListener(clickListener);
        }
    }

    protected void userToolbar(Toolbar toolbar, String title, View.OnClickListener clickListener) {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

            getSupportActionBar().setTitle(title);
        }
        if (clickListener == null) {
            toolbar.setNavigationOnClickListener(v -> finish());
        } else {
            toolbar.setNavigationOnClickListener(clickListener);
        }
    }

    protected void userToolbar(Toolbar toolbar, int resId, View.OnClickListener clickListener) {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(resId);
        }
        if (clickListener == null) {
            toolbar.setNavigationOnClickListener(v -> finish());
        } else {
            toolbar.setNavigationOnClickListener(clickListener);
        }
    }

    protected void userToolbar(Toolbar toolbar, String title) {
        userToolbar(toolbar, title, null);
    }

    protected void userToolbar(Toolbar toolbar, int resId) {
        userToolbar(toolbar, resId, null);
    }

    protected void userToolbar(Toolbar toolbar) {
        userToolbar(toolbar, null, null);
    }

    protected void setToolbarTitle(String title) {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(title);
        }
    }

    protected void setToolbarTitle(int resId) {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(resId);
        }
    }

    protected void setToolbarTextViewInfo(Toolbar toolbar) {
        Class cls = toolbar.getClass();
        try {
            Field field = cls.getDeclaredField("mTitleTextView");
            field.setAccessible(true);
            TextView tv = (TextView) field.get(toolbar);
            tv.setEllipsize(TextUtils.TruncateAt.MIDDLE);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    protected ActionMenuView getToolbarMenuView(Toolbar toolbar) {
        ActionMenuView view = null;
        Class cls = toolbar.getClass();
        try {
            Field field = cls.getDeclaredField("mMenuView");
            field.setAccessible(true);
            view = (ActionMenuView) field.get(toolbar);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return view;
    }

    /**
     * 设置是否需要适配手机（必须写在before里，默认为true）
     *
     * @return
     */
    protected void setIsNeedAdapterPhone(boolean b) {
        mIsNeedAdapterPhone = b;
    }

    /**
     * 设置是否开启侧滑返回
     *
     * @param b
     */
    protected void setIsUsedSlidingBack(boolean b) {
        mIsUsedSlidingBack = b;
    }

    /**
     * 设置是否需要不显示导航条（必须写子before里，默认为false）
     *
     * @return
     */
    protected void setIsNeedGoneNavigationBar(boolean b) {
        mIsNeedGoneNavigationBar = b;
    }

    /**
     * 获取夜间模式状态
     */
    protected boolean getIsNightTheme() {
        return false;
    }

    /**
     * 是否需要适配手机
     *
     * @return
     */
    protected boolean isNeedAdapterPhone() {
        if (Build.VERSION.SDK_INT > 21 || Build.MODEL.toLowerCase().contains("vivo")) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * android5.0以下的适配
     */
    private void android4Adapter() {
//        CoordinatorLayout coordinatorLayout = (CoordinatorLayout) findViewById(getResources().getIdentifier("coordinatorLayout", "id", getPackageName()));
//        if (coordinatorLayout != null && coordinatorLayout.getChildCount() > 0) {
//            if (coordinatorLayout.getChildAt(0) instanceof AppBarLayout) {
//                AppBarLayout appBarLayout = (AppBarLayout) coordinatorLayout.getChildAt(0);
//                if (appBarLayout.getChildCount() > 0 && appBarLayout.getChildAt(0) instanceof Toolbar) {
//                    Toolbar toolbar = (Toolbar) appBarLayout.getChildAt(0);
//                    AppBarLayout.LayoutParams lp = (AppBarLayout.LayoutParams) toolbar.getLayoutParams();
//                    lp.setMargins(0, Utils.getTop(this), 0, 0);
//                    toolbar.setLayoutParams(lp);
//                }
//            }
//        }
    }

    /**
     * 获取application
     *
     * @return
     */
    protected Appli getAppli() {
        return mAppli;
    }

    /**
     * 获取数据库
     *
     * @return
     */
//    protected DaoSession getmDaoSession() {
//        if (mDaoSession == null) {
//            mDaoSession = getAppli().getDaoSession();
//        }
//        return mDaoSession;
//    }

    /**
     * 获取presenter
     *
     * @return
     */
    protected P getPresenter() {
        return mPresenter;
    }

    protected void setPresenter() {
        if (mPresenter == null)
            mPresenter = createPresenter();
    }

    /**
     * 获取handler
     *
     * @return
     */
    protected WeakHandler getHandler() {
        return mBaseHandler;
    }


    protected void toHideNav() {
        mBaseHandler.post(mHideRunnable);
        final View decorView = getWindow().getDecorView();
        decorView.setOnSystemUiVisibilityChangeListener(visibility -> {
            mBaseHandler.post(mHideRunnable); // hide the navigation bar
        });
    }

    protected void showToast(String content) {
        EToast.get().showToast(getBaseContext(), content);
    }

    protected void toShowNav() {
        mBaseHandler.removeCallbacks(mHideRunnable);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
    }

    protected Runnable mHideRunnable = () -> {
        // must be executed in main thread :)
        getWindow().getDecorView().setSystemUiVisibility(getHideFlags());
    };

    private int getHideFlags() {
        int flags;
        int curApiVersion = Build.VERSION.SDK_INT;
        // This work only for android 4.4+
        if (curApiVersion >= Build.VERSION_CODES.KITKAT) {
            // This work only for android 4.4+
            // hide navigation bar permanently in android activity
            // touch the screen, the navigation bar will not show
            flags = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE;
//                    | View.SYSTEM_UI_FLAG_FULLSCREEN;

        } else {
            // touch the screen, the navigation bar will show
            flags = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
        }
        return flags;
    }

    @Override
    protected void onDestroy() {
        try {
            super.onDestroy();
            if (mBaseHandler != null) {
                mBaseHandler.removeCallbacksAndMessages(null);
                mBaseHandler = null;
            }
//            if (mUnbinder != null) {
//                mUnbinder.unbind();
//            }
            if (mPresenter != null) {
                mPresenter.detachView();
            }
            EToast.get().destory();
//            Glide.get(this).clearMemory();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 判断权限集合
    protected boolean needPermissions(String... permissions) {
        //判断版本是否兼容
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return false;
        }
        boolean isNeed;
        for (String permission : permissions) {
            isNeed = needsPermission(permission);
            if (isNeed) {
                return true;
            }
        }
        return false;
    }

    protected List<String> noPermissions(String... permissions) {
        List<String> list = new ArrayList<>();
        for (String permission : permissions) {
            if (needPermissions(permission)) {
                list.add(permission);
            }
        }
        return list;
    }

    // 判断是否缺少权限
    protected boolean needsPermission(String permission) {
        return ContextCompat.checkSelfPermission(Appli.getContext(), permission) != PackageManager.PERMISSION_GRANTED;
    }

    /**
     * 打开activity
     *
     * @param pClass
     * @param key
     * @param value
     */
    protected void openActivity(Class<?> pClass, String key, Object value) {
        Bundle bundle = new Bundle();
        if (value instanceof String) {
            bundle.putString(key, (String) value);
        } else if (value instanceof Integer) {
            bundle.putInt(key, (Integer) value);
        } else if (value instanceof Boolean) {
            bundle.putBoolean(key, (Boolean) value);
        }
        openActivity(pClass, bundle);
    }

    /**
     * 打开activity
     *
     * @param pClass
     */
    protected void openActivity(Class<?> pClass) {
        openActivity(pClass, null);
    }

    protected void openActivity(Class<?> pClass, Bundle pBundle) {
        Intent intent = new Intent(this, pClass);
        if (pBundle != null) {
            intent.putExtras(pBundle);
        }
        startActivity(intent);
    }

    /**
     * 双击退出。
     */
    protected boolean exitBy2Click() {
        long mNowTime = System.currentTimeMillis();//获取第一次按键时间
        if ((mNowTime - mExitPressedTime) > 2000) {//比较两次按键时间差
            Toast.makeText(this, getString(R.string.nav_back_again_finish), Toast.LENGTH_SHORT).show();
            mExitPressedTime = mNowTime;
        } else {
            RxBus.get().destory();
            finish();
//            DaoUtils.saveSessionFromDBToFile();
//            DaoUtils.saveURLPATHFromDBToFile();
            exitExecute();
            return true;
        }
        return false;
    }

    /**
     * 退出时候执行
     */
    protected void exitExecute() {

    }

    protected RecyclerView.LayoutManager getLayoutManager() {
        return new LinearLayoutManager(getApplicationContext());
    }

    protected RecyclerView.LayoutManager getGridLayoutManager(int count) {
        return new GridLayoutManager(getApplicationContext(), count);
    }

    @Override
    public Resources getResources() {
        Resources res = super.getResources();
        Configuration config = new Configuration();
        config.setToDefaults();
        res.updateConfiguration(config, res.getDisplayMetrics());
        return res;
    }


    @Override
    public void setContentView(int id) {
        if (mIsUsedSlidingBack) {
            setContentView(getLayoutInflater().inflate(id, null));
        } else {
            super.setContentView(id);
        }
    }

    @Override
    public void setContentView(View v) {
        if (mIsUsedSlidingBack) {
            setContentView(v, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            try {
                behindImageView.setScaleType(ImageView.ScaleType.FIT_XY);
                behindImageView.setImageBitmap(getBitmap());
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            super.setContentView(v);
        }
    }

    @Override
    public void setContentView(View v, ViewGroup.LayoutParams params) {
        if (mIsUsedSlidingBack) {
            super.setContentView(slidingPaneLayout, params);
            frameLayout.removeAllViews();
            frameLayout.addView(v, params);
        } else {
            super.setContentView(v, params);
        }
    }


    @Override
    public void onPanelClosed(View view) {

    }

    @Override
    public void onPanelOpened(View view) {
        if (mIsUsedSlidingBack) {
            finish();
            this.overridePendingTransition(0, 0);
        }
    }

    @Override
    public void onPanelSlide(View view, float v) {
        if (mIsUsedSlidingBack) {
//            Utils.log(TAG, "onPanelSlide ：" + v);
            //duang duang duang 你可以在这里加入很多特效
            behindImageView.setTranslationX(v * defaultTranslationX - defaultTranslationX);
            shadowImageView.setAlpha(v < 0.8 ? 1 : (1.5f - v));
        }
    }

    /**
     * 取得视觉差背景图
     *
     * @return
     */
    public Bitmap getBitmap() {
        return null;
//        return BitmapFactory.decodeFile(mFileTemp.getAbsolutePath());
    }

    /**
     * 启动视觉差返回Activity
     *
     * @param activity
     * @param intent
     */
    public void startParallaxSwipeBackActivty(Activity activity, Intent intent) {
        startParallaxSwipeBackActivty(activity, intent, true);
    }

    /**
     * startParallaxSwipeBackActivty
     *
     * @param activity
     * @param intent
     * @param isFullScreen
     */
    public void startParallaxSwipeBackActivty(Activity activity, Intent intent, boolean isFullScreen) {
//        screenshots(activity, isFullScreen, mFileTemp);
//        startActivity(intent);
//        this.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right);
    }
}
