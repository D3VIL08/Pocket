package dev.pocket.core.common.result

/**
 * The outcome of an operation that can fail in a way the UI must explain.
 *
 * Kotlin's own `Result` is deliberately not used across layer boundaries: it cannot be returned
 * from suspend functions without opt-ins, and it carries a `Throwable` where the UI needs a typed
 * reason it can turn into a message.
 */
sealed interface Outcome<out T> {
    data class Success<T>(val value: T) : Outcome<T>
    data class Failure(val reason: FailureReason) : Outcome<Nothing>
}

/** Why an operation failed, in terms the presentation layer can act on. */
sealed interface FailureReason {
    /** Input did not pass a domain rule. [message] is safe to show to the user. */
    data class Validation(val field: Field, val message: String) : FailureReason {
        enum class Field { AMOUNT, CATEGORY, DATE, NOTE }
    }

    /** A referenced row no longer exists. */
    data class NotFound(val what: String) : FailureReason

    /** Anything unexpected — storage errors and the like. */
    data class Unexpected(val cause: Throwable) : FailureReason
}

inline fun <T, R> Outcome<T>.map(transform: (T) -> R): Outcome<R> = when (this) {
    is Outcome.Success -> Outcome.Success(transform(value))
    is Outcome.Failure -> this
}

inline fun <T> Outcome<T>.onSuccess(action: (T) -> Unit): Outcome<T> = apply {
    if (this is Outcome.Success) action(value)
}

inline fun <T> Outcome<T>.onFailure(action: (FailureReason) -> Unit): Outcome<T> = apply {
    if (this is Outcome.Failure) action(reason)
}

fun <T> Outcome<T>.getOrNull(): T? = (this as? Outcome.Success)?.value
