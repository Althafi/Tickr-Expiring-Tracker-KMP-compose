package com.project.tickr.presentation.onboarding

data class OnboardingUiState(
    val currentPage: Int = 0,
    val pages: List<OnboardingPage> = defaultPages,
    val isLastPage: Boolean = false
) {
    companion object {
        val defaultPages = listOf(
            OnboardingPage(
                title = "Yuk, kelola bahan makanan, kosmetik, hingga stok tokomu dengan lebih pintar. Katakan selamat tinggal pada barang kedaluwarsa!",
                imageResId = "ill_onboarding_welcome",
                contentDescription = "cd_onboarding_welcome"
            ),
            OnboardingPage(
                title = "Satu catatan kecil darimu, langkah besar untuk kurangi sampah dan hemat isi dompet. Pantau semua tanggal kedaluwarsa dengan mudah.",
                imageResId = "ill_onboarding_eco",
                contentDescription = "cd_onboarding_eco"
            ),
            OnboardingPage(
                title = "Terima notifikasi sebelum terlambat. Nikmati kemudahan melacak tanpa takut kecolongan lagi. Mulai sekarang!",
                imageResId = "ill_onboarding_notify",
                contentDescription = "cd_onboarding_notify"
            )
        )
    }
}

data class OnboardingPage(
    val title: String,
    val imageResId: String,
    val contentDescription: String
)
