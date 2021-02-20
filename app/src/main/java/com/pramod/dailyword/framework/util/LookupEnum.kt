package com.pramod.dailyword.framework.util

import android.util.Log
import java.util.*
import kotlin.collections.ArrayList

class LookupEnum {
    companion object {
        val TAG = LookupEnum::class.simpleName

        fun <E : Enum<E>> lookUp(e: Class<E>, name: String, ignoreCase: Boolean = false): E? {

            try {
                for (i in e.enumConstants!!) {
                    if (i.name == name ||
                        (ignoreCase && i.name.toLowerCase(Locale.US) == name.toLowerCase(Locale.US))
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
                                (ignoreCase && i.name.toLowerCase(Locale.US) == name.toLowerCase(
                                    Locale.US
                                )))
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