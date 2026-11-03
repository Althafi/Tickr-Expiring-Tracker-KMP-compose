package com.project.tickr.ui.screen.onboarding

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import kotlin.math.abs
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.delay
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
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
import org.jetbrains.compose.resources.painterResource
import tickrapp.shared.generated.resources.Res
import tickrapp.shared.generated.resources.ic_arrow_right
import org.jetbrains.compose.resources.stringResource
import com.project.tickr.ui.i18n.LocalAppInfo
import tickrapp.shared.generated.resources.*

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
                    pageIndex = pageIndex,
                    isActive = pagerState.currentPage == pageIndex,
                    pageOffset = {
                        // 0 = halaman ini di tengah; ±1 = satu halaman di kiri/kanan.
                        (pagerState.currentPage - pageIndex) + pagerState.currentPageOffsetFraction
                    },
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
                            text = stringResource(Res.string.onboarding_continue),
                            color = TickrTheme.colors.onBrand,
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
                    text = stringResource(Res.string.app_version_footer, LocalAppInfo.current.versionName),
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
    pageIndex: Int,
    isActive: Boolean,
    pageOffset: () -> Float,
) {
    // Teks menyusul saat halaman pertama kali tampil; gerak ilustrasi ada di OnboardingIllustration.
    val intro = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        intro.animateTo(1f, tween(durationMillis = 700, easing = FastOutSlowInEasing))
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = TickrTheme.spacing.screen),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Lebih banyak ruang di atas → konten turun mendekati tengah
        Spacer(modifier = Modifier.weight(0.55f))

        // Slot 360dp; ilustrasi persegi (pivot animasinya memakai ruang 500x500) di tengahnya.
        Box(
            modifier = Modifier.fillMaxWidth().height(360.dp),
            contentAlignment = Alignment.Center,
        ) {
            OnboardingIllustration(
                page = pageIndex,
                active = isActive,
                modifier = Modifier
                    .fillMaxHeight()
                    .graphicsLayer {
                        val offset = pageOffset()
                        val distance = abs(offset).coerceIn(0f, 1f)
                        // Parallax saat digeser: ilustrasi bergeser lebih jauh dari halamannya, mengecil dan memudar.
                        translationX = offset * size.width * 0.35f
                        val s = 1f - 0.18f * distance
                        scaleX = s
                        scaleY = s
                        alpha = 1f - distance
                    },
            )
        }

        Spacer(modifier = Modifier.height(TickrTheme.spacing.lg))

        Text(
            text = stringResource(page.title),
            style = TickrTheme.typography.onboardingTitle.copy(
                fontSize = 15.sp,
                lineHeight = 22.sp,
            ),
            color = TickrTheme.colors.textPrimary,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer {
                    val offset = pageOffset()
                    val distance = abs(offset).coerceIn(0f, 1f)
                    // Teks menyusul ilustrasi: telat sedikit saat intro, memudar lebih cepat saat digeser.
                    val enter = ((intro.value - 0.3f) / 0.7f).coerceIn(0f, 1f)
                    translationX = offset * size.width * 0.12f
                    translationY = (1f - enter) * 16.dp.toPx() + distance * 8.dp.toPx()
                    alpha = (1f - distance * 1.4f).coerceIn(0f, 1f) * enter
                },
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
            contentDescription = stringResource(Res.string.cd_onboarding_next),
            tint = TickrTheme.colors.onBrand,
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
