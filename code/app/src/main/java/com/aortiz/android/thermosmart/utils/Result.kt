package com.aortiz.android.thermosmart.utils

enum class ERROR {
    UNKNOWN,
    INVALID_USER,
    INVALID_DEVICE,
    ALREADY_FOLLOWING,
    NOT_FOLLOWING
}

sealed class OperationResult<out R> {
    data class Success<out T>(val data: T) : OperationResult<T>()
    data class Error(val exception: Exception, val code: ERROR = ERROR.UNKNOWN) :
        OperationResult<Nothing>()
}
