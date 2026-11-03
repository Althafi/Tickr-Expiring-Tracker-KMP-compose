package com.project.tickr.presentation.help

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import tickrapp.shared.generated.resources.*

class HelpViewModel : ViewModel() {

    private val _state = MutableStateFlow(
        HelpUiState(
            faqs = listOf(
                FaqItem(
                    id = "1",
                    question = Res.string.help_faq_q1,
                    answer = Res.string.help_faq_a1,
                ),
                FaqItem(
                    id = "2",
                    question = Res.string.help_faq_q2,
                    answer = Res.string.help_faq_a2,
                ),
                FaqItem(
                    id = "3",
                    question = Res.string.help_faq_q3,
                    answer = Res.string.help_faq_a3,
                ),
            )
        )
    )
    val state: StateFlow<HelpUiState> = _state.asStateFlow()

    private val _events = Channel<HelpEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    fun onAction(action: HelpAction) {
        when (action) {
            is HelpAction.ToggleFaq -> _state.update { s ->
                s.copy(expandedId = if (s.expandedId == action.id) null else action.id)
            }
            HelpAction.ContactEmail -> viewModelScope.launch {
                _events.send(HelpEvent.OpenExternal("mailto:support@tickr.app")) // TODO(user): sesuaikan alamat email
            }
            HelpAction.ContactWhatsApp -> viewModelScope.launch {
                _events.send(HelpEvent.OpenExternal("https://wa.me/628123456789")) // TODO(user): sesuaikan nomor WhatsApp
            }
            HelpAction.Back -> viewModelScope.launch {
                _events.send(HelpEvent.NavigateBack)
            }
        }
    }
}
