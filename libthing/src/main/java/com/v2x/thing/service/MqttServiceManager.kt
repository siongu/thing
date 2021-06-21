package com.v2x.thing.service

import android.content.ComponentName
import android.content.Context
import android.content.Context.BIND_AUTO_CREATE
import android.content.Intent
import android.os.IBinder
import androidx.collection.arrayMapOf

class MqttServiceManager(private val context: Context) {
    private val conns = arrayMapOf<Any, MutableList<MqttServiceConnection>>()
    private val defaultKey = Any()

    internal class MqttServiceConnectionImpl(
        val dispatcher: MqttDispatcher?,
    ) : MqttServiceConnection {
        private lateinit var dispatcherHandler: IMqttDispatcherHandler
        override fun removeDispatcher(dispatcher: MqttDispatcher) {
            dispatcherHandler.unRegister(dispatcher)
        }

        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            println("connect to mqttService.")
            dispatcherHandler = service as IMqttDispatcherHandler
            dispatcher?.apply { dispatcherHandler.register(this) }

        }

        override fun onServiceDisconnected(name: ComponentName?) {
            dispatcher?.apply { removeDispatcher(this) }
        }
    }

    fun bindMqttService(dispatcher: MqttDispatcher?) {
        MqttServiceConnectionImpl(dispatcher).let { conn ->
            conns[defaultKey] = conns[defaultKey]
                ?: mutableListOf<MqttServiceConnection>().apply {
                    conns[defaultKey] = this
                }
            conns[defaultKey]?.add(conn)
            context.bindService(
                Intent(context, V2XMqttService::class.java),
                conn,
                BIND_AUTO_CREATE
            )
        }
    }

    fun unbindMqttService() {
        conns[defaultKey]?.onEach {
            context.unbindService(it)
            (it as MqttServiceConnectionImpl).dispatcher?.apply {
                it.removeDispatcher(this)
            }
        }
    }
}