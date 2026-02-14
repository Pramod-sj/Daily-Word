package com.pramod.dailyword.di

import com.pramod.dailyword.framework.prefmanagers.EdgeToEdgeApplicator
import com.pramod.dailyword.framework.prefmanagers.EdgeToEdgeEnabler
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface Edge2EdgeModuleEntryPoint {

    fun edgeToEdgeEnabler(): EdgeToEdgeEnabler

    fun edgeToEdgeApplicator(): EdgeToEdgeApplicator

}