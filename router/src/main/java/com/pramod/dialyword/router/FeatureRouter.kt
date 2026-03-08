package com.pramod.dialyword.router

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import javax.inject.Inject
import javax.inject.Singleton
import androidx.core.net.toUri

interface FeatureRouter {

    fun handles(uri: Uri): Boolean

    fun getNavigationIntent(context: Context, uri: Uri): Intent?

}