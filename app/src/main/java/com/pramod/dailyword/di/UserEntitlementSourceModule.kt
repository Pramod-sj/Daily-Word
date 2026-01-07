package com.pramod.dailyword.di

import com.pramod.dailyword.framework.userEntitlement.DonationEntitlementSource
import com.pramod.dailyword.framework.userEntitlement.EntitlementSource
import com.pramod.dailyword.framework.userEntitlement.RewardAdEntitlementSource
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet


@Module
@InstallIn(value = [SingletonComponent::class])
interface UserEntitlementSourceModule {

    @Binds
    @IntoSet
    fun provideRewardAdEntitlementSource(
        rewardAdEntitlementSource: RewardAdEntitlementSource
    ): EntitlementSource

    @Binds
    @IntoSet
    fun provideDonationEntitlementSource(
        donationEntitlementSource: DonationEntitlementSource
    ): EntitlementSource


}