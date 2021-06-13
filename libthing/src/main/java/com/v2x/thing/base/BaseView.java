package com.v2x.thing.base;

/**
 * Created by xzw on 2018/3/14.
 */
public interface BaseView {
    default void showLoading() {
    }

    default void hideLoading() {
    }

    default void showError(String msg) {
    }

    default void showToast(String toast) {
    }

}
