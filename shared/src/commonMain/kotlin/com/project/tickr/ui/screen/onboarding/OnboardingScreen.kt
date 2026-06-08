package com.project.tickr.ui.screen.onboarding

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.project.tickr.presentation.onboarding.OnboardingAction
import com.project.tickr.presentation.onboarding.OnboardingUiState
import com.project.tickr.ui.theme.TickrCornerRadius
import com.project.tickr.ui.theme.TickrTheme
import kotlin.math.absoluteValue

@Composable
fun OnboardingScreen(
    state: OnboardingUiState,
    onAction: (OnboardingAction) -> Unit,
) {
    var currentPage by remember { mutableStateOf(state.currentPage) }
    var dragOffset by remember { mutableStateOf(0f) }

    val draggableState = rememberDraggableState { delta ->
        dragOffset += delta
        if (dragOffset.absoluteValue > 50) {
            if (dragOffset > 0 && currentPage > 0) {
                currentPage--
                onAction(OnboardingAction.PageChanged(currentPage))
            } else if (dragOffset < 0 && currentPage < state.pages.size - 1) {
                currentPage++
                onAction(OnboardingAction.PageChanged(currentPage))
            }
            dragOffset = 0f
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(TickrTheme.colors.background)
            .draggable(
                state = draggableState,
                orientation = Orientation.Horizontal,
            )
    ) {
        // Background decorative blobs
        DecorativeBlobs()

        // Main content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    horizontal = TickrTheme.spacing.screen,
                    vertical = TickrTheme.spacing.lg,
                ),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Illustration area
            Spacer(modifier = Modifier.height(32.dp))
            OnboardingPageContent(
                page = state.pages[currentPage],
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp),
            )

            Spacer(modifier = Modifier.weight(1f))

            // Body text
            Text(
                text = state.pages[currentPage].title,
                style = TickrTheme.typography.onboardingTitle,
                color = TickrTheme.colors.textPrimary,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(TickrTheme.spacing.xl))

            // Page indicator
            PageIndicator(
                currentPage = currentPage,
                pageCount = state.pages.size,
            )

            Spacer(modifier = Modifier.height(TickrTheme.spacing.xl))

            // Action button
            if (state.isLastPage) {
                Button(
                    onClick = { onAction(OnboardingAction.Finish) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(TickrCornerRadius.button),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = TickrTheme.colors.primaryBrand,
                    ),
                ) {
                    Text(
                        text = "Lanjutkan", // TODO(user): gunakan resource string
                        color = Color.White,
                        style = TickrTheme.typography.productName,
                    )
                }
            } else {
                CircleArrowButton(
                    onClick = { onAction(OnboardingAction.Next) },
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                )
            }

            Spacer(modifier = Modifier.height(TickrTheme.spacing.lg))

            // Footer
            Text(
                text = "Versi 1.0.4 • © 2024 Tickr Team", // TODO(user): gunakan resource string
                style = TickrTheme.typography.countdown.copy(fontSize = 12.sp),
                color = TickrTheme.colors.textSecondary,
            )
        }
    }
}

@Composable
fun OnboardingPageContent(
    page: com.project.tickr.presentation.onboarding.OnboardingPage,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .background(
                color = TickrTheme.colors.primaryBrand.copy(alpha = 0.1f),
                shape = RoundedCornerShape(20.dp),
            ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "Illustration: ${page.imageResId}",
            color = TickrTheme.colors.primaryBrand,
        )
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
        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.Center,
    ) {
        repeat(pageCount) { index ->
            IndicatorDot(
                isActive = index == currentPage,
            )
            if (index < pageCount - 1) {
                Spacer(modifier = Modifier.width(8.dp))
            }
        }
    }
}

@Composable
fun IndicatorDot(
    isActive: Boolean,
) {
    val width: Dp by animateDpAsState(
        targetValue = if (isActive) 24.dp else 8.dp,
        animationSpec = tween(durationMillis = 300),
    )

    Box(
        modifier = Modifier
            .width(width)
            .height(8.dp)
            .background(
                color = if (isActive) {
                    TickrTheme.colors.primaryBrand
                } else {
                    TickrTheme.colors.textSecondary.copy(alpha = 0.3f)
                },
                shape = RoundedCornerShape(TickrCornerRadius.pill),
            ),
    )
}

@Composable
fun CircleArrowButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .width(56.dp)
            .height(56.dp),
        shape = CircleShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = TickrTheme.colors.primaryBrand,
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 4.dp,
        ),
    ) {
        Text("→", color = Color.White, fontSize = 20.sp)
    }
}

@Composable
fun DecorativeBlobs() {
    val blobColor = TickrTheme.colors.primaryBrand.copy(alpha = 0.1f)

    Canvas(modifier = Modifier.fillMaxSize()) {
        // Top-right blob
        drawOval(
            color = blobColor,
            topLeft = Offset(size.width * 0.7f, -100f),
            size = Size(200.dp.toPx(), 200.dp.toPx()),
        )

        // Bottom-left blob
        drawOval(
            color = blobColor,
            topLeft = Offset(-80f, size.height * 0.75f),
            size = Size(220.dp.toPx(), 220.dp.toPx()),
        )

        // Bottom-right blob (smaller)
        drawOval(
            color = blobColor,
            topLeft = Offset(size.width * 0.8f, size.height * 0.8f),
            size = Size(150.dp.toPx(), 150.dp.toPx()),
        )
    }
}

@Preview
@Composable
fun OnboardingScreenLightPreview() {
    TickrTheme(darkTheme = false) {
        OnboardingScreen(
            state = OnboardingUiState(),
            onAction = {},
        )
    }
}

@Preview
@Composable
fun OnboardingScreenDarkPreview() {
    TickrTheme(darkTheme = true) {
        OnboardingScreen(
            state = OnboardingUiState(),
            onAction = {},
        )
    }
}
