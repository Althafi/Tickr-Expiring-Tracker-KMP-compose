package com.project.tickr.presentation.onboarding

import org.jetbrains.compose.resources.StringResource
import tickrapp.shared.generated.resources.*

data class OnboardingUiState(
    val currentPage: Int = 0,
    val pages: List<OnboardingPage> = defaultPages,
    val isLastPage: Boolean = false
) {
    companion object {
        val defaultPages = listOf(
            OnboardingPage(
                title = Res.string.onboarding_title_1,
                imageResId = "ill_onboarding_welcome",
                contentDescription = Res.string.cd_onboarding_welcome
            ),
            OnboardingPage(
                title = Res.string.onboarding_title_2,
                imageResId = "ill_onboarding_eco",
                contentDescription = Res.string.cd_onboarding_eco
            ),
            OnboardingPage(
                title = Res.string.onboarding_title_3,
                imageResId = "ill_onboarding_notify",
                contentDescription = Res.string.cd_onboarding_notify
            )
        )
    }
}

data class OnboardingPage(
    val title: StringResource,
    val imageResId: String,
    val contentDescription: StringResource
)
