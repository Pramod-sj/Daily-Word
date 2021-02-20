package com.pramod.dailyword.business.domain.util

interface EntityMapper<E, D> {
    fun fromEntity(entity: E): D
    fun toEntity(domain: D): E
}