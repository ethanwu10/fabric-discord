package dev.ethanwu.mc.fabricdiscord.util

import reactor.core.Disposable
import reactor.core.Disposables
import java.util.function.Supplier

class DisposableHolder<T : Disposable?> private constructor(
    private val inner: Disposable.Swap,
) : Disposable by inner, Supplier<T> {
    constructor(initial: T) : this(Disposables.swap()) {
        inner.update(initial)
    }

    fun update(new: T) = inner.update(new)
    fun replace(new: T) = inner.replace(new)

    @Suppress("UNCHECKED_CAST")
    override fun get(): T = inner.get() as T
}
