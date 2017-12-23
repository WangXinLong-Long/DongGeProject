package com.invengo.base.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.invengo.app.Appli;
import com.invengo.base.presenter.impl.BasePresenterImpl;
import com.invengo.util.ui.WeakHandler;
import com.trello.rxlifecycle2.components.support.RxFragment;


/**
 * 基本Fragment
 * Created by user on 2016/11/4.
 */

public abstract class BaseFragment<P extends BasePresenterImpl> extends RxFragment /*implements Constant, RxConstant */{
    private Appli mAppli;
    private WeakHandler mBaseHandler;//handler
    protected View mFragmentView;
//    private DaoSession mDaoSession;//数据库
//    private Unbinder mUnbinder;//用于butterKnife解绑
    private P mPresenter;//功能调用

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        before();
        if (mFragmentView == null && layoutID() > 0) {
            mFragmentView = inflater.inflate(layoutID(), container, false);
        }
        after(mFragmentView);
        init(mFragmentView, savedInstanceState);
        data();
        return mFragmentView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
//        if (mUnbinder != null) {
//            mUnbinder.unbind();
//        }
        if (mPresenter != null) {
            mPresenter.detachView();
        }
        if (mBaseHandler != null) {
            mBaseHandler.removeCallbacksAndMessages(null);
            mBaseHandler = null;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
//        BaseInfoUtils.addTopActivity(getClass().getName());
    }

    protected void before() {
        mAppli = (Appli) getActivity().getApplication();
    }

    protected void after(View view) {
//        mUnbinder = ButterKnife.bind(this, view);
        mBaseHandler = new WeakHandler();
        mPresenter = createPresenter();
    }

    protected abstract int layoutID();

    protected abstract void init(View view, Bundle savedInstanceState);

    protected abstract P createPresenter();

    protected void data() {

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

    /**
     * 获取handler
     *
     * @return
     */
    protected WeakHandler getHandler() {
        return mBaseHandler;
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
        Intent intent = new Intent(getContext(), pClass);
        if (pBundle != null) {
            intent.putExtras(pBundle);
        }
        startActivity(intent);
    }

    protected void userToolbar(Toolbar toolbar, String title, View.OnClickListener clickListener) {
        getCompatActivity().setSupportActionBar(toolbar);
        if (getCompatActivity().getSupportActionBar() != null) {
            getCompatActivity().getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getCompatActivity().getSupportActionBar().setTitle(title);
        }
        if (clickListener == null) {
            toolbar.setNavigationOnClickListener(v -> getActivity().finish());
        } else {
            toolbar.setNavigationOnClickListener(clickListener);
        }
    }

    protected void closeToolbarBack() {
        if (getCompatActivity().getSupportActionBar() != null) {
            getCompatActivity().getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        }
    }

    protected void userToolbar(Toolbar toolbar, int resId, View.OnClickListener clickListener) {
        getCompatActivity().setSupportActionBar(toolbar);
        if (getCompatActivity().getSupportActionBar() != null) {
            getCompatActivity().getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getCompatActivity().getSupportActionBar().setTitle(resId);
        }
        if (clickListener == null) {
            toolbar.setNavigationOnClickListener(v -> getActivity().finish());
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
        if (getCompatActivity().getSupportActionBar() != null) {
            getCompatActivity().getSupportActionBar().setTitle(title);
        }
    }

    protected void setToolbarTitle(int resId) {
        if (getCompatActivity().getSupportActionBar() != null) {
            getCompatActivity().getSupportActionBar().setTitle(resId);
        }
    }

    protected AppCompatActivity getCompatActivity() {
        return (AppCompatActivity) getActivity();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (hidden) {
            onFragmentHide();
        } else {
            onFragmentShow();
        }
    }

    // FragmentTransaction 调用show 回调
    protected void onFragmentShow() {
        //在BaseActivity 中维护的一个变量，表示当前Activity 显示的Fragment (hide show 方式);
    }

    // FragmentTransaction 调用hide 回调
    protected void onFragmentHide() {

    }
}
