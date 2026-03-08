package com.pramod.dialyword.router

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.net.toUri
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppRouter @Inject constructor(
    private val featureRouters: Set<@JvmSuppressWildcards FeatureRouter>
) {

    fun navigateTo(context: Context, routeUriString: String, extras: Bundle? = null) {
        try {
            val uri = routeUriString.toUri()

            // Iterate through the injected Set
            val router = featureRouters.find { it.handles(uri) }

            if (router != null) {
                val intent = router.getNavigationIntent(context, uri)
                    ?.apply { extras?.let { putExtras(extras) } }
                if (intent != null) {
                    context.startActivity(intent)
                } else {
                    Log.w("AppRouter", "No page exists for: $routeUriString")
                    Toast.makeText(context, "Feature coming soon!", Toast.LENGTH_SHORT).show()
                }
            } else {
                Log.w("AppRouter", "No feature router found for: $routeUriString")
                Toast.makeText(context, "Feature coming soon!", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Log.e("AppRouter", "Failed to parse URI: $routeUriString", e)
        }
    }

}