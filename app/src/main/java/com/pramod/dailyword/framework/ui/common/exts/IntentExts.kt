package com.pramod.dailyword.framework.ui.common.exts

import android.app.Activity
import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.pramod.dailyword.BuildConfig
import com.pramod.dailyword.R
import com.pramod.dailyword.business.domain.model.Word
import com.pramod.dailyword.framework.ui.aboutapp.AboutAppActivity
import com.pramod.dailyword.framework.ui.bookmarks.FavoriteWordsActivity
import com.pramod.dailyword.framework.ui.home.HomeActivity
import com.pramod.dailyword.framework.ui.notification_consent.NotificationConsentActivity
import com.pramod.dailyword.framework.ui.recap.RecapWordsActivity
import com.pramod.dailyword.framework.ui.settings.AppSettingActivity
import com.pramod.dailyword.framework.ui.splash_screen.SplashScreenActivity
import com.pramod.dailyword.framework.ui.worddetails.WordDetailedActivity
import com.pramod.dailyword.framework.ui.words.WordListActivity
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream


fun Activity.openSplashScreen(vararg flag: Int) {
    val intent = Intent(this, SplashScreenActivity::class.java)
    flag.forEach {
        intent.addFlags(it)
    }
    startActivity(intent)
}

fun ComponentActivity.openHomePage(withFadeAnimation: Boolean = false, finish: Boolean = false) {
    val intent = Intent(this, HomeActivity::class.java).apply {
        this@openHomePage.intent.extras?.let {
            Timber.i("openHomePage: $it")
            putExtras(it)
        }
    }
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


fun AppCompatActivity.openNotificationConsentPage(withFadeAnimation: Boolean = false, finish: Boolean = false) {
    val intent = Intent(this, NotificationConsentActivity::class.java).apply {
        this@openNotificationConsentPage.intent.extras?.let {
            Timber.i("openHomePage: $it")
            putExtras(it)
        }
    }
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
    finish: Boolean = false,
    word: Word? = null
) {
    val intent = Intent(this, WordDetailedActivity::class.java)
    val bundle = Bundle()
    bundle.putString("WORD_DATE", wordDate)
    bundle.putSerializable("WORD", word)
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

fun Activity.openBookmarksPage() {
    startActivity(Intent(this, FavoriteWordsActivity::class.java))
}

fun Activity.openRecapPage() {
    startActivity(Intent(this, RecapWordsActivity::class.java))
}


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
            type = "text/plain"
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
        FileProvider.getUriForFile(
            context,
            BuildConfig.APPLICATION_ID + ".fileprovider",
            file.absoluteFile
        )
    } catch (e: Exception) {
        null
    }
}

inline fun <T1 : Any, T2 : Any, R : Any> safeLet(p1: T1?, p2: T2?, block: (T1, T2) -> R?): R? {
    return if (p1 != null && p2 != null) block(p1, p2) else null
}

inline fun <T1 : Any, T2 : Any, T3 : Any, R : Any> safeLet(
    p1: T1?,
    p2: T2?,
    p3: T3?,
    block: (T1, T2, T3) -> R?
): R? {
    return if (p1 != null && p2 != null && p3 != null) block(p1, p2, p3) else null
}

inline fun <T1 : Any, T2 : Any, T3 : Any, T4 : Any, R : Any> safeLet(
    p1: T1?,
    p2: T2?,
    p3: T3?,
    p4: T4?,
    block: (T1, T2, T3, T4) -> R?
): R? {
    return if (p1 != null && p2 != null && p3 != null && p4 != null) block(p1, p2, p3, p4) else null
}

inline fun <T1 : Any, T2 : Any, T3 : Any, T4 : Any, T5 : Any, R : Any> safeLet(
    p1: T1?,
    p2: T2?,
    p3: T3?,
    p4: T4?,
    p5: T5?,
    block: (T1, T2, T3, T4, T5) -> R?
): R? {
    return if (p1 != null && p2 != null && p3 != null && p4 != null && p5 != null) block(
        p1,
        p2,
        p3,
        p4,
        p5
    ) else null
}

