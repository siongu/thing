package com.common.stdlib.network;


import org.reactivestreams.Publisher;

import io.reactivex.Flowable;
import io.reactivex.FlowableTransformer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by xzw on 2017/6/22.
 */

public class RxTransformer {
    private static final FlowableTransformer TRANSFORMER = new FlowableTransformer() {
        @Override
        public Publisher apply(Flowable upstream) {
            return upstream.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
        }
    };

    public static <T> FlowableTransformer<T, T> applySchedulers() {
        return (FlowableTransformer<T, T>) TRANSFORMER;
    }

}
