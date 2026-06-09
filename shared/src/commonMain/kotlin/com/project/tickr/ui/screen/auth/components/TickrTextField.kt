package com.project.tickr.ui.screen.auth.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.project.tickr.presentation.common.UiText
import com.project.tickr.presentation.common.asString
import com.project.tickr.ui.theme.TickrCornerRadius
import com.project.tickr.ui.theme.TickrTheme

@Composable
fun TickrTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    label: String? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    error: UiText? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    singleLine: Boolean = true,
    enabled: Boolean = true,
) {
    Column(modifier = modifier) {
        if (label != null) {
            Text(
                text = label,
                style = TickrTheme.typography.body,
                color = TickrTheme.colors.textSecondary,
                modifier = Modifier.padding(bottom = 4.dp),
            )
        }
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = {
                Text(
                    text = placeholder,
                    style = TickrTheme.typography.body,
                    color = TickrTheme.colors.textSecondary,
                )
            },
            leadingIcon = leadingIcon,
            trailingIcon = trailingIcon,
            isError = error != null,
            supportingText = error?.let {
                {
                    Text(
                        text = it.asString(),
                        style = TickrTheme.typography.countdown,
                        color = TickrTheme.colors.critical,
                    )
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 56.dp),
            shape = RoundedCornerShape(TickrCornerRadius.button),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = TickrTheme.colors.primaryBrand,
                unfocusedBorderColor = TickrTheme.colors.textSecondary.copy(alpha = 0.35f),
                errorBorderColor = TickrTheme.colors.critical,
                focusedTextColor = TickrTheme.colors.textPrimary,
                unfocusedTextColor = TickrTheme.colors.textPrimary,
                disabledTextColor = TickrTheme.colors.textSecondary,
                cursorColor = TickrTheme.colors.primaryBrand,
                focusedLabelColor = TickrTheme.colors.primaryBrand,
                unfocusedLabelColor = TickrTheme.colors.textSecondary,
            ),
            singleLine = singleLine,
            keyboardOptions = keyboardOptions,
            visualTransformation = visualTransformation,
            enabled = enabled,
        )
    }
}
