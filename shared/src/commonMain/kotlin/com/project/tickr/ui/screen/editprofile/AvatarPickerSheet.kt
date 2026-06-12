package com.project.tickr.ui.screen.editprofile

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.project.tickr.ui.theme.TickrTheme

// 12 preset seeds — DiceBear adventurer style menghasilkan karakter unik per seed
// TODO(user): sesuaikan seed / tambah lebih banyak sesuai kebutuhan
private val AVATAR_PRESETS = listOf(
    "Kitsune", "Panda", "Koala", "Shiba",
    "Tanuki", "Capybara", "Axolotl", "Neko",
    "Usagi", "Kuma", "Totoro", "Kappa",
)

// Warna background pastel bervariasi — agar setiap kartu terasa berbeda
private const val BG_COLORS = "b6e3f4,c0aede,d1d4f9,ffd5dc,ffdfbf"

fun dicebearUrl(seed: String, size: Int = 200): String =
    "https://api.dicebear.com/9.x/adventurer/png?seed=$seed&size=$size&backgroundColor=$BG_COLORS"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AvatarPickerSheet(
    currentSeed: String,
    onSelected: (seed: String) -> Unit,
    onDismiss: () -> Unit,
) {
    val colors = TickrTheme.colors
    val spacing = TickrTheme.spacing
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = colors.surface,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = spacing.xxl),
        ) {
            Text(
                text = "Pilih Avatar", // TODO(user): gunakan string resource
                style = TickrTheme.typography.sectionTitle,
                color = colors.textPrimary,
                modifier = Modifier.padding(horizontal = spacing.screen, vertical = spacing.sm),
            )
            Text(
                text = "Tap avatar untuk memilih", // TODO(user): gunakan string resource
                style = TickrTheme.typography.body,
                color = colors.textSecondary,
                modifier = Modifier.padding(horizontal = spacing.screen),
            )
            Spacer(Modifier.height(spacing.lg))

            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                contentPadding = PaddingValues(horizontal = spacing.screen, vertical = spacing.sm),
                horizontalArrangement = Arrangement.spacedBy(spacing.md),
                verticalArrangement = Arrangement.spacedBy(spacing.md),
            ) {
                items(AVATAR_PRESETS) { seed ->
                    AvatarCell(
                        seed = seed,
                        isSelected = seed == currentSeed,
                        onClick = { onSelected(seed) },
                    )
                }
            }
        }
    }
}

@Composable
private fun AvatarCell(
    seed: String,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    val colors = TickrTheme.colors

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(72.dp)
            .clip(CircleShape)
            .then(
                if (isSelected) Modifier.border(3.dp, colors.primaryBrand, CircleShape)
                else Modifier.border(1.5.dp, colors.textSecondary.copy(alpha = 0.15f), CircleShape)
            )
            .clickable(onClick = onClick),
    ) {
        AsyncImage(
            model = dicebearUrl(seed, size = 144),
            contentDescription = seed, // TODO(user): tambahkan terjemahan nama avatar jika perlu
            contentScale = ContentScale.Crop,
            modifier = Modifier.size(72.dp).clip(CircleShape),
        )
    }
}
