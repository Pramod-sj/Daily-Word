package com.pramod.dailyword.di

import com.pramod.dailyword.BuildConfig
import com.pramod.dailyword.framework.datasource.network.abstraction.IPNetworkService
import com.pramod.dailyword.framework.datasource.network.abstraction.WordNetworkService
import com.pramod.dailyword.framework.datasource.network.impl.IPNetworkServiceImpl
import com.pramod.dailyword.framework.datasource.network.impl.WordNetworkServiceImpl
import com.pramod.dailyword.framework.datasource.network.mappers.IPInfoNEMapper
import com.pramod.dailyword.framework.datasource.network.mappers.WordNEMapper
import com.pramod.dailyword.framework.datasource.network.service.IPService
import com.pramod.dailyword.framework.datasource.network.service.WordApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton


@Module
@InstallIn(value = [SingletonComponent::class])
object NetworkModule {

    @JvmStatic
    @Singleton
    @Provides
    fun provideOkHttpClient(): OkHttpClient {
        /*val interceptor = HttpLoggingInterceptor()
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY)*/
        return OkHttpClient.Builder()
            //.addInterceptor(interceptor)
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
    fun provideRetrofit(
        okHttpClient: OkHttpClient,
        gsonConverterFactory: GsonConverterFactory
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BuildConfig.API_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(gsonConverterFactory)
            .build()
    }

    @JvmStatic
    @Singleton
    @Provides
    fun provideWordApiService(retrofit: Retrofit): WordApiService {
        return retrofit.create(WordApiService::class.java)
    }

    @JvmStatic
    @Singleton
    @Provides
    fun provideWordNetworkService(
        wordApiService: WordApiService,
        wordNEMapper: WordNEMapper
    ): WordNetworkService {
        return WordNetworkServiceImpl(
            wordApiService, wordNEMapper
        )
    }


    @JvmStatic
    @Singleton
    @Provides
    fun provideIPService(retrofit: Retrofit): IPService {
        return retrofit.create(IPService::class.java)
    }

    @JvmStatic
    @Singleton
    @Provides
    fun provideIPNetworkService(
        ipService: IPService,
        ipInfoNEMapper: IPInfoNEMapper
    ): IPNetworkService {
        return IPNetworkServiceImpl(
            ipService, ipInfoNEMapper
        )
    }

}