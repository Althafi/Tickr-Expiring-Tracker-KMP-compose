package com.project.tickr.ui.screen.home.additem

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.project.tickr.domain.model.Category
import com.project.tickr.presentation.additem.AddItemAction
import com.project.tickr.presentation.additem.AddItemUiState
import com.project.tickr.presentation.additem.AddItemViewModel
import com.project.tickr.presentation.additem.UNIT_OPTIONS
import com.project.tickr.ui.common.PlatformDatePicker
import com.project.tickr.ui.common.rememberImagePickerLauncher
import com.project.tickr.ui.theme.TickrCornerRadius
import com.project.tickr.ui.theme.TickrTheme
import kotlinx.coroutines.launch

// ——— Public entry-point composable ———

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddItemSheet(
    viewModel: AddItemViewModel,
    onDismiss: () -> Unit,
) {
    val state by viewModel.state.collectAsState()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    var showDatePicker by remember { mutableStateOf(false) }

    val dismissSheet: () -> Unit = {
        scope.launch {
            sheetState.hide()
            onDismiss()
        }
    }

    val pickImage = rememberImagePickerLauncher { uri ->
        viewModel.onAction(AddItemAction.PhotoPicked(uri))
    }

    if (showDatePicker) {
        PlatformDatePicker(
            show = true,
            initialDateMillis = state.expiryDateMillis,
            onDateSelected = { millis ->
                viewModel.onAction(AddItemAction.ExpiryPicked(millis))
                showDatePicker = false
            },
            onDismiss = { showDatePicker = false },
        )
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = TickrCornerRadius.card, topEnd = TickrCornerRadius.card),
        containerColor = TickrTheme.colors.surface,
        dragHandle = { androidx.compose.material3.BottomSheetDefaults.DragHandle() },
    ) {
        AddItemContent(
            state = state,
            onAction = viewModel::onAction,
            onDismiss = dismissSheet,
            onPickImage = pickImage,
            onShowDatePicker = { showDatePicker = true },
        )
    }
}

// ——— Content (testable + previewable) ———

@Composable
fun AddItemContent(
    state: AddItemUiState,
    onAction: (AddItemAction) -> Unit,
    onDismiss: () -> Unit,
    onPickImage: () -> Unit,
    onShowDatePicker: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = TickrTheme.colors
    val typography = TickrTheme.typography
    val spacing = TickrTheme.spacing

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = spacing.screen)
            .padding(bottom = spacing.xxl),
        verticalArrangement = Arrangement.spacedBy(spacing.md),
    ) {
        // ── Header ──────────────────────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Tambah Barang",
                style = typography.sectionTitle,
                color = colors.textPrimary,
            )
            // Plain X icon — no circle wrapper
            IconButton(onClick = onDismiss) {
                Icon(Icons.Outlined.Close, contentDescription = "Tutup", tint = colors.textPrimary)
            }
        }

        // ── 1. Photo picker ─────────────────────────────────────────
        PhotoPickerBox(
            photoPath = state.photoPath,
            onClick = onPickImage,
        )

        // ── 2. Nama produk ──────────────────────────────────────────
        OutlinedTextField(
            value = state.name,
            onValueChange = { onAction(AddItemAction.NameChanged(it)) },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Nama Produk", color = colors.textSecondary) },
            isError = state.nameError != null,
            supportingText = state.nameError?.let { msg -> { Text(msg, color = colors.critical) } },
            shape = RoundedCornerShape(TickrCornerRadius.button),
            colors = tickrOutlinedColors(),
            singleLine = true,
        )

        // ── 3. Kategori ─────────────────────────────────────────────
        CategorySection(state = state, onAction = onAction)

        // ── 4. Jumlah ───────────────────────────────────────────────
        QuantitySection(state = state, onAction = onAction)

        // ── 5. Satuan (chips scrollable horizontal) ─────────────────
        UnitSection(state = state, onAction = onAction)

        // ── 6. Waktu Kedaluwarsa ────────────────────────────────────
        ExpiryDateSection(
            state = state,
            onAction = onAction,
            onShowDatePicker = onShowDatePicker,
        )

        // ── 7. CTA ──────────────────────────────────────────────────
        Button(
            onClick = { onAction(AddItemAction.Save) },
            enabled = state.canSave,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(TickrCornerRadius.button),
            colors = ButtonDefaults.buttonColors(
                containerColor = colors.primaryBrand,
                disabledContainerColor = colors.primaryBrand.copy(alpha = 0.4f),
            ),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp),
        ) {
            if (state.isSaving) {
                CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier.size(22.dp),
                    strokeWidth = 2.dp,
                )
            } else {
                Icon(
                    Icons.Outlined.Check,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp),
                )
                Spacer(Modifier.width(TickrTheme.spacing.sm))
                Text(text = "Catat Barang", color = Color.White, style = typography.body)
            }
        }
    }
}

// ——— Section composables ———

@Composable
private fun PhotoPickerBox(
    photoPath: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = TickrTheme.colors
    val typography = TickrTheme.typography

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(120.dp)
            .clip(RoundedCornerShape(TickrCornerRadius.card))
            .border(
                width = 1.5.dp,
                brush = SolidColor(
                    if (photoPath != null) colors.safe else colors.textSecondary.copy(alpha = 0.4f),
                ),
                shape = RoundedCornerShape(TickrCornerRadius.card),
            )
            .background(colors.background)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        if (photoPath != null) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.Outlined.Image,
                    contentDescription = null,
                    tint = colors.safe,
                    modifier = Modifier.size(28.dp),
                )
                Spacer(Modifier.height(TickrTheme.spacing.xs))
                Text(
                    text = "Foto dipilih — ketuk untuk ganti",
                    style = typography.caption,
                    color = colors.safe,
                    textAlign = TextAlign.Center,
                )
            }
        } else {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.Outlined.CameraAlt,
                    contentDescription = null,
                    tint = colors.textSecondary,
                    modifier = Modifier.size(28.dp),
                )
                Spacer(Modifier.height(TickrTheme.spacing.xs))
                Text(
                    text = "Ketuk untuk unggah foto",
                    style = typography.body,
                    color = colors.textSecondary,
                )
            }
        }
    }
}

@Composable
private fun CategorySection(
    state: AddItemUiState,
    onAction: (AddItemAction) -> Unit,
) {
    val colors = TickrTheme.colors
    val typography = TickrTheme.typography

    Column {
        Text(text = "Kategori", style = typography.body, color = colors.textSecondary)
        Spacer(Modifier.height(TickrTheme.spacing.xs))
        when {
            state.isLoadingCategories -> {
                Box(
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    contentAlignment = Alignment.CenterStart,
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = colors.primaryBrand,
                        strokeWidth = 2.dp,
                    )
                }
            }
            state.dbCategories.isEmpty() -> {
                Text(
                    text = "Gagal memuat kategori. Coba tutup dan buka kembali.",
                    style = typography.caption,
                    color = colors.critical,
                )
            }
            else -> {
                Column(verticalArrangement = Arrangement.spacedBy(TickrTheme.spacing.xs)) {
                    state.dbCategories.forEach { cat ->
                        CategoryRadioRow(
                            label = cat.name,
                            selected = state.selectedCategoryId == cat.id,
                            onClick = { onAction(AddItemAction.CategorySelected(cat.id, cat.name)) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryRadioRow(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = TickrTheme.colors
    val typography = TickrTheme.typography

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(TickrCornerRadius.button))
            .background(if (selected) colors.primaryBrand.copy(alpha = 0.08f) else Color.Transparent)
            .border(
                width = 1.dp,
                color = if (selected) colors.primaryBrand else colors.textSecondary.copy(alpha = 0.2f),
                shape = RoundedCornerShape(TickrCornerRadius.button),
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label,
            style = typography.body,
            color = if (selected) colors.primaryBrand else colors.textPrimary,
        )
        // Radio indicator
        Box(
            modifier = Modifier
                .size(20.dp)
                .clip(CircleShape)
                .border(
                    width = if (selected) 0.dp else 2.dp,
                    color = if (selected) Color.Transparent else colors.textSecondary.copy(alpha = 0.4f),
                    shape = CircleShape,
                )
                .background(if (selected) colors.primaryBrand else Color.Transparent),
            contentAlignment = Alignment.Center,
        ) {
            if (selected) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(Color.White),
                )
            }
        }
    }
}

@Composable
private fun QuantitySection(
    state: AddItemUiState,
    onAction: (AddItemAction) -> Unit,
) {
    val colors = TickrTheme.colors
    val typography = TickrTheme.typography

    Column {
        Text(text = "Jumlah", style = typography.body, color = colors.textSecondary)
        Spacer(Modifier.height(TickrTheme.spacing.xs))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(TickrTheme.spacing.sm),
        ) {
            // Minus button
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(
                        if (state.quantity > 0) colors.primaryBrand.copy(alpha = 0.1f)
                        else colors.background,
                    )
                    .clickable(enabled = state.quantity > 0) {
                        onAction(AddItemAction.QtyChanged(state.quantity - 1))
                    },
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Outlined.Remove,
                    contentDescription = "Kurang",
                    tint = if (state.quantity > 0) colors.primaryBrand
                    else colors.textSecondary.copy(alpha = 0.4f),
                    modifier = Modifier.size(20.dp),
                )
            }

            // Editable number field
            OutlinedTextField(
                value = if (state.quantity == 0) "" else state.quantity.toString(),
                onValueChange = { v ->
                    onAction(AddItemAction.QtyChanged(v.filter { it.isDigit() }.toIntOrNull() ?: 0))
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                textStyle = typography.sectionTitle.copy(
                    color = colors.textPrimary,
                    textAlign = TextAlign.Center,
                ),
                placeholder = {
                    Text(
                        "0",
                        style = typography.sectionTitle.copy(textAlign = TextAlign.Center),
                        color = colors.textSecondary,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                    )
                },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(TickrCornerRadius.button),
                colors = tickrOutlinedColors(),
                singleLine = true,
            )

            // Plus button
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(colors.primaryBrand.copy(alpha = 0.1f))
                    .clickable { onAction(AddItemAction.QtyChanged(state.quantity + 1)) },
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Outlined.Add,
                    contentDescription = "Tambah",
                    tint = colors.primaryBrand,
                    modifier = Modifier.size(20.dp),
                )
            }
        }
    }
}

@Composable
private fun UnitSection(
    state: AddItemUiState,
    onAction: (AddItemAction) -> Unit,
) {
    val colors = TickrTheme.colors
    val typography = TickrTheme.typography

    Column {
        Text(text = "Satuan", style = typography.body, color = colors.textSecondary)
        Spacer(Modifier.height(TickrTheme.spacing.xs))
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(TickrTheme.spacing.sm),
            contentPadding = PaddingValues(horizontal = 2.dp),
        ) {
            items(UNIT_OPTIONS) { unit ->
                val selected = unit == state.unit
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50.dp))
                        .background(if (selected) colors.primaryBrand else Color.Transparent)
                        .border(
                            width = 1.dp,
                            color = if (selected) Color.Transparent
                            else colors.textSecondary.copy(alpha = 0.3f),
                            shape = RoundedCornerShape(50.dp),
                        )
                        .clickable { onAction(AddItemAction.UnitChanged(unit)) }
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                ) {
                    Text(
                        text = unit,
                        style = typography.body,
                        color = if (selected) Color.White else colors.textPrimary,
                    )
                }
            }
        }
    }
}

@Composable
private fun ExpiryDateSection(
    state: AddItemUiState,
    onAction: (AddItemAction) -> Unit,
    onShowDatePicker: () -> Unit,
) {
    val colors = TickrTheme.colors
    val typography = TickrTheme.typography

    Column {
        Text(text = "Waktu Kedaluwarsa", style = typography.body, color = colors.textSecondary)
        Spacer(Modifier.height(TickrTheme.spacing.xs))
        OutlinedTextField(
            value = state.rawDateDigits,
            onValueChange = { v ->
                val digits = v.filter { it.isDigit() }.take(8)
                onAction(AddItemAction.RawDateChanged(digits))
            },
            visualTransformation = DateMaskTransformation,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            placeholder = { Text("DD / MM / YYYY", color = colors.textSecondary) },
            trailingIcon = {
                IconButton(onClick = onShowDatePicker) {
                    Icon(
                        Icons.Outlined.CalendarToday,
                        contentDescription = "Pilih tanggal dari kalender",
                        tint = colors.textSecondary,
                    )
                }
            },
            isError = state.expiryError != null,
            supportingText = state.expiryError?.let { msg ->
                { Text(msg, color = colors.critical, style = typography.caption) }
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(TickrCornerRadius.button),
            colors = tickrOutlinedColors(),
            singleLine = true,
        )
    }
}

// ——— Helpers ———

/** Transforms "DDMMYYYY" digits into "DD / MM / YYYY" for display only. */
private object DateMaskTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val digits = text.text
        val out = StringBuilder()
        digits.forEachIndexed { i, c ->
            out.append(c)
            if (i == 1 || i == 3) out.append(" / ")
        }
        val offsetMap = object : OffsetMapping {
            override fun originalToTransformed(offset: Int) = when {
                offset <= 1 -> offset
                offset <= 3 -> offset + 3
                else -> minOf(offset + 6, 14)
            }
            override fun transformedToOriginal(offset: Int) = when {
                offset <= 1 -> offset
                offset <= 5 -> 2
                offset == 6 -> 3
                offset <= 10 -> 4
                else -> minOf(offset - 6, 8)
            }
        }
        return TransformedText(AnnotatedString(out.toString()), offsetMap)
    }
}

@Composable
private fun tickrOutlinedColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = TickrTheme.colors.primaryBrand,
    unfocusedBorderColor = TickrTheme.colors.textSecondary.copy(alpha = 0.4f),
    focusedTextColor = TickrTheme.colors.textPrimary,
    unfocusedTextColor = TickrTheme.colors.textPrimary,
    cursorColor = TickrTheme.colors.primaryBrand,
    errorBorderColor = TickrTheme.colors.critical,
    errorTextColor = TickrTheme.colors.textPrimary,
    errorCursorColor = TickrTheme.colors.critical,
)

// ——— Previews ———

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun AddItemContentPreview() {
    TickrTheme {
        AddItemContent(
            state = AddItemUiState(
                dbCategories = listOf(
                    Category(1, null, "Makanan & Minuman", "ic_food", "#0D6759", ""),
                    Category(2, null, "Kecantikan", "ic_beauty", "#FA9A08", ""),
                    Category(3, null, "Obat & Vitamin", "ic_med", "#5EC9B7", ""),
                    Category(4, null, "Lainnya", "ic_other", "#6B7280", ""),
                ),
                selectedCategoryId = 1,
                quantity = 2,
                unit = "Pcs",
                rawDateDigits = "12032025",
                expiryIsoDate = "2025-03-12",
            ),
            onAction = {},
            onDismiss = {},
            onPickImage = {},
            onShowDatePicker = {},
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF1A221E)
@Composable
private fun AddItemContentDarkPreview() {
    TickrTheme(darkTheme = true) {
        AddItemContent(
            state = AddItemUiState(),
            onAction = {},
            onDismiss = {},
            onPickImage = {},
            onShowDatePicker = {},
        )
    }
}
