package com.impraise.supr.data

sealed class Result<T> {

    class Success<T>(val data: T) : Result<T>()
    class Error<T>(val error: Throwable, val data: T? = null) : Result<T>()

    fun either(success: (T) -> T, error: (Result.Error<T>) -> T): T =
            when (this) {
                is Result.Success -> success(data)
                is Result.Error -> error(this)
            }
}

sealed class PaginatedResult<out T> {

    class Success<out T>(val data: List<T>, val pageDetail: PageDetail) : PaginatedResult<T>()
    class Error<out T>(val error: Throwable, val data: T? = null) : PaginatedResult<T>()
}

data class PageDetail(val hasNextPage: Boolean = false,
                        val totalCount: Int = 0,
                        val pageNumber: Int = 0)