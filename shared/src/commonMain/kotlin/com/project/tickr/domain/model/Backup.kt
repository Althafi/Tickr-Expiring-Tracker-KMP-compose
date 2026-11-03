package com.project.tickr.domain.model

/** Isi file cadangan siap disimpan, beserta nama file yang disarankan. */
data class BackupExport(
    val fileName: String,
    val content: String,
    val itemCount: Int,
)

data class BackupSummary(
    val itemCount: Int,
)
