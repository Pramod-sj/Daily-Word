package com.pramod.dailyword.framework.util

import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Handler
import android.os.Looper
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import timber.log.Timber
import java.io.File

const val TAG_GLIDE_UTILS = "GlideUtils"

/**
 * @param url url of image to be loaded
 * @param cacheCallback true -> cache and false -> not cache
 */
fun Context.isImageCached(url: String, cacheCallback: (isCached: Boolean) -> Unit) {
    Glide.with(this)
        .load(url)
        .apply(RequestOptions().onlyRetrieveFromCache(true))
        .addListener(object : RequestListener<Drawable> {
            override fun onLoadFailed(
                e: GlideException?,
                model: Any?,
                target: Target<Drawable>?,
                isFirstResource: Boolean
            ): Boolean {
                Handler(Looper.getMainLooper()).post {
                    cacheCallback.invoke(false)
                }
                return true
            }

            override fun onResourceReady(
                resource: Drawable?,
                model: Any?,
                target: Target<Drawable>?,
                dataSource: DataSource?,
                isFirstResource: Boolean
            ): Boolean {
                Handler(Looper.getMainLooper()).post {
                    cacheCallback.invoke(true)
                }

                return true
            }

        })
        .submit()
}


fun Context.preloadImage(
    url: String,
    preloadCallback: (isPreloadSucceed: Boolean) -> Unit
) {
    Glide.with(this)
        .downloadOnly()
        .load(url)
        .addListener(object : RequestListener<File> {
            override fun onLoadFailed(
                e: GlideException?,
                model: Any?,
                target: Target<File>?,
                isFirstResource: Boolean
            ): Boolean {
                Timber.i("onLoadFailed: " + e?.message)
                Handler(Looper.getMainLooper()).post {
                    preloadCallback.invoke(false)
                }
                return true
            }

            override fun onResourceReady(
                resource: File?,
                model: Any?,
                target: Target<File>?,
                dataSource: DataSource?,
                isFirstResource: Boolean
            ): Boolean {
                Timber.i("onResourceReady: ")
                Handler(Looper.getMainLooper()).post {
                    preloadCallback.invoke(true)
                }
                return true
            }

        }).submit()


}