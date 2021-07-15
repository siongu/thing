/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.common.stdlib.network.retrofit.livedata


import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import kotlinx.coroutines.Dispatchers
import retrofit2.Call
import retrofit2.CallAdapter
import java.io.IOException
import java.lang.reflect.Type

/**
 * A Retrofit adapter that converts the Call into a LiveData of ApiResponse.
 * @param <R>
</R> */
class LiveDataCallAdapter<R>(private val responseType: Type) :
    CallAdapter<R, LiveData<Resource<R>>> {

    override fun responseType() = responseType

    override fun adapt(call: Call<R>): LiveData<Resource<R>> {
        return liveData(Dispatchers.IO) {
            try {
                println("execute call in the thread:${Thread.currentThread().name}")
                val response = call.execute()
                when (ApiResponse.create(response)) {
                    is ApiSuccessResponse,
                    is ApiEmptyResponse
                    -> {
                        emit(Resource.success(response.body()))
                    }
                    is ApiErrorResponse -> {
                        emit(Resource.error(response.body(), response.message()))
                    }
                }
            } catch (e: IOException) {
                e.printStackTrace()
                emit(Resource.error(null as R?, msg = e.message ?: "unknown error"))
            }
        }
    }
}
