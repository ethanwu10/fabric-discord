package dev.ethanwu.mc.fabricdiscord.util

import java.util.*

fun <T> Optional<T>.orElseNull(): T? = this.orElse(null)
