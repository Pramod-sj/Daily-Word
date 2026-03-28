package com.pramod.dialyword.router

import android.content.Context
import android.content.Intent
import android.net.Uri

/**
 * Navigation router interface for feature modules.
 *
 * Each feature module should implement this interface to provide navigation capabilities
 * and handle deep links or URI-based navigation within the application's modular architecture.
 *
 * This interface follows the Dependency Inversion Principle by allowing the navigation
 * layer to depend on abstractions rather than concrete implementations from feature modules.
 *
 * Example implementation:
 * ```
 * class ProfileFeatureRouter : FeatureRouter {
 *     override fun handles(uri: Uri): Boolean {
 *         return uri.path?.startsWith("/profile") == true
 *     }
 *
 *     override fun getNavigationIntent(context: Context, uri: Uri): Intent? {
 *         return if (handles(uri)) {
 *             Intent(context, ProfileActivity::class.java).apply {
 *                 data = uri
 *             }
 *         } else null
 *     }
 * }
 * ```
 */
interface FeatureRouter {

    /**
     * Determines whether this feature module can handle the given URI.
     *
     * @param uri The URI to be evaluated for handling capability.
     * @return `true` if this feature module can handle the URI, `false` otherwise.
     */
    fun handles(uri: Uri): Boolean

    /**
     * Creates and returns a navigation [Intent] for the given URI if this feature handles it.
     *
     * This method should only return a valid Intent if [handles] returns `true` for the same URI.
     * The returned Intent should be configured with all necessary data and extras required
     * to properly navigate to the destination within the feature module.
     *
     * @param context The [Context] used to create the Intent.
     * @param uri The URI containing navigation information and parameters.
     * @return An [Intent] configured for navigation, or `null` if the URI cannot be handled
     *         by this feature module.
     */
    fun getNavigationIntent(context: Context, uri: Uri): Intent?
}