package com.common.stdlib.util

fun String?.notNull(notNullValue: String = ""): String {
    return this ?: notNullValue
}