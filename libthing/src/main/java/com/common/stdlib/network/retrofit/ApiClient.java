package com.common.stdlib.network.retrofit;

import android.text.TextUtils;

import com.common.stdlib.network.Domain;
import com.common.stdlib.network.retrofit.livedata.LiveDataCallAdapterFactory;

import java.util.WeakHashMap;

import okhttp3.CookieJar;
import okhttp3.Interceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by xzw on 2017/6/21.
 */
public class ApiClient {
    private static Retrofit retrofit;
    private static Retrofit.Builder builder = new Retrofit.Builder();
    private static WeakHashMap<String, Object> serviceCache = new WeakHashMap<>();

    static {
        builder.addConverterFactory(GsonConverterFactory.create())
//                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
//                .addCallAdapterFactory(LiveDataCallAdapterFactory.create())
                .client(HttpClient.client());
        if (!TextUtils.isEmpty(Domain.baseUrl())) {
            builder.baseUrl(Domain.baseUrl());
        }
        retrofit = builder.build();
    }

    public static void setCookieJar(CookieJar cookieJar) {
        if (cookieJar != null) {
            retrofit = builder.client(HttpClient.client().newBuilder().cookieJar(cookieJar).build()).build();
        }
    }

    public static void addInterceptor(Interceptor interceptor) {
        if (interceptor == null) return;
        addInterceptor(-1, interceptor);
    }

    public static void addInterceptor(int index, Interceptor interceptor) {
        if (interceptor == null) return;
        HttpClient.addInterceptor(index, interceptor);
        retrofit = builder.client(HttpClient.client()).build();
        serviceCache.clear();
    }

    public static <T> T createService(Class<T> clazz) {
        T service;
        if (serviceCache.containsKey(clazz.getName())) {
            service = (T) serviceCache.get(clazz.getName());
        } else {
            service = retrofit.create(clazz);
            serviceCache.put(clazz.getName(), service);
        }
        return service;
    }
}
