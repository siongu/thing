package com.common.stdlib.network;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

/**
 * Created by xzw on 2017/6/22.
 */

public abstract class AbsObserver<T> implements Observer<T> {
    @Override
    public void onSubscribe(Disposable d) {

    }

    @Override
    public void onNext(T t) {
        onSuccess(t);
    }

    @Override
    public void onError(Throwable e) {
        onFailure(e);
    }

    @Override
    public void onComplete() {

    }

    public abstract void onSuccess(T response);

    public abstract void onFailure(Throwable e);

}
