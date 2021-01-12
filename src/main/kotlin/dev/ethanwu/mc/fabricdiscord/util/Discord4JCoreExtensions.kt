package dev.ethanwu.mc.fabricdiscord.util

import discord4j.core.event.EventDispatcher
import discord4j.core.event.domain.Event
import reactor.core.publisher.Flux

inline fun <reified T : Event> EventDispatcher.on(): Flux<T> = on(T::class.java)
