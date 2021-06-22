package com.v2x.thing.service

import android.app.Service
import android.content.Context
import android.content.Context.CONNECTIVITY_SERVICE
import android.content.Intent
import android.net.ConnectivityManager
import android.os.Binder
import android.os.IBinder
import android.util.Log
import org.eclipse.paho.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.*
import java.util.concurrent.ScheduledThreadPoolExecutor
import java.util.concurrent.TimeUnit

class MqttManager private constructor(private val context: Context, builder: Builder) {
    private var factory: MqttConfigFactory
    private var dispatchers: List<MqttDispatcher>
    private var mqttAndroidClient: MqttAndroidClient? = null
    private var mMqttConnectOptions: MqttConnectOptions? = null
    private lateinit var info: MqttConfig
    private val tag = this::class.simpleName
    private val lock = Any()
    private val scheduledPool = ScheduledThreadPoolExecutor(1)

    init {
        factory = builder.factory
        dispatchers = builder.dispatchers
        init()
    }

    class Builder(private val context: Context) {
        lateinit var factory: MqttConfigFactory
        val dispatchers = mutableListOf<MqttDispatcher>()
        fun factory(factory: MqttConfigFactory): Builder {
            this.factory = factory
            return this
        }

        fun addDispatcher(dispatcher: MqttDispatcher): Builder {
            dispatchers.add(dispatcher)
            return this
        }

        fun build(): MqttManager {
            return MqttManager(context, this)
        }
    }

    private fun connectMqtt() {
        doConnectAsync().run {
            scheduledPool.execute(this)
        }
    }

    private fun doConnectAsync(): Runnable {
        return Runnable {
            try {
                doClientConnection()
                println("connecting...")
            } catch (e: Exception) {
                println("connecting fail!,Exception=${e.message} ")
                e.printStackTrace()
            }
        }
    }

    /**
     * 初始化
     */
    private fun init() {
        info = factory.create()
        mqttAndroidClient = MqttAndroidClient(context, info.serverUri, info.clientId)
        mqttAndroidClient?.setCallback(mqttCallback) //设置监听订阅消息的回调
        mMqttConnectOptions = MqttConnectOptions().apply {
            isCleanSession = true //设置是否清除缓存
            connectionTimeout = 10 //设置超时时间，单位：秒
            keepAliveInterval = 20 //设置心跳包发送间隔，单位：秒
            userName = info.userName //设置用户名
            password = info.password.toCharArray() //设置密码
        }
        val message = "{\"terminal_uid\":\"${info.clientId}\"}"
        val topics = info.publishTopics
        val qos = 2
        val retained = false
        if (message != "" || !topics.isNullOrEmpty()) {
            // 最后的遗嘱
            try {
                topics.forEach { topic ->
                    mMqttConnectOptions?.setWill(topic, message.toByteArray(), qos, retained)
                }
            } catch (e: Exception) {
                Log.i(tag, "Exception Occured", e)
                iMqttActionListener.onFailure(null, e)
            }
        }
    }

    /**
     * 连接MQTT服务器
     */
    private fun doClientConnection() {
        if (!mqttAndroidClient!!.isConnected && isConnectIsNormal) {
            try {
                mqttAndroidClient?.connect(mMqttConnectOptions, null, iMqttActionListener)
            } catch (e: MqttException) {
                e.printStackTrace()
            }
        }
    }

    /**
     * 判断网络是否连接
     */
    private val isConnectIsNormal: Boolean
        get() {
            val connectivityManager =
                context.getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
            val info = connectivityManager.activeNetworkInfo
            return if (info != null && info.isAvailable) {
                val name = info.typeName
                Log.i(tag, "当前网络名称：$name")
                true
            } else {
                Log.i(tag, "没有可用网络")
                /*
                *没有可用网络的时候，延迟3秒再尝试重连
                */
                doConnectAsync().run {
                    scheduledPool.schedule(this, 3000, TimeUnit.MILLISECONDS)
                }
                false
            }
        }

    //MQTT是否连接成功的监听
    private val iMqttActionListener: IMqttActionListener = object : IMqttActionListener {
        override fun onSuccess(arg0: IMqttToken) {
            Log.i(tag, "连接成功 ")
            try {
                info.publishTopics.forEach { topic ->
                    mqttAndroidClient?.subscribe(topic, 2) //订阅主题，参数：主题、服务质量
                }
            } catch (e: MqttException) {
                e.printStackTrace()
            }
        }

        override fun onFailure(arg0: IMqttToken?, arg1: Throwable?) {
            arg1?.printStackTrace()
            Log.i(tag, "连接失败 ")
            doClientConnection() //连接失败，重连（可关闭服务器进行模拟）
        }
    }

    //订阅主题的回调
    private val mqttCallback: MqttCallback = object : MqttCallback {
        @Throws(Exception::class)
        override fun messageArrived(topic: String, message: MqttMessage) {
            val msg = String(message.payload)
            Log.i(tag, "收到消息：topic:$topic, msg: $msg")
            dispatchers.forEach { dispatcher ->
                val filterTopics = dispatcher.filterTopics()
                if (filterTopics != null) {
                    if (filterTopics.contains(topic))
                        dispatcher.dispatchMqttMessage(topic, msg)
                } else {
                    dispatcher.dispatchMqttMessage(topic, msg)
                }
            }
        }

        override fun deliveryComplete(arg0: IMqttDeliveryToken) {}
        override fun connectionLost(arg0: Throwable?) {
            arg0?.printStackTrace()
            Log.i(tag, "连接断开 ")
            doClientConnection() //连接断开，重连
        }
    }

    fun start() {
        connectMqtt()
    }

    private fun close() {
        try {
            mqttAndroidClient?.disconnect()//断开连接
            mqttAndroidClient?.unregisterResources()
            mqttAndroidClient?.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

data class MqttConfig(
    val serverUri: String,
    val clientId: String,
    val publishTopics: List<String>,
    val userName: String,
    val password: String

)
