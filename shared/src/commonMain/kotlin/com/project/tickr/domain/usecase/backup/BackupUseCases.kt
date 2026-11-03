package com.project.tickr.domain.usecase.backup

import com.project.tickr.core.result.DataResult
import com.project.tickr.domain.model.BackupExport
import com.project.tickr.domain.model.BackupSummary
import com.project.tickr.domain.repository.BackupRepository

class ExportBackupUseCase(private val repository: BackupRepository) {
    suspend operator fun invoke(): DataResult<BackupExport> = repository.export()
}

class ImportBackupUseCase(private val repository: BackupRepository) {
    suspend operator fun invoke(content: String): DataResult<BackupSummary> = repository.import(content)
}
