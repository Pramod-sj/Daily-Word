package com.pramod.dailyword.framework.transition

import android.view.View
import androidx.core.view.ViewCompat
import com.google.android.material.transition.platform.MaterialContainerTransformSharedElementCallback
import java.util.*

class CustomMaterialSharedCallback : MaterialContainerTransformSharedElementCallback() {
    private val mSharedElementViews: MutableList<View>
    fun setSharedElementViews(vararg sharedElementViews: View) {
        mSharedElementViews.clear()
        mSharedElementViews.addAll(listOf(*sharedElementViews))
    }

    override fun onMapSharedElements(
        names: MutableList<String>,
        sharedElements: MutableMap<String, View>
    ) {
        if (mSharedElementViews.isNotEmpty()) {
            removeObsoleteElements(names, sharedElements, mapObsoleteElements(names))
            for (sharedElementView in mSharedElementViews) {
                val transitionName = ViewCompat.getTransitionName(sharedElementView)
                names.add(transitionName!!)
                sharedElements[transitionName] = sharedElementView
            }
        }
    }


    /**
     * Maps all views that don't start with "android" namespace.
     *
     * @param names All shared element names.
     * @return The obsolete shared element names.
     */
    private fun mapObsoleteElements(names: List<String>): List<String> {
        val elementsToRemove: MutableList<String> = ArrayList(names.size)
        for (name in names) {
            if (name.startsWith("android")) continue
            elementsToRemove.add(name)
        }
        return elementsToRemove
    }

    /**
     * Removes obsolete elements from names and shared elements.
     *
     * @param names            Shared element names.
     * @param sharedElements   Shared elements.
     * @param elementsToRemove The elements that should be removed.
     */
    private fun removeObsoleteElements(
        names: MutableList<String>,
        sharedElements: MutableMap<String, View>,
        elementsToRemove: List<String>
    ) {
        if (elementsToRemove.isNotEmpty()) {
            names.removeAll(elementsToRemove)
            for (elementToRemove in elementsToRemove) {
                sharedElements.remove(elementToRemove)
            }
        }
    }


    init {
        mSharedElementViews = ArrayList()
    }
}