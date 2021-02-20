package com.pramod.dailyword.framework.util

import android.graphics.*


object GradientUtils {
    fun addGradient(originalBitmap: Bitmap, startColor: Int, endColor: Int): Bitmap? {
        val width = originalBitmap.width
        val height = originalBitmap.height
        val updatedBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(updatedBitmap)
        canvas.drawBitmap(originalBitmap, 0f, 0f, null)
        val paint = Paint()
        val shader =
            LinearGradient(
                0f,
                0f,
                0f,
                height.toFloat(),
                startColor,
                endColor,
                Shader.TileMode.CLAMP
            )
        paint.shader = shader
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
        return updatedBitmap
    }
}