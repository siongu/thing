package com.common.stdlib.network

import com.orhanobut.logger.Logger
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl

class OkCookieJar : CookieJar {
    private val cookieStore = mutableMapOf<String, MutableList<Cookie>>()
    override fun saveFromResponse(url: HttpUrl, cookies: MutableList<Cookie>) {
        cookieStore[url.host()] = cookies.apply {
            forEach {
                Logger.d("host:${url.host()} Cookie:${it.name()}=${it.value()}")
            }
        }
    }

    override fun loadForRequest(url: HttpUrl): MutableList<Cookie> {
        return cookieStore[url.host()] ?: arrayListOf()
    }
}