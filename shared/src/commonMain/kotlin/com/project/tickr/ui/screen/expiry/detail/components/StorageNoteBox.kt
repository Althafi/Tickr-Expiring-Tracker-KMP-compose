package com.project.tickr.ui.screen.expiry.detail.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.project.tickr.ui.theme.TickrTheme

@Composable
fun StorageNoteBox(
    note: String?,
    modifier: Modifier = Modifier,
) {
    if (note.isNullOrBlank()) return // hidden bila kosong

    val colors = TickrTheme.colors
    val typography = TickrTheme.typography
    val spacing = TickrTheme.spacing

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(colors.warningContainer)
            .padding(spacing.cardInner),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Outlined.PushPin,
                contentDescription = null,
                tint = colors.warning,
                modifier = Modifier.size(18.dp),
            )
            Spacer(Modifier.width(spacing.sm))
            Text(
                text = "Catatan Penyimpanan", // TODO(user): stringResource detail_storage_note_title
                style = typography.body.copy(fontWeight = FontWeight.SemiBold),
                color = colors.warning,
            )
        }
        Spacer(Modifier.height(spacing.sm))
        Text(
            text = note,
            style = typography.body,
            color = colors.textPrimary,
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFAFAFA)
@Composable
private fun StorageNoteBoxPreview() {
    TickrTheme {
        StorageNoteBox(
            note = "Simpan di kulkas bagian bawah, jauhkan dari makanan matang.",
            modifier = Modifier.padding(16.dp),
        )
    }
}
