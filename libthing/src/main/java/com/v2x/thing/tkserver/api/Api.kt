package com.v2x.thing.tkserver.api

import com.v2x.thing.model.AuthInfo
import com.v2x.thing.model.AuthorizeParam
import com.v2x.thing.model.LocationInfo
import com.v2x.thing.model.TrackParam
import com.v2x.thing.tkserver.Config
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface Api {
    @POST("${Config.BASE_URL}Api/User/Authorize")
    fun authorize(@Body param: AuthorizeParam): Call<AuthInfo>

    @POST("${Config.BASE_URL}Api/Location/Tracking")
    fun getLocationInfo(@Body param: TrackParam): Call<LocationInfo>

}