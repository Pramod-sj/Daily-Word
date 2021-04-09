package com.pramod.dailyword.framework.util

import android.util.Log

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
                Log.i(TAG, "lookUp: $e")
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
                Log.i(TAG, "lookUp: $e")
            }
            return enumExcept
        }

    }

}