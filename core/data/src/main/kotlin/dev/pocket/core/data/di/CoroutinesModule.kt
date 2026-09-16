package dev.pocket.core.data.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dev.pocket.core.common.di.ApplicationScope
import dev.pocket.core.common.di.Dispatcher
import dev.pocket.core.common.di.PocketDispatchers
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import java.time.Clock
import javax.inject.Singleton

/**
 * Dispatchers, the application scope, and the clock are all injected rather than reached for
 * statically — that is what lets domain tests run on a deterministic scheduler and pin "today".
 */
@Module
@InstallIn(SingletonComponent::class)
internal object CoroutinesModule {

    @Provides
    @Dispatcher(PocketDispatchers.IO)
    fun provideIoDispatcher(): CoroutineDispatcher = Dispatchers.IO

    @Provides
    @Dispatcher(PocketDispatchers.Default)
    fun provideDefaultDispatcher(): CoroutineDispatcher = Dispatchers.Default

    @Provides
    @Singleton
    @ApplicationScope
    fun provideApplicationScope(
        @Dispatcher(PocketDispatchers.Default) dispatcher: CoroutineDispatcher,
    ): CoroutineScope = CoroutineScope(SupervisorJob() + dispatcher)

    @Provides
    @Singleton
    fun provideClock(): Clock = Clock.systemDefaultZone()
}
