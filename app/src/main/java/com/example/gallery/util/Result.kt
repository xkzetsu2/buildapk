package com.example.gallery.util

sealed class Result<out T> {
    data class Success<out T>(val value: T) : Result<T>()
    data class Failure(val exception: Exception) : Result<Nothing>()

    companion object {
        fun <T> success(value: T): Result<T> = Success(value)
        fun <T> failure(exception: Exception): Result<T> = Failure(exception)
    }

    fun isSuccess(): Boolean = this is Success<*>
    fun isFailure(): Boolean = this is Failure

    fun getOrNull(): T? = when (this) {
        is Success -> value
        is Failure -> null
    }

    fun getOrElse(defaultValue: T): T = when (this) {
        is Success -> value
        is Failure -> defaultValue
    }

    fun getOrThrow(): T = when (this) {
        is Success -> value
        is Failure -> throw exception
    }

    fun <R> map(transform: (T) -> R): Result<R> = when (this) {
        is Success -> Success(transform(value))
        is Failure -> this
    }

    fun <R> flatMap(transform: (T) -> Result<R>): Result<R> = when (this) {
        is Success -> transform(value)
        is Failure -> this
    }

    fun onSuccess(action: (T) -> Unit): Result<T> {
        if (this is Success) action(value)
        return this
    }

    fun onFailure(action: (Exception) -> Unit): Result<T> {
        if (this is Failure) action(exception)
        return this
    }
}

inline fun <T> Result<T>.fold(
    onSuccess: (T) -> Unit,
    onFailure: (Exception) -> Unit
) {
    when (this) {
        is Success -> onSuccess(value)
        is Failure -> onFailure(exception)
    }
}