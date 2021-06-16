package com.v2x.thing.tkserver

import com.common.stdlib.network.Domain
import com.common.stdlib.storage.KV
import com.v2x.thing.handleOnUiThread
import com.v2x.thing.model.AuthorizeParam
import com.v2x.thing.model.LocationInfo
import com.v2x.thing.model.TrackParam
import com.v2x.thing.tkserver.api.DataSource
import com.v2x.thing.toJson
import java.util.concurrent.ScheduledThreadPoolExecutor
import java.util.concurrent.TimeUnit

class TkServer private constructor() {
    companion object {
        val INSTANCE by lazy { TkServer() }
    }

    init {
        Domain.setBaseUrl(Config.DEFAULT_BASE_URL)
    }

    private var schedulePool: ScheduledThreadPoolExecutor? = null
    private var onReceiveMessageListener: OnReceiveMessageListener? = null
    fun startExecutorService() {
        if (schedulePool == null) schedulePool = ScheduledThreadPoolExecutor(1)
        schedulePool?.scheduleAtFixedRate({
            if (getToken())
                getLocationInfo()
        }, 100, 1000, TimeUnit.MILLISECONDS)
    }

    private fun getToken(isExpired: Boolean = false): Boolean {
        var isSuccess = false
        val token = KV.decodeString("token")
        if (!isExpired) {
            println("accessToken:$token")
            if (!token.isNullOrBlank())
                return true
        } else {
            println("accessToken已过期:$token")
        }
        try {
            val tokenCall = DataSource.authorize(AuthorizeParam())
            val res = tokenCall.execute()
            if (res.isSuccessful) {
                val body = res.body()
                println("getToken success:${body.toJson()}")
                body?.apply {
                    when (State) {
                        0 -> {
                            isSuccess = true
                            val accessToken = AccessToken
                            saveToken(accessToken)
                            println("accessToken update:$accessToken")
                        }
                        1000 -> {
                            isSuccess = false
                            handleOnUiThread {
                                onReceiveMessageListener?.onReceiveError(
                                    State,
                                    "AppKey 或者 AppSecret 无效"
                                )
                            }
                        }
                    }
                }
            } else {
                println("getToken error:${res.errorBody().toJson()}")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return isSuccess
    }

    private fun saveToken(token: String) {
        KV.encode("token", token)
    }

    private fun getLocationInfo() {
        val param = TrackParam(Token = KV.decodeString("token"))
        try {
            val call = DataSource.getLocationInfo(param)
            val loc = call.execute()
            if (loc.isSuccessful) {
                val info = loc.body()
                println("received locationInfo:${info.toJson()}")
                info?.apply {
                    when (State) {
                        0 -> onReceiveMessageListener?.onReceiveMessage(this) // 成功
                        2001 -> handleOnUiThread {
                            onReceiveMessageListener?.onReceiveError(State, "无数据")
                        }
                        2002 -> handleOnUiThread {
                            onReceiveMessageListener?.onReceiveError(State, "参数错误")
                        }
                        6000 -> {
                            handleOnUiThread {
                                onReceiveMessageListener?.onReceiveError(State, "Token错误或已过期")
                            }
                            getToken(true)
                        }
                    }
                }
            } else {
                println("getLocationInfo error:${loc.errorBody().toJson()}")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun setOnReceiveMessageListener(onReceiveMessageListener: OnReceiveMessageListener) {
        this.onReceiveMessageListener = onReceiveMessageListener
    }

    fun onDestroy() {
        schedulePool?.shutdown()
        schedulePool = null
    }

    interface OnReceiveMessageListener {
        fun onReceiveMessage(locationInfo: LocationInfo)
        fun onReceiveError(errorCode: Int, msg: String) {}
    }
}