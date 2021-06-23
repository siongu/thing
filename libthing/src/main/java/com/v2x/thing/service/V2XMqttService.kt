package com.v2x.thing.service

import android.util.Log

/**
 */
@Deprecated("")
class V2XMqttService {
//    override fun onCreate() {
//        isCreate = true
//        super.onCreate()
//    }

    companion object {
        private var isCreate = false
        private var mqConfigFactory: MqttConfigFactory = DefaultMqttConfigFactory()
        fun setMqConfigFactory(factory: MqttConfigFactory) {
            if (isCreate) {
                Log.w(
                    "V2XMqttService",
                    "setting 'mqConfigFactory' after starting service will not be available. "
                )
            } else {
                mqConfigFactory = factory
            }
        }
    }

//    override fun onCreateFactory(): MqttConfigFactory {
//        return mqConfigFactory
//    }
}