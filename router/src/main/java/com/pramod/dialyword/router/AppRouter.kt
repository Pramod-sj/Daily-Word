package com.pramod.dialyword.router

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.net.toUri
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Centralized navigation router for the application.
 *
 * This singleton class serves as the primary navigation coordinator across all feature modules
 * and the app module. It delegates URI-based navigation requests to the appropriate feature
 * module's [FeatureRouter] implementation.
 *
 * ## Usage
 * Any feature module or the app module should use this router to perform navigation instead
 * of directly creating Intents. This ensures consistent navigation behavior and proper
 * decoupling between feature modules.
 *
 * ### From Activity/Fragment:
 * ```
 * @Inject lateinit var appRouter: AppRouter
 *
 * // Simple navigation
 * appRouter.navigateTo(context, "myapp://profile/123")
 *
 * // Navigation with extras
 * val extras = Bundle().apply {
 *     putString("source", "home_screen")
 * }
 * appRouter.navigateTo(context, "myapp://settings", extras)
 * ```
 *
 * ### From ViewModel (requires Context):
 * ```
 * class HomeViewModel @Inject constructor(
 *     private val appRouter: AppRouter
 * ) : ViewModel() {
 *     fun navigateToProfile(context: Context, userId: String) {
 *         appRouter.navigateTo(context, "myapp://profile/$userId")
 *     }
 * }
 * ```
 *
 * ## Architecture Benefits
 * - **Decoupling**: Feature modules don't need to know about each other's implementation details
 * - **Testability**: Navigation logic can be easily mocked and tested
 * - **Extensibility**: New features can be added by simply providing a new [FeatureRouter] implementation
 * - **Single Responsibility**: Each feature module handles its own routing logic
 *
 * @property featureRouters Set of all [FeatureRouter] implementations provided by feature modules
 *                          via Dependency Injection. The set is automatically populated by Dagger/Hilt.
 */
@Singleton
class AppRouter @Inject constructor(
    private val featureRouters: Set<@JvmSuppressWildcards FeatureRouter>
) {
    fun navigateTo(context: Context, routeUriString: String, extras: Bundle? = null) {
        try {
            val uri = routeUriString.toUri()

            // Iterate through the injected Set to find a router that can handle this URI
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