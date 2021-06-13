package com.common.stdlib.network

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.reactivex.Flowable
import io.reactivex.FlowableTransformer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

fun <T> Flowable<T>.compose(transformer: FlowableTransformer<T, T> = RxTransformer.applySchedulers()): Flowable<T> {
    return this.compose(transformer)
}

fun ViewModel.launch(context: CoroutineContext = EmptyCoroutineContext, block: suspend CoroutineScope.() -> Unit) {
    viewModelScope.launch(context = context) {
        println("launch thread:${Thread.currentThread().name}")
        block()
    }
}