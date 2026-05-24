package com.pramod.dailyword.network.di

import com.pramod.dailyword.network.EndpointProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Singleton
    @Provides
    @NetworkCoreInstance
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder().apply {
            //if (BuildConfig.DEBUG) {
            addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BASIC))
            //}
        }.build()
    }

    @Singleton
    @Provides
    @NetworkCoreInstance
    fun provideGsonConverterFactory(): GsonConverterFactory = GsonConverterFactory.create()


    @JvmStatic
    @Singleton
    @Provides
    @NetworkCoreInstance
    fun provideScalarConverterFactory(): ScalarsConverterFactory = ScalarsConverterFactory.create()


    @JvmStatic
    @Singleton
    @Provides
    @NetworkCoreInstance
    fun provideGsonRetrofit(
        @NetworkCoreInstance okHttpClient: OkHttpClient,
        @NetworkCoreInstance gsonConverterFactory: GsonConverterFactory,
        @NetworkCoreInstance endpointProvider: EndpointProvider
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl(endpointProvider.getBaseUrl())
            .client(okHttpClient)
            .addConverterFactory(gsonConverterFactory)
            .build()
    }

}