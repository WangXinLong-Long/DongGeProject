package com.invengo.base.presenter.impl;

import android.content.Context;
import android.support.annotation.NonNull;
import android.widget.Toast;


import com.invengo.base.view.BaseView;
import com.invengo.util.ui.EToast;

import java.lang.ref.WeakReference;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;


/**
 * Created by user on 2016/11/4.
 */

public abstract class BasePresenterImpl<V extends BaseView> implements RxConstant {
    private V view;
    private Context mContext;
    private Toast toast = null;
    private long oneTime;
    private String oldMsg;

    private WeakReference<CompositeDisposable> mCompositeDisposable;

    public BasePresenterImpl(@NonNull Context context, @NonNull V view) {
        this.view = view;
        this.mContext = context;
        mCompositeDisposable = new WeakReference<>(new CompositeDisposable());
    }

    protected V getView() {
        return view;
    }

    protected Context getContext() {
        return mContext;
    }

    public void detachView() {
        this.view = null;
        onUnsubscribe();
    }

    //订阅
    protected void addSubscription(Disposable disposable) {
        if (mCompositeDisposable == null || mCompositeDisposable.get() == null) {
            mCompositeDisposable = new WeakReference<>(new CompositeDisposable());
        }
        mCompositeDisposable.get().add(disposable);
    }

    //RXjava取消注册，以避免内存泄露
    private void onUnsubscribe() {
        if (mCompositeDisposable != null && mCompositeDisposable.get() != null && !mCompositeDisposable.get().isDisposed()) {
            mCompositeDisposable.get().dispose();
            mCompositeDisposable.clear();
        }
        mCompositeDisposable = null;
        mContext = null;
    }

    protected void showToast(int resId) {
        if (getContext() == null) {
            return;
        }
        String s = getContext().getResources().getString(resId);
        showToast(s);
    }

    protected void showToast(String s) {
        EToast.get().showToast(mContext,s);
    }

}
