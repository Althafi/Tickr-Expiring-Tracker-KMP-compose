package com.project.tickr.ui.screen.auth.components

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.text.KeyboardOptions
import com.project.tickr.presentation.common.UiText
import com.project.tickr.ui.theme.TickrTheme

@Composable
fun PasswordTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    isVisible: Boolean,
    onToggleVisibility: () -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    error: UiText? = null,
    enabled: Boolean = true,
) {
    TickrTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = placeholder,
        modifier = modifier,
        label = label,
        leadingIcon = leadingIcon,
        trailingIcon = {
            IconButton(
                onClick = onToggleVisibility,
                modifier = Modifier.size(44.dp),
            ) {
                Icon(
                    imageVector = if (isVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                    contentDescription = "Tampilkan atau sembunyikan kata sandi", // TODO(user): gunakan resource string cd_toggle_password
                    tint = TickrTheme.colors.textSecondary,
                    modifier = Modifier.size(20.dp),
                )
            }
        },
        error = error,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        visualTransformation = if (isVisible) VisualTransformation.None else PasswordVisualTransformation(),
        singleLine = true,
        enabled = enabled,
    )
}
