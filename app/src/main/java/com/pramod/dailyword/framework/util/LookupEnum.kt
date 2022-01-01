package com.pramod.dailyword.framework.util

import timber.log.Timber

class LookupEnum {
    companion object {
        val TAG = LookupEnum::class.simpleName

        fun <E : Enum<E>> lookUp(e: Class<E>, name: String, ignoreCase: Boolean = false): E? {

            try {
                for (i in e.enumConstants!!) {
                    if (i.name == name ||
                        (ignoreCase && i.name.equals(name, ignoreCase = true))
                    )
                        return i
                }

            } catch (e: Exception) {
                Timber.i( "lookUp: $e")
                return null
            }
            return null
        }

        fun <E : Enum<E>> getAllEnumExcept(
            e: Class<E>,
            name: String,
            ignoreCase: Boolean = false
        ): List<E> {
            val enumExcept = ArrayList<E>()

            try {
                for (i in e.enumConstants!!) {
                    if (!(i.name == name ||
                                (ignoreCase && i.name.equals(name, ignoreCase = true)))
                    )
                        enumExcept.add(i)

                }

            } catch (e: Exception) {
                Timber.i( "lookUp: $e")
            }
            return enumExcept
        }

    }

}