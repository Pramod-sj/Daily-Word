package com.pramod.dailyword.util

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import androidx.core.content.ContextCompat
import com.pramod.dailyword.R

/*
fun Context.shareApp() {
    val intent = Intent(Intent.ACTION_SEND)
        .apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, resources.getString(R.string.app_name))
            putExtra(
                Intent.EXTRA_TEXT,
                resources.getString(R.string.share_text) + "\n" + resources.getString(R.string.google_app_url)
            )
        }
    startActivity(Intent.createChooser(intent, "Choose app..."))
}*/

fun Activity.shareApp(
    text: String = resources.getString(R.string.share_text) + "\n" + resources.getString(
        R.string.google_app_url
    ), bitmap: Bitmap? = null
) {
    val share = Intent(Intent.ACTION_SEND)
    share.putExtra(Intent.EXTRA_TEXT, text)
    bitmap?.let {
        share.type = "image/*"
        val imageToShare: Uri = Uri.parse(
            MediaStore.Images.Media.insertImage(
                contentResolver,
                bitmap,
                "Share",
                null
            )
        )
        share.putExtra(Intent.EXTRA_STREAM, imageToShare)
    }
    ContextCompat.startActivity(this, Intent.createChooser(share, "Choose app..."), null)
}