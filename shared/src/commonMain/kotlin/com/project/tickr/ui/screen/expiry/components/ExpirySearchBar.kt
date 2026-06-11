package com.project.tickr.ui.screen.expiry.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.project.tickr.ui.theme.TickrCornerRadius
import com.project.tickr.ui.theme.TickrTheme

@Composable
fun ExpirySearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onFilterClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = TickrTheme.colors
    val typography = TickrTheme.typography
    val spacing = TickrTheme.spacing

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            modifier = Modifier
                .weight(1f)
                .height(48.dp)
                .clip(RoundedCornerShape(TickrCornerRadius.pill))
                .background(colors.surface)
                .border(1.dp, colors.textSecondary.copy(alpha = 0.18f), RoundedCornerShape(TickrCornerRadius.pill))
                .padding(horizontal = spacing.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                Icons.Outlined.Search,
                contentDescription = null,
                tint = colors.textSecondary,
                modifier = Modifier.size(20.dp).semantics { contentDescription = "Cari barang" }, // TODO(user): pakai stringResource
            )
            Spacer(Modifier.width(spacing.sm))
            BasicTextField(
                value = query,
                onValueChange = onQueryChange,
                modifier = Modifier.weight(1f),
                textStyle = typography.body.copy(color = colors.textPrimary),
                cursorBrush = SolidColor(colors.primaryBrand),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { }),
                decorationBox = { inner ->
                    if (query.isEmpty()) {
                        Text(
                            "Cari barang…", // TODO(user): pakai stringResource
                            style = typography.body,
                            color = colors.textSecondary,
                        )
                    }
                    inner()
                },
            )
        }

        Spacer(Modifier.width(spacing.sm))

        IconButton(
            onClick = onFilterClick,
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(TickrCornerRadius.button))
                .background(colors.surface)
                .border(1.dp, colors.textSecondary.copy(alpha = 0.18f), RoundedCornerShape(TickrCornerRadius.button))
                .semantics { contentDescription = "Buka filter status" }, // TODO(user): pakai stringResource
        ) {
            Icon(
                Icons.Outlined.Tune,
                contentDescription = null,
                tint = colors.textPrimary,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFAFAFA)
@Composable
private fun ExpirySearchBarPreview() {
    TickrTheme {
        ExpirySearchBar(
            query = "",
            onQueryChange = {},
            onFilterClick = {},
            modifier = Modifier.padding(16.dp),
        )
    }
}
