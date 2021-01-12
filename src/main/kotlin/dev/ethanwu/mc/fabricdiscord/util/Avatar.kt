package dev.ethanwu.mc.fabricdiscord.util

import java.util.*

object Avatar {
    fun avatarForUuid(uuid: UUID): String {
        return "https://crafatar.com/avatars/$uuid.png?overlay"
    }
}
