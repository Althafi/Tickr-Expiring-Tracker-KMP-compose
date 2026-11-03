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
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.project.tickr.presentation.common.ErrorMessageProvider
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
) : ViewModel() {

    private val _state = MutableStateFlow(AddItemUiState())
    val state: StateFlow<AddItemUiState> = _state.asStateFlow()

    private val _events = Channel<AddItemEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    init {
        loadCategories()
    }

    fun onAction(action: AddItemAction) {
        when (action) {
            AddItemAction.Reset -> {
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
            AddItemAction.Save -> save()
            AddItemAction.Dismiss -> { /* dismiss handled by UI layer directly */ }
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

        _state.update { it.copy(isSaving = true) }
        viewModelScope.launch {
            val savedImagePath: String? = if (s.photoPath != null) {
                when (val r = saveProductImage(s.photoPath)) {
                    is DataResult.Success -> r.data
                    is DataResult.Error -> null
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
                syncReminders.requestSync()
                _events.send(AddItemEvent.Saved)
            }.onError { err ->
                // Barang gagal disimpan: jangan biarkan foto yatim di penyimpanan.
                savedImagePath?.let { discardProductImage(it) }
                _state.update { it.copy(isSaving = false) }
                _events.send(AddItemEvent.ShowError(errorMessages.provide(err)))
            }
        }
    }
}
