package com.project.tickr.presentation.common

import com.project.tickr.core.result.AppError
import tickrapp.shared.generated.resources.Res
import tickrapp.shared.generated.resources.error_generic
import tickrapp.shared.generated.resources.error_invalid_data
import tickrapp.shared.generated.resources.error_not_found
import tickrapp.shared.generated.resources.error_backup_invalid
import tickrapp.shared.generated.resources.error_backup_newer
import tickrapp.shared.generated.resources.error_validation_expiry
import tickrapp.shared.generated.resources.error_validation_name
import tickrapp.shared.generated.resources.error_validation_quantity

/** Memetakan [AppError] (pesan developer) menjadi teks untuk pengguna. */
class ErrorMessageProvider {
    fun provide(error: AppError): UiText = when (error) {
        is AppError.NotFound -> UiText.Res(Res.string.error_not_found)
        is AppError.Validation -> when (error.field) {
            "backup" -> UiText.Res(Res.string.error_backup_invalid)
            "backup_version" -> UiText.Res(Res.string.error_backup_newer)
            "name" -> UiText.Res(Res.string.error_validation_name)
            "expiry_date" -> UiText.Res(Res.string.error_validation_expiry)
            "quantity" -> UiText.Res(Res.string.error_validation_quantity)
            else -> UiText.Res(Res.string.error_invalid_data)
        }
        is AppError.Unknown -> UiText.Res(Res.string.error_generic)
    }
}
