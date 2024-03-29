package com.pramod.dailyword.di

import com.pramod.dailyword.BuildConfig
import com.pramod.dailyword.framework.datasource.network.service.IPService
import com.pramod.dailyword.framework.datasource.network.service.WordApiService
import com.pramod.dailyword.framework.firebase.FBRemoteConfig
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
@InstallIn(value = [SingletonComponent::class])
object NetworkModule {

    @JvmStatic
    @Singleton
    @Provides
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder().apply {
            if (BuildConfig.DEBUG) {
                addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BASIC))
            }
        }.build()
    }

    @JvmStatic
    @Singleton
    @Provides
    fun provideGsonConverterFactory(): GsonConverterFactory {
        return GsonConverterFactory.create()
    }

    @JvmStatic
    @Singleton
    @Provides
    fun provideScalarConverterFactory(): ScalarsConverterFactory {
        return ScalarsConverterFactory.create()
    }


    @JvmStatic
    @GsonRetrofitClient
    @Singleton
    @Provides
    fun provideGsonRetrofit(
        okHttpClient: OkHttpClient,
        gsonConverterFactory: GsonConverterFactory,
        fbRemoteConfig: FBRemoteConfig
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl(fbRemoteConfig.baseUrl())
            .client(okHttpClient)
            .addConverterFactory(gsonConverterFactory)
            .build()
    }

    @JvmStatic
    @ScalarRetrofitClient
    @Singleton
    @Provides
    fun provideScalarRetrofit(
        okHttpClient: OkHttpClient,
        scalarsConverterFactory: ScalarsConverterFactory,
        gsonConverterFactory: GsonConverterFactory,
        fbRemoteConfig: FBRemoteConfig
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl(fbRemoteConfig.baseUrl())
            .client(okHttpClient)
            .addConverterFactory(scalarsConverterFactory)
            .addConverterFactory(gsonConverterFactory)
            .build()
    }

    @JvmStatic
    @Singleton
    @Provides
    fun provideWordApiService(@GsonRetrofitClient retrofit: Retrofit): WordApiService {
        return retrofit.create(WordApiService::class.java)
    }

    @JvmStatic
    @Singleton
    @Provides
    fun provideIPService(@ScalarRetrofitClient retrofit: Retrofit): IPService {
        return retrofit.create(IPService::class.java)
    }

}