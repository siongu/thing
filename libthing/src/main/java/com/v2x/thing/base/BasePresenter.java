package com.v2x.thing.base;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;

/**
 * Created by xzw on 2018/3/14.
 */
public abstract class BasePresenter<V extends BaseView> {
    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    public final void addDisposable(Disposable... disposable) {
        compositeDisposable.addAll(disposable);
    }

    public final void clear() {
        compositeDisposable.clear();
    }

    private V v;

    public V getView() {
        return v;
    }

    public abstract void unSubscribe();

    public void attachView(V v) {
        this.v = v;
    }

    public void onCreate() {
    }

    public void detachView() {
        v = null;
    }

}
