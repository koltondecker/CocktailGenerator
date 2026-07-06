package com.koltondecker.cocktailgenerator.data.local

import android.content.Context
import androidx.room.Room
import com.koltondecker.cocktailgenerator.data.local.dao.PantryDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object LocalDatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext ctx: Context): AppDatabase =
        Room.databaseBuilder(ctx, AppDatabase::class.java, "cocktail_generator.db")
            // Empty catalog until first refresh — no migration story needed yet.
            // Room 2.6.x's no-arg form; upgrade to `dropAllTables=true` when we
            // move to Room 2.7+.
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    fun providePantryDao(db: AppDatabase): PantryDao = db.pantryDao()
}
