package dev.ethanwu.mc.fabricdiscord.minecraft.util

import dev.ethanwu.mc.fabricdiscord.minecraft.proxy.NIL_UUID
import java.util.*

val UUID.nullable: UUID?
    get() = when (this) {
        NIL_UUID -> null
        else -> this
    }
