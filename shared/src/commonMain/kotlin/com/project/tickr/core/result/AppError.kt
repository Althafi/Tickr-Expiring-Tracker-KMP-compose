package com.project.tickr.core.result

sealed class AppError(open val message: String) {
    data class NotFound(val entity: String) : AppError("$entity not found")
    data class Validation(val field: String, val reason: String) : AppError("Validation failed on '$field': $reason")
    data class Unknown(val detail: String) : AppError(detail)
}
