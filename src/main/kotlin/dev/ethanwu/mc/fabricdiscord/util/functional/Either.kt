package dev.ethanwu.mc.fabricdiscord.util.functional

class Either<out L, out R> private constructor(
    private val l: L?,
    private val r: R?,
) {
    companion object {
        fun <R, L> left(l: L) = Either<L, R>(l, null)
        fun <L, R> right(r: R) = Either<L, R>(null, r)
    }

    init {
        assert((l == null) != (r == null))
    }

    val isRight: Boolean
        get() = r != null
    val isLeft: Boolean
        inline get() = !isRight

    fun <R2> map(mapper: (R) -> R2): Either<L, R2> = if (r != null) {
        Either(null, mapper(r))
    } else {
        Either(l, null)
    }

    fun <L2, R2> bimap(mapperL: (L) -> L2, mapperR: (R) -> R2): Either<L2, R2> = if (r != null) {
        Either(null, mapperR(r))
    } else {
        Either(mapperL(l!!), null)
    }

    fun fromLeft(): L = l!!
    fun fromRight(): R = r!!
}

fun <Base, L : Base, R : Base> Either<L, R>.toCommon(): Base =
    if (isRight) fromRight() else fromLeft()
