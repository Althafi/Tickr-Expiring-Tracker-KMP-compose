package com.project.tickr.presentation

import com.project.tickr.presentation.common.AppMessenger
import com.project.tickr.presentation.common.MessageKind
import com.project.tickr.presentation.common.UiText
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals

class AppMessengerTest {

    @Test
    fun messages_sent_before_a_collector_exists_are_delivered_in_order() = runBlocking<Unit> {
        val messenger = AppMessenger()
        messenger.success(UiText.Raw("satu"))
        messenger.error(UiText.Raw("dua"))

        val received = messenger.messages.take(2).toList()
        assertEquals(listOf("satu", "dua"), received.map { (it.text as UiText.Raw).value })
        assertEquals(listOf(MessageKind.SUCCESS, MessageKind.ERROR), received.map { it.kind })
    }

    @Test
    fun overflow_drops_the_oldest_messages_instead_of_blocking() = runBlocking<Unit> {
        val messenger = AppMessenger()
        repeat(40) { messenger.success(UiText.Raw("m$it")) } // pengirim tidak pernah terblokir

        val kept = messenger.messages.take(16).toList().map { (it.text as UiText.Raw).value }
        assertEquals((24..39).map { "m$it" }, kept)
    }
}
