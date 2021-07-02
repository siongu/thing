package com.v2x.thing.service

import android.content.Context
import android.content.Context.CONNECTIVITY_SERVICE
import android.net.ConnectivityManager
import android.util.Log
import org.eclipse.paho.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.*
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class MqttManager private constructor(
    private val context: Context,
    private val configFactory: MqttConfigFactory,
    private val dispatchers: List<MqttDispatcher>
) {
    private val TAG = this.javaClass.simpleName
    private var mqttAndroidClient: MqttAndroidClient? = null
    private var mMqttConnectOptions: MqttConnectOptions? = null
    private val qos = 0
    private lateinit var config: MqttConfig
    private val tag = this::class.simpleName
    private val scheduledPool = Executors.newScheduledThreadPool(1)

    class Builder(private val context: Context) {
        private lateinit var configFactory: MqttConfigFactory
        private val dispatchers = mutableListOf<MqttDispatcher>()
        fun configure(configFactory: MqttConfigFactory): Builder {
            this.configFactory = configFactory
            return this
        }

        fun addDispatcher(dispatcher: MqttDispatcher): Builder {
            dispatchers.add(dispatcher)
            return this
        }

        fun addDispatchers(vararg dispatcher: MqttDispatcher): Builder {
            dispatchers.addAll(dispatcher)
            return this
        }

        fun addDispatchers(dispatchers: List<MqttDispatcher>): Builder {
            this.dispatchers.addAll(dispatchers)
            return this
        }

        fun build(): MqttManager {
            return MqttManager(context, configFactory, dispatchers)
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
        config = configFactory.create()
        Log.d(TAG, "info: $config")
        mqttAndroidClient = MqttAndroidClient(context, config.serverUri, config.clientId)
        mqttAndroidClient?.setCallback(mqttCallback) //设置监听订阅消息的回调
        mMqttConnectOptions = MqttConnectOptions().apply {
            isCleanSession = config.isCleanSession //设置是否清除缓存
            connectionTimeout = config.connectionTimeout //设置超时时间，单位：秒
            keepAliveInterval = config.keepAliveInterval //设置心跳包发送间隔，单位：秒
            userName = config.userName //设置用户名
            password = config.password.toCharArray() //设置密码
        }
        val message = "{\"terminal_uid\":\"${config.clientId}\"}"
        val topics = config.subscribeTopics
        val retained = false
        if (message != "" || !topics.isNullOrEmpty()) {
            // 最后的遗嘱
            try {
                topics.forEach { topic ->
                    mMqttConnectOptions?.setWill(topic, message.toByteArray(), qos, retained)
                }
            } catch (e: Exception) {
                Log.i(tag, "Exception occurred", e)
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
            Log.i(tag, "连接成功:$config")
            try {
                config.subscribeTopics.let { topics ->
                    val qoss = IntArray(topics.size) { qos }
                    mqttAndroidClient?.subscribe(topics, qoss) //订阅主题，参数：主题、服务质量
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
            Log.d(tag, "收到消息：topic:$topic, msg: $msg")
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

    fun connect() {
        init()
        connectMqtt()
    }

    fun publish(topic: String? = null, message: MqttMessage): IMqttDeliveryToken? {
        val publishTopic = checkPublishTopic(topic)
        try {
            return mqttAndroidClient?.publish(publishTopic, message)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    private fun checkPublishTopic(topic: String?): String? {
        val publishTopic = topic ?: config.publishTopic
        Log.d(TAG, "publishTopic:$publishTopic")
        return publishTopic
    }

    fun publish(
        topic: String? = null,
        message: MqttMessage,
        userContext: Any,
        callback: IMqttActionListener
    ): IMqttDeliveryToken? {
        val publishTopic = checkPublishTopic(topic)
        try {
            return mqttAndroidClient?.publish(publishTopic, message, userContext, callback)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    fun publish(
        topic: String? = null,
        payload: ByteArray?,
        qos: Int,
        retained: Boolean
    ): IMqttDeliveryToken? {
        val publishTopic = checkPublishTopic(topic)
        try {
            return mqttAndroidClient?.publish(publishTopic, payload, qos, retained)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    fun publish(
        topic: String? = null,
        payload: ByteArray?,
        qos: Int,
        retained: Boolean,
        userContext: Any,
        callback: IMqttActionListener
    ): IMqttDeliveryToken? {
        val publishTopic = checkPublishTopic(topic)
        try {
            return mqttAndroidClient?.publish(
                publishTopic,
                payload,
                qos,
                retained,
                userContext,
                callback
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }


    fun close() {
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
    val subscribeTopics: Array<String>,
    val publishTopic: String? = null,
    val userName: String,
    val password: String,
    val isCleanSession: Boolean = true,
    val connectionTimeout: Int = 10,
    val keepAliveInterval: Int = 20
)
