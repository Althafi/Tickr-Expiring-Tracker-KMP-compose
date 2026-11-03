package com.project.tickr.presentation.additem

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.tickr.core.result.onError
import com.project.tickr.core.result.onSuccess
import com.project.tickr.core.util.DateTimeUtil
import com.project.tickr.core.result.DataResult
import com.project.tickr.domain.usecase.category.GetCategoriesUseCase
import com.project.tickr.domain.usecase.home.AddItemParams
import com.project.tickr.domain.usecase.home.AddItemUseCase
import com.project.tickr.domain.usecase.item.DiscardProductImageUseCase
import com.project.tickr.domain.usecase.item.SaveProductImageUseCase
import com.project.tickr.core.result.AppError
import com.project.tickr.domain.model.CategoryKeys
import com.project.tickr.domain.model.ProductInfo
import com.project.tickr.domain.provider.ConnectivityProvider
import com.project.tickr.domain.usecase.product.DownloadProductImageUseCase
import com.project.tickr.domain.usecase.product.LookupBarcodeUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.project.tickr.presentation.common.AppMessenger
import com.project.tickr.presentation.common.ErrorMessageProvider
import com.project.tickr.presentation.common.asUiText
import com.project.tickr.presentation.common.UiText
import tickrapp.shared.generated.resources.*
import com.project.tickr.domain.usecase.reminder.SyncRemindersUseCase

class AddItemViewModel(
    private val addItem: AddItemUseCase,
    private val getCategories: GetCategoriesUseCase,
    private val dateTime: DateTimeUtil,
    private val saveProductImage: SaveProductImageUseCase,
    private val discardProductImage: DiscardProductImageUseCase,
    private val errorMessages: ErrorMessageProvider,
    private val syncReminders: SyncRemindersUseCase,
    private val messenger: AppMessenger,
    private val lookupBarcode: LookupBarcodeUseCase,
    private val downloadProductImage: DownloadProductImageUseCase,
    private val connectivity: ConnectivityProvider,
) : ViewModel() {

    private val _state = MutableStateFlow(AddItemUiState())
    val state: StateFlow<AddItemUiState> = _state.asStateFlow()

    private val _events = Channel<AddItemEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    private var lookupJob: Job? = null
    private var noticeJob: Job? = null

    init {
        loadCategories()
    }

    fun onAction(action: AddItemAction) {
        when (action) {
            AddItemAction.Reset -> {
                lookupJob?.cancel()
                noticeJob?.cancel()
                _state.update { s ->
                    val cached = s.dbCategories
                    AddItemUiState(
                        dbCategories = cached,
                        isLoadingCategories = cached.isEmpty(),
                        selectedCategoryId = cached.firstOrNull()?.id,
                        selectedCategoryName = cached.firstOrNull()?.name,
                    )
                }
                loadCategories()
            }
            is AddItemAction.NameChanged ->
                _state.update { it.copy(name = action.value, nameError = null) }
            is AddItemAction.CategorySelected ->
                _state.update { it.copy(selectedCategoryId = action.id, selectedCategoryName = action.name) }
            is AddItemAction.QtyChanged ->
                _state.update { it.copy(quantity = action.value.coerceAtLeast(0)) }
            is AddItemAction.UnitChanged ->
                _state.update { it.copy(unit = action.value) }
            is AddItemAction.RawDateChanged -> handleRawDate(action.rawDigits)
            is AddItemAction.ExpiryPicked -> {
                val iso = dateTime.isoDateFromMillis(action.millis)
                val parts = iso.split("-")
                val rawDigits = if (parts.size == 3) "${parts[2]}${parts[1]}${parts[0]}" else ""
                _state.update {
                    it.copy(
                        rawDateDigits = rawDigits,
                        expiryDateMillis = action.millis,
                        expiryIsoDate = iso,
                        expiryError = null,
                    )
                }
            }
            is AddItemAction.PhotoPicked ->
                _state.update { it.copy(photoPath = action.path) }
            AddItemAction.ScanClicked -> onScanClicked()
            AddItemAction.ScannerLaunched -> _state.update { it.copy(requestScanner = false) }
            is AddItemAction.BarcodeScanned -> onBarcodeScanned(action.code)
            AddItemAction.ScanFailed ->
                showNotice(UiText.Res(Res.string.additem_scan_failed), NoticeKind.WARNING)
            AddItemAction.ClearBarcode -> _state.update { it.copy(barcode = null) }
            AddItemAction.DismissNotice -> {
                noticeJob?.cancel()
                _state.update { it.copy(notice = null) }
            }
            AddItemAction.Save -> save()
            AddItemAction.Dismiss -> { /* dismiss handled by UI layer directly */ }
        }
    }

    // ——— Scan barcode (fitur online) ———

    private fun onScanClicked() {
        if (_state.value.isLookingUp) return
        if (!connectivity.isOnline()) {
            // Offline: fitur ini tidak dipakai, tapi form tetap bisa diisi manual.
            showNotice(UiText.Res(Res.string.error_offline), NoticeKind.OFFLINE)
            return
        }
        _state.update { it.copy(requestScanner = true, notice = null) }
    }

    private fun onBarcodeScanned(code: String) {
        val barcode = code.trim()
        lookupJob?.cancel()
        noticeJob?.cancel()
        _state.update { it.copy(barcode = barcode, isLookingUp = true, notice = null) }
        lookupJob = viewModelScope.launch {
            lookupBarcode(barcode)
                .onSuccess { product ->
                    if (product == null) {
                        _state.update { it.copy(isLookingUp = false) }
                        showNotice(UiText.Res(Res.string.additem_scan_not_found), NoticeKind.INFO)
                    } else {
                        applyProduct(product)
                        showNotice(UiText.Res(Res.string.additem_scan_found), NoticeKind.SUCCESS)
                    }
                }
                .onError { err ->
                    _state.update { it.copy(isLookingUp = false) }
                    val kind = if (err is AppError.Offline || err is AppError.Network) NoticeKind.OFFLINE else NoticeKind.INFO
                    showNotice(errorMessages.provide(err), kind)
                }
        }
    }

    /** Mengisi form dari data produk. Isian yang sudah diketik pengguna tidak ditimpa. */
    private fun applyProduct(product: ProductInfo) {
        _state.update { s ->
            val category = product.categoryKey
                ?.takeIf { it != CategoryKeys.OTHER }
                ?.let { key -> s.dbCategories.firstOrNull { it.key == key } }
            s.copy(
                isLookingUp = false,
                name = if (s.name.isBlank()) product.displayName.orEmpty() else s.name,
                nameError = null,
                photoPath = s.photoPath ?: product.imageUrl,
                selectedCategoryId = category?.id ?: s.selectedCategoryId,
                selectedCategoryName = category?.name ?: s.selectedCategoryName,
                quantity = if (s.quantity == 0) (product.quantity ?: 1) else s.quantity,
                unit = if (s.quantity == 0 && product.unit != null) product.unit else s.unit,
            )
        }
    }

    private fun showNotice(text: UiText, kind: NoticeKind) {
        noticeJob?.cancel()
        _state.update { it.copy(notice = AddItemNotice(text, kind)) }
        noticeJob = viewModelScope.launch {
            delay(NOTICE_DURATION_MS)
            _state.update { it.copy(notice = null) }
        }
    }

    private fun handleRawDate(rawDigits: String) {
        val clamped = rawDigits.filter { it.isDigit() }.take(8)
        if (clamped.length < 8) {
            _state.update {
                it.copy(rawDateDigits = clamped, expiryDateMillis = null, expiryIsoDate = "", expiryError = null)
            }
            return
        }
        val day = clamped.substring(0, 2).toIntOrNull() ?: 0
        val month = clamped.substring(2, 4).toIntOrNull() ?: 0
        val year = clamped.substring(4, 8).toIntOrNull() ?: 0
        val iso = dateTime.isoFromDMY(day, month, year)
        val millis = if (iso != null) dateTime.millisFromDMY(day, month, year) else null
        _state.update {
            it.copy(
                rawDateDigits = clamped,
                expiryIsoDate = iso ?: "",
                expiryDateMillis = millis,
                expiryError = if (iso == null) UiText.Res(Res.string.error_invalid_date_format) else null,
            )
        }
    }

    private fun loadCategories() {
        viewModelScope.launch {
            getCategories()
                .onSuccess { cats ->
                    _state.update { s ->
                        val firstCat = cats.firstOrNull()
                        s.copy(
                            isLoadingCategories = false,
                            dbCategories = cats,
                            selectedCategoryId = s.selectedCategoryId ?: firstCat?.id,
                            selectedCategoryName = s.selectedCategoryName ?: firstCat?.name,
                        )
                    }
                }
                .onError {
                    _state.update { it.copy(isLoadingCategories = false) }
                }
        }
    }

    private fun save() {
        val s = _state.value
        if (!s.canSave) return

        _state.update { it.copy(isSaving = true, saveError = null) }
        viewModelScope.launch {
            var photoFailed = false
            val savedImagePath: String? = if (s.photoPath != null) {
                // Foto dari barcode masih berupa URL: diunduh dulu. Foto dari galeri disalin dan dikecilkan.
                val saved = if (s.photoPath.startsWith("https://")) downloadProductImage(s.photoPath)
                else saveProductImage(s.photoPath)
                when (val r = saved) {
                    is DataResult.Success -> r.data
                    is DataResult.Error -> { photoFailed = true; null }
                }
            } else null

            addItem(
                AddItemParams(
                    name = s.name.trim(),
                    categoryId = s.selectedCategoryId,
                    quantity = s.quantity,
                    unit = s.unit,
                    expiryDateMillis = s.expiryDateMillis,
                    imageUrl = savedImagePath,
                    barcode = s.barcode,
                )
            ).onSuccess {
                _state.update { s ->
                    val cached = s.dbCategories
                    AddItemUiState(
                        dbCategories = cached,
                        isLoadingCategories = false,
                        selectedCategoryId = cached.firstOrNull()?.id,
                        selectedCategoryName = cached.firstOrNull()?.name,
                    )
                }
                noticeJob?.cancel()
                syncReminders.requestSync()
                if (photoFailed) messenger.error(Res.string.msg_item_saved_photo_failed.asUiText())
                else messenger.success(Res.string.msg_item_saved.asUiText())
                _events.send(AddItemEvent.Saved)
            }.onError { err ->
                // Barang gagal disimpan: jangan biarkan foto yatim di penyimpanan.
                savedImagePath?.let { discardProductImage(it) }
                _state.update { it.copy(isSaving = false, saveError = errorMessages.provide(err)) }
            }
        }
    }

    private companion object {
        const val NOTICE_DURATION_MS = 6_000L
    }
}
