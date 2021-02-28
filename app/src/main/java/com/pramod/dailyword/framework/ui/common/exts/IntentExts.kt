package com.pramod.dailyword.framework.ui.common.exts

import android.app.Activity
import android.app.ActivityOptions
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import androidx.core.content.ContextCompat
import androidx.paging.ExperimentalPagingApi
import com.pramod.dailyword.R
import com.pramod.dailyword.framework.ui.aboutapp.AboutAppActivity
import com.pramod.dailyword.framework.ui.bookmarks.FavoriteWordsActivity
import com.pramod.dailyword.framework.ui.changelogs.ChangelogActivity
import com.pramod.dailyword.framework.ui.donate.DonateActivity
import com.pramod.dailyword.framework.ui.home.HomeActivity
import com.pramod.dailyword.framework.ui.recap.RecapWordsActivity
import com.pramod.dailyword.framework.ui.settings.AppSettingActivity
import com.pramod.dailyword.framework.ui.worddetails.WordDetailedActivity
import com.pramod.dailyword.framework.ui.words.WordListActivity
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
@ExperimentalPagingApi
fun Activity.openHomePage(withFadeAnimation: Boolean = false, finish: Boolean = false) {
    val intent = Intent(this, HomeActivity::class.java)
    startActivity(intent)
    if (withFadeAnimation) {
        overridePendingTransition(
            android.R.anim.fade_in,
            android.R.anim.fade_out
        )
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


fun Activity.openChangelogPage(showContinueButton: Boolean = false) {
    val intent = Intent(this, ChangelogActivity::class.java)
    overridePendingTransition(
        android.R.anim.fade_in,
        android.R.anim.fade_out
    )
    intent.putExtra(ChangelogActivity.EXTRA_SHOW_CONTINUE_BUTTON, showContinueButton)
    startActivity(intent)
}

fun Activity.openDonatePage() {
    val intent = Intent(this, DonateActivity::class.java)
    startActivity(intent)
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
