package com.project.tickr.ui.screen.onboarding

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.delay
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.project.tickr.presentation.onboarding.OnboardingAction
import com.project.tickr.presentation.onboarding.OnboardingPage
import com.project.tickr.presentation.onboarding.OnboardingUiState
import com.project.tickr.ui.theme.TickrCornerRadius
import com.project.tickr.ui.theme.TickrTheme
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import tickrapp.shared.generated.resources.Res
import tickrapp.shared.generated.resources.ic_arrow_right
import tickrapp.shared.generated.resources.splashscreen_1
import tickrapp.shared.generated.resources.splashscreen_2
import tickrapp.shared.generated.resources.splashscreen_3

private val pageImages: List<DrawableResource> = listOf(
    Res.drawable.splashscreen_1,
    Res.drawable.splashscreen_2,
    Res.drawable.splashscreen_3,
)

@Composable
fun OnboardingScreen(
    state: OnboardingUiState,
    onAction: (OnboardingAction) -> Unit,
) {
    val pagerState = rememberPagerState(initialPage = state.currentPage) { state.pages.size }
    val isLastPage = pagerState.currentPage == state.pages.lastIndex

    // Swipe pager → sync ke ViewModel
    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }.collect { page ->
            onAction(OnboardingAction.PageChanged(page))
        }
    }

    // ViewModel Next/PageChanged → scroll pager (animasi slide)
    LaunchedEffect(state.currentPage) {
        if (pagerState.currentPage != state.currentPage) {
            pagerState.animateScrollToPage(state.currentPage)
        }
    }

    // Auto-slide setiap 2 detik; timer mulai hanya setelah animasi selesai (settledPage)
    LaunchedEffect(pagerState.settledPage) {
        delay(2.seconds)
        val next = (pagerState.settledPage + 1) % state.pages.size
        pagerState.animateScrollToPage(next)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(TickrTheme.colors.background)
    ) {
        DecorativeBlobs()

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Illustration pager — slide animation bawaan HorizontalPager
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
            ) { pageIndex ->
                OnboardingPageItem(
                    page = state.pages[pageIndex],
                    imageRes = pageImages[pageIndex],
                )
            }

            // Bottom controls (di luar pager agar tidak ikut slide)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = TickrTheme.spacing.screen)
                    .padding(bottom = TickrTheme.spacing.lg),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                PageIndicator(
                    currentPage = pagerState.currentPage,
                    pageCount = state.pages.size,
                )

                Spacer(modifier = Modifier.height(TickrTheme.spacing.xl))

                if (isLastPage) {
                    Button(
                        onClick = { onAction(OnboardingAction.Finish) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(TickrCornerRadius.button),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = TickrTheme.colors.primaryBrand,
                        ),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp),
                    ) {
                        Text(
                            text = "Lanjutkan", // TODO(user): gunakan resource string onboarding_continue
                            color = Color.White,
                            style = TickrTheme.typography.productName,
                        )
                    }
                } else {
                    CircleArrowButton(
                        onClick = { onAction(OnboardingAction.Next) },
                    )
                }

                Spacer(modifier = Modifier.height(TickrTheme.spacing.lg))

                Text(
                    text = "Versi 1.0.4 • © 2024 Tickr Team", // TODO(user): gunakan resource string app_version_copyright
                    style = TickrTheme.typography.countdown.copy(fontSize = 12.sp),
                    color = TickrTheme.colors.textSecondary,
                )
            }
        }
    }
}

@Composable
private fun OnboardingPageItem(
    page: OnboardingPage,
    imageRes: DrawableResource,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = TickrTheme.spacing.screen),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Lebih banyak ruang di atas → konten turun mendekati tengah
        Spacer(modifier = Modifier.weight(0.55f))

        Image(
            painter = painterResource(imageRes),
            contentDescription = page.contentDescription,
            modifier = Modifier
                .fillMaxWidth()
                .height(360.dp),
            contentScale = ContentScale.Fit,
        )

        Spacer(modifier = Modifier.height(TickrTheme.spacing.lg))

        Text(
            text = page.title,
            style = TickrTheme.typography.onboardingTitle.copy(
                fontSize = 15.sp,
                lineHeight = 22.sp,
            ),
            color = TickrTheme.colors.textPrimary,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
            maxLines = 3,
            overflow = TextOverflow.Ellipsis,
        )

        Spacer(modifier = Modifier.weight(0.45f))
    }
}

@Composable
fun PageIndicator(
    currentPage: Int,
    pageCount: Int,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(pageCount) { index ->
            IndicatorDot(isActive = index == currentPage)
            if (index < pageCount - 1) {
                Spacer(modifier = Modifier.width(6.dp))
            }
        }
    }
}

@Composable
private fun IndicatorDot(isActive: Boolean) {
    val width: Dp by animateDpAsState(
        targetValue = if (isActive) 24.dp else 8.dp,
        animationSpec = tween(durationMillis = 300),
    )
    val color by animateColorAsState(
        targetValue = if (isActive) TickrTheme.colors.primaryBrand else TickrTheme.colors.textSecondary.copy(alpha = 0.3f),
        animationSpec = tween(durationMillis = 300),
    )

    Box(
        modifier = Modifier
            .width(width)
            .height(8.dp)
            .background(
                color = color,
                shape = RoundedCornerShape(TickrCornerRadius.pill),
            ),
    )
}

@Composable
private fun CircleArrowButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Button(
        onClick = onClick,
        modifier = modifier.size(56.dp),
        shape = CircleShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = TickrTheme.colors.primaryBrand,
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp),
    ) {
        Icon(
            painter = painterResource(Res.drawable.ic_arrow_right),
            contentDescription = "Lanjut ke halaman berikutnya", // TODO(user): gunakan resource string cd_onboarding_next
            tint = Color.White,
            modifier = Modifier.size(24.dp),
        )
    }
}

@Composable
fun DecorativeBlobs() {
    val blobColor = TickrTheme.colors.primaryBrand.copy(alpha = 0.08f)

    Canvas(modifier = Modifier.fillMaxSize()) {
        // Top-right blob
        drawOval(
            color = blobColor,
            topLeft = Offset(size.width * 0.65f, -120f),
            size = Size(220.dp.toPx(), 220.dp.toPx()),
        )
        // Bottom-left blob
        drawOval(
            color = blobColor,
            topLeft = Offset(-100f, size.height * 0.72f),
            size = Size(240.dp.toPx(), 240.dp.toPx()),
        )
    }
}

@Preview(name = "Onboarding · Page 1", showSystemUi = true)
@Composable
private fun PreviewPage1() {
    TickrTheme(darkTheme = false) {
        OnboardingScreen(
            state = OnboardingUiState(currentPage = 0),
            onAction = {},
        )
    }
}

@Preview(name = "Onboarding · Page 2", showSystemUi = true)
@Composable
private fun PreviewPage2() {
    TickrTheme(darkTheme = false) {
        OnboardingScreen(
            state = OnboardingUiState(currentPage = 1),
            onAction = {},
        )
    }
}

@Preview(name = "Onboarding · Page 3 (Lanjutkan)", showSystemUi = true)
@Composable
private fun PreviewPage3() {
    TickrTheme(darkTheme = false) {
        OnboardingScreen(
            state = OnboardingUiState(currentPage = 2, isLastPage = true),
            onAction = {},
        )
    }
}

@Preview(name = "Onboarding · Dark", showSystemUi = true)
@Composable
private fun PreviewDark() {
    TickrTheme(darkTheme = true) {
        OnboardingScreen(
            state = OnboardingUiState(currentPage = 0),
            onAction = {},
        )
    }
}
