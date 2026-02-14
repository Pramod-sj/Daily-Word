package com.pramod.dailyword.framework.userEntitlement

sealed class Entitlement {

    object AdFree : Entitlement()

    data class AdFreeUntil(val expiry: Long) : Entitlement()

}
