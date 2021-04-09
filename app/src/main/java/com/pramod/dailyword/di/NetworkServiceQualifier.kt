package com.pramod.dailyword.di

import java.lang.annotation.RetentionPolicy
import javax.inject.Qualifier


@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class ScalarRetrofitClient

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class GsonRetrofitClient