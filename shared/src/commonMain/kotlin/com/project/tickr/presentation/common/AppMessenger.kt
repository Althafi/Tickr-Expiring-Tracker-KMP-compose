package com.project.tickr.presentation.common

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow

enum class MessageKind { SUCCESS, ERROR }

/**
 * Pesan singkat untuk pengguna (snackbar). [onAction] dijalankan bila pengguna menekan [actionLabel],
 * mis. "Urungkan". Aksinya harus mandiri: ViewModel pengirim mungkin sudah tidak ada saat ditekan.
 */
data class AppMessage(
    val text: UiText,
    val kind: MessageKind = MessageKind.SUCCESS,
    val actionLabel: UiText? = null,
    val onAction: (suspend () -> Unit)? = null,
)

/**
 * Jalur tunggal pesan lintas layar. Pesan yang dikirim sebelum ada host yang menampilkan
 * (mis. saat berpindah layar) ditahan dan ditampilkan begitu host siap.
 */
class AppMessenger {
    private val channel = Channel<AppMessage>(capacity = 16, onBufferOverflow = BufferOverflow.DROP_OLDEST)

    val messages: Flow<AppMessage> = channel.receiveAsFlow()

    fun show(message: AppMessage) {
        channel.trySend(message)
    }

    fun success(text: UiText, actionLabel: UiText? = null, onAction: (suspend () -> Unit)? = null) =
        show(AppMessage(text, MessageKind.SUCCESS, actionLabel, onAction))

    fun error(text: UiText) = show(AppMessage(text, MessageKind.ERROR))
}
