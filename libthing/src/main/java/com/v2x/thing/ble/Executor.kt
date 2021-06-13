package com.v2x.thing.ble

import java.util.concurrent.Executors

private val executor = Executors.newFixedThreadPool(5)

fun execute(task: Runnable) {
    executor.execute(task)
}
