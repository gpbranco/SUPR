package com.impraise.supr.data

sealed class ResultList<T> {

    class Success<T>(val data: List<T>) : ResultList<T>()
    class Error<T>(val error: Throwable, val data: T? = null) : ResultList<T>()
}

fun <R, T> ResultList<T>.either(
        onSuccess: (data: List<T>) -> R,
        onError: (error: Throwable) -> R
): R {
    return when (this) {
        is ResultList.Success -> onSuccess(data)
        is ResultList.Error -> onError(error)
    }
}

fun <T> ResultList<T>.getOrDefault(
        default: () -> List<T>
): List<T> {
    return when (this) {
        is ResultList.Success -> this.data
        is ResultList.Error -> default()
    }
}