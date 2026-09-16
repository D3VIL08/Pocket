package dev.pocket.core.common.di

import javax.inject.Qualifier

/**
 * Qualifies an injected [kotlinx.coroutines.CoroutineDispatcher]. Injecting dispatchers instead of
 * referencing `Dispatchers.IO` directly is what lets tests swap in a deterministic scheduler.
 */
@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class Dispatcher(val pocketDispatcher: PocketDispatchers)

enum class PocketDispatchers {
    Default,
    IO,
}

/** Qualifies the application-scoped [kotlinx.coroutines.CoroutineScope]. */
@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class ApplicationScope
