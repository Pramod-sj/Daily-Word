package com.pramod.dailyword.framework.ui.common.exts

import android.app.Activity
import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.paging.ExperimentalPagingApi
import com.pramod.dailyword.BuildConfig
import com.pramod.dailyword.R
import com.pramod.dailyword.framework.ui.aboutapp.AboutAppActivity
import com.pramod.dailyword.framework.ui.bookmarks.FavoriteWordsActivity
import com.pramod.dailyword.framework.ui.home.HomeActivity
import com.pramod.dailyword.framework.ui.recap.RecapWordsActivity
import com.pramod.dailyword.framework.ui.settings.AppSettingActivity
import com.pramod.dailyword.framework.ui.splash_screen.SplashScreenActivity
import com.pramod.dailyword.framework.ui.worddetails.WordDetailedActivity
import com.pramod.dailyword.framework.ui.words.WordListActivity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import java.io.File
import java.io.FileOutputStream


@ExperimentalCoroutinesApi
@ExperimentalPagingApi
fun Activity.openSplashScreen(vararg flag: Int) {
    val intent = Intent(this, SplashScreenActivity::class.java)
    flag.forEach {
        intent.addFlags(it)
    }
    startActivity(intent)
}

@ExperimentalCoroutinesApi
@ExperimentalPagingApi
fun AppCompatActivity.openHomePage(withFadeAnimation: Boolean = false, finish: Boolean = false) {
    val intent = Intent(this, HomeActivity::class.java)
    if (withFadeAnimation) {
        /*overridePendingTransition(
            android.R.anim.fade_in,
            android.R.anim.fade_out
        )*/
        val option = ActivityOptions.makeCustomAnimation(
                this,
                android.R.anim.fade_in,
                android.R.anim.fade_out
        )
        startActivity(intent, option.toBundle())
    } else {
        startActivity(intent)
    }
    if (finish) {
        finish()
    }
}


fun Activity.openWordDetailsPage(
        wordDate: String,
        option: ActivityOptions? = null,
        shouldAnimate: Boolean? = false,
        finish: Boolean = false
) {
    val intent = Intent(this, WordDetailedActivity::class.java)
    val bundle = Bundle()
    bundle.putString("WORD_DATE", wordDate)
    intent.putExtras(bundle)
    if (option != null && shouldAnimate == true) {
        startActivity(intent, option.toBundle())
    } else {
        startActivity(intent)
    }
    if (finish) {
        finish()
    }
}

fun Activity.openWordListPage() {
    startActivity(Intent(this, WordListActivity::class.java))
}

@ExperimentalPagingApi
fun Activity.openBookmarksPage() {
    startActivity(Intent(this, FavoriteWordsActivity::class.java))
}

fun Activity.openRecapPage() {
    startActivity(Intent(this, RecapWordsActivity::class.java))
}

@ExperimentalCoroutinesApi
fun Activity.openRandomWordPage() {
    startActivity(Intent(this, WordDetailedActivity::class.java))
}

fun Activity.openSettingPage() {
    val intent = Intent(this, AppSettingActivity::class.java)
    startActivity(intent)
}

fun Activity.openAboutPage() {
    val intent = Intent(this, AboutAppActivity::class.java)
    startActivity(intent)
}

fun Activity.shareApp(
        text: String = resources.getString(R.string.share_text) + "\n" + resources.getString(
                R.string.google_app_url
        ), bitmap: Bitmap? = null
) {

    val shareWithImage = Intent.createChooser(Intent().apply {
        action = Intent.ACTION_SEND

        if (bitmap == null) {
            type = "text/plain";
            putExtra(Intent.EXTRA_TITLE, "Share Daily Word with your friends and family today!")
        } else {
            putExtra(Intent.EXTRA_STREAM, getUriFromBitmap(this@shareApp, bitmap))
            type = "image/*"
        }

        putExtra(Intent.EXTRA_TEXT, text)

    }, "Choose app")

    ContextCompat.startActivity(this, shareWithImage, null)
}


fun getUriFromBitmap(context: Context, bitmap: Bitmap): Uri? {
    return try {
        val file = File(context.externalCacheDir, "${System.currentTimeMillis()}.jpg")
        val fos = FileOutputStream(file)
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, fos)
        FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + ".fileprovider", file.absoluteFile)
    } catch (e: Exception) {
        null
    }
}

