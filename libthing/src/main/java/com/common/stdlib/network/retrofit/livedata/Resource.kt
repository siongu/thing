package com.common.stdlib.network.retrofit.livedata

data class Resource<T>(
    val status: Status,
    val data: T?,
    val message: String?

) {
    companion object {
        fun <T> success(data: T?): Resource<T> {
            return Resource(Status.SUCCESS, data, null)
        }

        fun <T> error(data: T?, msg: String): Resource<T> {
            return Resource(Status.ERROR, data, msg)
        }

        fun <T> loading(data: T? = null): Resource<T> {
            return Resource(Status.LOADING, data, null)
        }
    }

    fun next(loading: (() -> Unit)? = null, success: (data: T?) -> Unit, error: ((msg: String) -> Unit)? = null) {
        when (status) {
            Status.LOADING -> loading?.invoke()
            Status.SUCCESS -> success(data)
            Status.ERROR -> error?.invoke(message ?: "unknown error")
        }
    }
}

