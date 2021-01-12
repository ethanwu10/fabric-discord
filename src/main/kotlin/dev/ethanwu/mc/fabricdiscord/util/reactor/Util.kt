package dev.ethanwu.mc.fabricdiscord.util.reactor

import reactor.core.Disposable
import reactor.core.publisher.BaseSubscriber
import reactor.core.publisher.Flux
import reactor.core.publisher.SignalType
import reactor.core.publisher.Sinks

fun <T> linkDataOnly(flux: Flux<T>, sink: Sinks.Many<T>, emitFailureHandler: Sinks.EmitFailureHandler): Disposable =
    flux.subscribeWith(object : BaseSubscriber<T>() {
        val wrappedHandler = Sinks.EmitFailureHandler { signalType, emitResult ->
            emitFailureHandler.onEmitFailure(signalType, emitResult).also { shouldRetry ->
                if (signalType == SignalType.ON_NEXT) {
                    when (emitResult) {
                        Sinks.EmitResult.FAIL_TERMINATED, Sinks.EmitResult.FAIL_CANCELLED ->
                            if (!shouldRetry) {
                                cancel()
                            }
                        else -> {
                        }
                    }
                }
            }
        }

        override fun hookOnNext(value: T) {
            sink.emitNext(value, wrappedHandler)
        }
    })
