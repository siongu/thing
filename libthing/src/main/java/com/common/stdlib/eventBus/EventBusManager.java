package com.common.stdlib.eventBus;

import org.greenrobot.eventbus.EventBus;

/**
 * Created by xzw on 16/9/23.
 */

public class EventBusManager {

    public static void register(Object subscriber) {
        if (!isRegistered(subscriber)) {
            EventBus.getDefault().register(subscriber);
        }
    }

    public static boolean isRegistered(Object subscriber) {
        return EventBus.getDefault().isRegistered(subscriber);
    }

    public static void unregister(Object subscriber) {
        EventBus.getDefault().unregister(subscriber);
    }

    public static void post(Object event) {
        EventBus.getDefault().post(event);
    }
}
