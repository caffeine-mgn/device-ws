package pw.binom

fun illegalArgument(lazyMessage: () -> String): Nothing {
    throw IllegalArgumentException(lazyMessage())
}