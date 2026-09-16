package dev.pocket.core.data.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dev.pocket.core.data.repository.DataStoreUserPreferencesRepository
import dev.pocket.core.data.repository.OfflineFirstCategoryRepository
import dev.pocket.core.data.repository.OfflineFirstExpenseRepository
import dev.pocket.core.domain.repository.CategoryRepository
import dev.pocket.core.domain.repository.ExpenseRepository
import dev.pocket.core.domain.repository.UserPreferencesRepository
import javax.inject.Singleton

/**
 * Binds the domain's repository interfaces to their Room-backed implementations. This module is
 * the only place in the app that knows which implementation is in use, which is what makes the
 * V2 sync swap a one-file change.
 */
@Module
@InstallIn(SingletonComponent::class)
internal abstract class DataModule {

    @Binds
    @Singleton
    abstract fun bindExpenseRepository(impl: OfflineFirstExpenseRepository): ExpenseRepository

    @Binds
    @Singleton
    abstract fun bindCategoryRepository(impl: OfflineFirstCategoryRepository): CategoryRepository

    @Binds
    @Singleton
    abstract fun bindUserPreferencesRepository(
        impl: DataStoreUserPreferencesRepository,
    ): UserPreferencesRepository
}
