package com.pramod.dailyword.business.domain.util

interface EntityMapper<E, D> {
    fun fromEntity(entity: E): D
    fun toEntity(domain: D): E
}

interface EntityMapperV2<E, D> {

    suspend fun fromEntity(entity: E): D

    suspend fun toEntity(domain: D): E

}