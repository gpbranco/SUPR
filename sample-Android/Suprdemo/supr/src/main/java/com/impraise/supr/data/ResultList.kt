package com.impraise.supr.data

sealed class ResultList<T> {

    class Success<T>(val data: List<T>) : ResultList<T>()
    class Error<T>(val error: Throwable, val data: T? = null) : ResultList<T>()

    fun either(success: (List<T>) -> List<T>, error: (ResultList.Error<T>) -> List<T>): List<T> =
            when (this) {
                is Success -> success(data)
                is Error -> error(this)
            }
}