package dev.ethanwu.mc.fabricdiscord.util.reactor

import org.slf4j.LoggerFactory
import reactor.core.Fuseable
import org.slf4j.Logger as Slf4jLogger
import reactor.util.Logger as ReactorLogger

class Logger(private val logger: Slf4jLogger) : ReactorLogger {
    constructor(name: String) : this(LoggerFactory.getLogger(name))

    companion object {
        val factory: (String) -> ReactorLogger = { Logger(it) }
    }

    private fun cleanArguments(arguments: Array<out Any?>): Array<out Any?> {
        return arguments.map {
            if (it is Fuseable.QueueSubscription<*>) {
                // QueueSubscription is a subclass of Collection but does not
                // actually implement the interface, so stringify it to avoid
                // an exception from the underlying logger trying to print it as
                // a Collection
                it.toString()
            } else {
                it
            }
        }.toTypedArray()
    }

    override fun getName(): String = logger.name

    override fun isTraceEnabled(): Boolean = logger.isTraceEnabled

    override fun trace(msg: String) = logger.trace(msg)

    override fun trace(format: String, vararg arguments: Any?) = logger.trace(format, *cleanArguments(arguments))

    override fun trace(msg: String, t: Throwable) = logger.trace(msg, t)

    override fun isDebugEnabled(): Boolean = logger.isDebugEnabled

    override fun debug(msg: String) = logger.debug(msg)

    override fun debug(format: String, vararg arguments: Any?) = logger.debug(format, *cleanArguments(arguments))

    override fun debug(msg: String, t: Throwable) = logger.debug(msg, t)

    override fun isInfoEnabled(): Boolean = logger.isInfoEnabled

    override fun info(msg: String) = logger.info(msg)

    override fun info(format: String, vararg arguments: Any?) = logger.info(format, *cleanArguments(arguments))

    override fun info(msg: String, t: Throwable) = logger.info(msg, t)

    override fun isWarnEnabled(): Boolean = logger.isWarnEnabled

    override fun warn(msg: String) = logger.warn(msg)

    override fun warn(format: String, vararg arguments: Any?) = logger.warn(format, *cleanArguments(arguments))

    override fun warn(msg: String, t: Throwable) = logger.warn(msg, t)

    override fun isErrorEnabled(): Boolean = logger.isErrorEnabled

    override fun error(msg: String) = logger.error(msg)

    override fun error(format: String, vararg arguments: Any?) = logger.error(format, *cleanArguments(arguments))

    override fun error(msg: String, t: Throwable) = logger.error(msg, t)
}
