package com.project.tickr.ui.screen.home.components

import com.project.tickr.ui.common.TickrIcons
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.project.tickr.domain.model.WasteTrend
import com.project.tickr.ui.theme.TickrCornerRadius
import com.project.tickr.ui.theme.TickrTheme
import tickrapp.shared.generated.resources.*
import org.jetbrains.compose.resources.stringResource

@Composable
fun WasteInsightCard(
    wasteTrend: WasteTrend?,
    modifier: Modifier = Modifier,
) {
    val colors = TickrTheme.colors
    val typography = TickrTheme.typography
    val spacing = TickrTheme.spacing

    val highlight = wasteTrend?.let {
        stringResource(
            if (it.isImproving) Res.string.waste_highlight_down else Res.string.waste_highlight_up,
            it.deltaPercent,
        )
    }
    val message = if (wasteTrend != null && highlight != null) {
        stringResource(
            if (wasteTrend.isImproving) Res.string.waste_message_improving else Res.string.waste_message_worsening,
            highlight,
        )
    } else null
    val trendDescription = if (highlight != null) {
        stringResource(Res.string.waste_cd_trend, highlight)
    } else stringResource(Res.string.waste_cd_no_data)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(TickrCornerRadius.card))
            .background(colors.surface)
            .border(
                width = 1.dp,
                color = colors.textSecondary.copy(alpha = 0.12f),
                shape = RoundedCornerShape(TickrCornerRadius.card),
            )
            .padding(spacing.cardInner)
            .semantics { contentDescription = trendDescription },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(spacing.md),
    ) {
        // Ikon lingkaran
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(if (wasteTrend != null) colors.safeContainer else colors.surface),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = if (wasteTrend != null) TickrIcons.TrendingDown else TickrIcons.Info,
                contentDescription = null,
                tint = if (wasteTrend != null) colors.safe else colors.textSecondary,
                modifier = Modifier.size(22.dp),
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            if (wasteTrend != null) {
                Text(
                    text = buildAnnotatedString {
                        val text = message.orEmpty()
                        val start = highlight?.let { text.indexOf(it) } ?: -1
                        if (start < 0 || highlight == null) {
                            append(text)
                        } else {
                            append(text.substring(0, start))
                            withStyle(SpanStyle(color = colors.safe, fontWeight = FontWeight.Bold)) {
                                append(highlight)
                            }
                            append(text.substring(start + highlight.length))
                        }
                    },
                    style = typography.body,
                    color = colors.textPrimary,
                )
                Spacer(Modifier.size(spacing.xs))
                Text(
                    text = stringResource(Res.string.waste_message_sub),
                    style = typography.caption,
                    color = colors.textSecondary,
                )
            } else {
                Text(
                    text = stringResource(Res.string.waste_empty),
                    style = typography.body,
                    color = colors.textSecondary,
                )
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFAFAFA)
@Composable
private fun WasteInsightCardPreview() {
    TickrTheme {
        Column(Modifier.padding(16.dp)) {
            WasteInsightCard(wasteTrend = WasteTrend(12, true))
            Spacer(Modifier.size(12.dp))
            WasteInsightCard(wasteTrend = null)
        }
    }
}
