package fr.eseo.ld.ts.cinelog.hiltmodules


import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import fr.eseo.ld.ts.cinelog.network.YoutubeApi
import fr.eseo.ld.ts.cinelog.network.YoutubeApiService
import fr.eseo.ld.ts.cinelog.network.ImdbApiService
import fr.eseo.ld.ts.cinelog.network.ImdbApiServiceImpl
import fr.eseo.ld.ts.cinelog.network.OmdbApiService
import fr.eseo.ld.ts.cinelog.network.OmdbApiServiceImpl
import fr.eseo.ld.ts.cinelog.network.TmdbApiService
import fr.eseo.ld.ts.cinelog.network.TmdbApiServiceImpl
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideYoutubeApi(): YoutubeApiService = YoutubeApi.api

    @Provides
    @Singleton
    fun provideImdbApi(): ImdbApiService = ImdbApiServiceImpl.imdbApi

    @Provides
    @Singleton
    fun provideOmdbApi(): OmdbApiService = OmdbApiServiceImpl.omdbApi

    @Provides
    @Singleton
    fun provideTmdbApi(): TmdbApiService = TmdbApiServiceImpl.tmdbApi
}
