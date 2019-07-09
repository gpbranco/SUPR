package com.impraise.supr.data

sealed class Result<T> {

    class Success<T>(val data: T) : Result<T>()
    class Error<T>(val error: Throwable, val data: T? = null) : Result<T>()
}

sealed class PaginatedResult<out T> {

    class Success<out T>(val data: List<T>, val pageDetail: PageDetail) : PaginatedResult<T>()
    class Error<out T>(val error: Throwable, val data: T? = null) : PaginatedResult<T>()
}

data class PageDetail(val hasNextPage: Boolean = false,
                        val totalCount: Int = 0,
                        val pageNumber: Int = 0)

fun <R, T> Result<T>.either(
        onSuccess: (data: T) -> R,
        onError: (error: Throwable) -> R
): R {
    return when (this) {
        is Result.Success -> onSuccess(data)
        is Result.Error -> onError(error)
    }
}

fun <T> Result<T>.getOrDefault(
        default: () -> T
): T {
    return when (this) {
        is Result.Success -> this.data
        is Result.Error -> default()
    }
}

fun <R, T> PaginatedResult<T>.either(
        onSuccess: (data: PaginatedResult.Success<T>) -> R,
        onError: (error: Throwable) -> R
): R {
    return when (this) {
        is PaginatedResult.Success -> onSuccess(this)
        is PaginatedResult.Error -> onError(error)
    }
}