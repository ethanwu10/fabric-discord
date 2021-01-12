package dev.ethanwu.mc.fabricdiscord.util

/**
 * Returns a function which calls the specified function [block] with its argument as the receiver
 *
 * Useful as the argument to a Java function which takes a lambda with a single Builder/Spec argument
 */
fun <T> spec(block: T.() -> Unit): (T) -> Unit = {
    it.block()
}
