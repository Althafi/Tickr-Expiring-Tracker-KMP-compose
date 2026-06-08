package com.project.tickr.core.validation

import com.project.tickr.core.result.AppError

object Validators {

    fun requireNotBlank(value: String?, field: String): AppError.Validation? =
        if (value.isNullOrBlank()) AppError.Validation(field, "must not be empty") else null

    fun requireMaxLength(value: String?, max: Int, field: String): AppError.Validation? =
        if ((value?.length ?: 0) > max) AppError.Validation(field, "max $max chars") else null

    fun requireIsoDate(value: String?, field: String): AppError.Validation? {
        val regex = Regex("""\d{4}-\d{2}-\d{2}""")
        return if (value == null || !regex.matches(value))
            AppError.Validation(field, "must be yyyy-MM-dd") else null
    }

    fun requireHexColor(value: String?, field: String): AppError.Validation? {
        val regex = Regex("""#([0-9a-fA-F]{6}|[0-9a-fA-F]{8})""")
        return if (value == null || !regex.matches(value))
            AppError.Validation(field, "must be #RRGGBB or #AARRGGBB") else null
    }

    fun requireEmail(value: String?, field: String): AppError.Validation? {
        val regex = Regex("""^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$""")
        return if (value == null || !regex.matches(value))
            AppError.Validation(field, "must be a valid email") else null
    }

    fun requireMinLength(value: String?, min: Int, field: String): AppError.Validation? =
        if ((value?.length ?: 0) < min) AppError.Validation(field, "minimum $min characters") else null
}
