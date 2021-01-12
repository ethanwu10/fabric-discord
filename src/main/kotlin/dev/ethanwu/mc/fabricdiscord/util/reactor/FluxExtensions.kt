package dev.ethanwu.mc.fabricdiscord.util.reactor

import dev.ethanwu.mc.fabricdiscord.util.functional.Either
import reactor.core.publisher.Flux
import reactor.core.publisher.GroupedFlux
import kotlin.reflect.KClass

internal fun <A, B> Flux<A>.groupByType(clazz: Class<B>): Flux<Either<Flux<A>, Flux<B>>> =
    groupBy { clazz.isInstance(it) }
        .map {
            if (it.key()) {
                @Suppress("UNCHECKED_CAST")
                Either.right(it as GroupedFlux<Boolean, B>)
            } else {
                Either.left(it)
            }
        }

internal inline fun <reified B, A> Flux<A>.groupByType() =
    groupByType(B::class.java)

internal inline fun <A, B : Any> Flux<A>.groupByType(clazz: KClass<B>) =
    groupByType(clazz.java)
