package com.v2x.thing.tkserver.api

import com.common.stdlib.network.retrofit.ApiClient
import com.v2x.thing.model.AuthInfo
import com.v2x.thing.model.AuthorizeParam
import com.v2x.thing.model.LocationInfo
import com.v2x.thing.model.TrackParam
import retrofit2.Call


object DataSource {
    private val service by lazy { ApiClient.createService(Api::class.java) }
    fun authorize(param: AuthorizeParam): Call<AuthInfo> {
        return service.authorize(param)
    }

    fun getLocationInfo(param: TrackParam): Call<LocationInfo> {
        return service.getLocationInfo(param)
    }
}