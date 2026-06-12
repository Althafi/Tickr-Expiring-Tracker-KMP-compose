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

class HelpViewModel : ViewModel() {

    private val _state = MutableStateFlow(
        HelpUiState(
            faqs = listOf(
                FaqItem(
                    id = "1",
                    question = "Bagaimana cara menambahkan barang baru?", // TODO(user): gunakan string resource help_faq_q1
                    answer = "Tekan tombol tambah (+) di bilah bawah, isi nama barang, kategori, dan tanggal kedaluwarsa, lalu simpan.", // TODO(user): help_faq_a1
                ),
                FaqItem(
                    id = "2",
                    question = "Kapan saya menerima pengingat kedaluwarsa?", // TODO(user): help_faq_q2
                    answer = "Tickr mengirim notifikasi bertahap sesuai tingkat urgensi: aman, peringatan, dan kritis menjelang tanggal kedaluwarsa.", // TODO(user): help_faq_a2
                ),
                FaqItem(
                    id = "3",
                    question = "Apakah data saya tersimpan aman?", // TODO(user): help_faq_q3
                    answer = "Data inventaris Anda tersimpan pada perangkat dan akun Anda. Perbarui kata sandi secara berkala lewat menu Keamanan Akun.", // TODO(user): help_faq_a3
                ),
                FaqItem(
                    id = "4",
                    question = "Bagaimana cara mengubah kata sandi?", // TODO(user): help_faq_q4
                    answer = "Buka Profil, pilih Edit Profil, lalu tekan Ubah Kata Sandi dan ikuti instruksinya.", // TODO(user): help_faq_a4
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
