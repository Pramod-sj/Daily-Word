package com.pramod.dailyword.di

import android.util.Log
import com.pramod.dailyword.framework.datasource.network.abstraction.IPNetworkService
import com.pramod.dailyword.framework.datasource.network.abstraction.WordNetworkService
import com.pramod.dailyword.framework.datasource.network.impl.IPNetworkServiceImpl
import com.pramod.dailyword.framework.datasource.network.impl.WordNetworkServiceImpl
import com.pramod.dailyword.framework.datasource.network.mappers.IPInfoNEMapper
import com.pramod.dailyword.framework.datasource.network.mappers.WordNEMapper
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
        /*val interceptor = HttpLoggingInterceptor()
        interceptor.setLevel(HttpLoggingInterceptor.Level.BASIC)*/
        return OkHttpClient.Builder()
            /*.addInterceptor(interceptor)*/
            .build()
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