package com.project.tickr.ui.screen.auth.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.project.tickr.domain.model.PasswordRequirements
import com.project.tickr.domain.model.PasswordStrength
import com.project.tickr.ui.theme.TickrTheme

@Composable
fun PasswordStrengthMeter(
    strength: PasswordStrength,
    requirements: PasswordRequirements,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        StrengthBar(strength = strength)
        Spacer(modifier = Modifier.height(8.dp))
        RequirementItem(
            met = requirements.hasMinLength,
            label = "Minimal 8 karakter", // TODO(user): gunakan resource string pw_req_length
        )
        RequirementItem(
            met = requirements.hasUpperAndLower,
            label = "Huruf besar & kecil", // TODO(user): gunakan resource string pw_req_case
        )
        RequirementItem(
            met = requirements.hasDigit,
            label = "Mengandung angka", // TODO(user): gunakan resource string pw_req_digit
        )
        RequirementItem(
            met = requirements.hasSymbol,
            label = "Mengandung simbol", // TODO(user): gunakan resource string pw_req_symbol
        )
    }
}

@Composable
private fun StrengthBar(
    strength: PasswordStrength,
    modifier: Modifier = Modifier,
) {
    val progress by animateFloatAsState(
        targetValue = strength.ordinal / PasswordStrength.entries.lastIndex.toFloat(),
        animationSpec = tween(durationMillis = 300),
        label = "strength_progress",
    )
    val barColor by animateColorAsState(
        targetValue = when (strength) {
            PasswordStrength.None -> TickrTheme.colors.textSecondary.copy(alpha = 0.2f)
            PasswordStrength.Weak -> TickrTheme.colors.critical
            PasswordStrength.Fair -> TickrTheme.colors.warning
            PasswordStrength.Good -> TickrTheme.colors.secondaryAccent
            PasswordStrength.Strong -> TickrTheme.colors.safe
        },
        animationSpec = tween(durationMillis = 300),
        label = "strength_color",
    )
    val strengthLabel = when (strength) {
        PasswordStrength.None -> ""
        PasswordStrength.Weak -> "Lemah" // TODO(user): gunakan resource string pw_strength_weak
        PasswordStrength.Fair -> "Cukup" // TODO(user): gunakan resource string pw_strength_fair
        PasswordStrength.Good -> "Baik"  // TODO(user): gunakan resource string pw_strength_good
        PasswordStrength.Strong -> "Kuat" // TODO(user): gunakan resource string pw_strength_strong
    }

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(TickrTheme.colors.textSecondary.copy(alpha = 0.12f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(progress)
                    .clip(RoundedCornerShape(2.dp))
                    .background(barColor)
            )
        }
        if (strengthLabel.isNotEmpty()) {
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = strengthLabel,
                style = TickrTheme.typography.countdown,
                color = barColor,
            )
        }
    }
}

@Composable
private fun RequirementItem(
    met: Boolean,
    label: String,
    modifier: Modifier = Modifier,
) {
    val iconColor by animateColorAsState(
        targetValue = if (met) TickrTheme.colors.safe else TickrTheme.colors.textSecondary.copy(alpha = 0.5f),
        animationSpec = tween(durationMillis = 200),
        label = "req_color_$label",
    )
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start,
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(iconColor),
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = label,
            style = TickrTheme.typography.countdown,
            color = iconColor,
        )
    }
}
