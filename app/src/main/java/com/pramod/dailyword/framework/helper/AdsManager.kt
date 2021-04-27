package com.pramod.dailyword.framework.helper

import android.content.Context
import android.content.res.ColorStateList
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import androidx.core.view.isVisible
import androidx.databinding.BindingAdapter
import androidx.databinding.DataBindingUtil
import com.facebook.ads.*
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.pramod.dailyword.R
import com.pramod.dailyword.databinding.BannerNativeAdBinding
import com.pramod.dailyword.databinding.BannerNativeAdVerticalBinding
import com.pramod.dailyword.databinding.DialogNativeAdBinding
import com.pramod.dailyword.framework.ui.common.exts.getContextCompatColor
import com.pramod.dailyword.framework.util.CommonUtils
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AdsManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val sharedPreferences =
        context.getSharedPreferences(
            PREFERENCES_NAME,
            Context.MODE_PRIVATE
        )
    private val editor = sharedPreferences.edit()

    fun incrementAdActivityCount() {
        val count = getAdActivityCount() + 1
        editor.putInt(KEY_ADS_ACTIVITY_OPENED_FREQ, count).commit()
    }

    fun getAdActivityCount(): Int = sharedPreferences.getInt(KEY_ADS_ACTIVITY_OPENED_FREQ, 1)


    companion object {
        private const val PREFERENCES_NAME = "ads_freq_preferences"

        //so for every 5th time show ad dialog
        private const val KEY_ADS_ACTIVITY_OPENED_FREQ = "activity_opened_freq"

        const val NATIVE_AD_ID = "2986832738063599_3129116340501904"
        const val NATIVE_BANNER_ID_1 = "2986832738063599_3127533043993567"
        const val NATIVE_BANNER_ID_2 = "2986832738063599_3129110357169169"
        const val NATIVE_BANNER_ID_3 = "2986832738063599_3129112467168958"
        const val NATIVE_BANNER_ID_4 = "2986832738063599_3134035463343325"
        const val NATIVE_INTERSTITIAL_AD = "2986832738063599_2986834191396787"
        const val TAG = "AdsManager"

        @JvmStatic
        fun newInstance(context: Context): AdsManager {
            return AdsManager(context)
        }


    }


    private var interstitialAdView: InterstitialAd? = null
    private var isInterstitialAdShown = false

    fun showInterstitialAd(): Boolean {
        interstitialAdView = InterstitialAd(context, null).also {
            it.loadAd(it.buildLoadAdConfig().withAdListener(object : AbstractAdListener() {
                override fun onAdLoaded(ad: Ad?) {
                    super.onAdLoaded(ad)
                    Log.i(TAG, "Interstitial Ad Load")
                    if (it.isAdLoaded && !isInterstitialAdShown) {
                        it.show()
                        isInterstitialAdShown = true
                    }
                }

                override fun onInterstitialDisplayed(ad: Ad?) {
                    super.onInterstitialDisplayed(ad)
                    Log.i(TAG, "Interstitial Ad displayed")
                }

                override fun onInterstitialDismissed(ad: Ad?) {
                    super.onInterstitialDismissed(ad)
                    Log.i(TAG, "Interstitial Ad dismissed")
                    isInterstitialAdShown = false
                }

                override fun onError(ad: Ad?, error: AdError?) {
                    super.onError(ad, error)
                    Log.i(TAG, "Interstitial Ad Error: ${error?.errorMessage}")
                }
            }).build())
        }
        return true
    }

    fun showNativeBannerAd(
        adId: String,
        nativeAdLayout: NativeAdLayout,
        colorAccent: Int,
        verticalBannerAd: Boolean,
        adLoadedCallack: ((error: String?) -> Unit)? = null
    ): Boolean {

        NativeBannerAd(context, adId).also {
            it.loadAd(it.buildLoadAdConfig().withAdListener(object : NativeAdListener {
                override fun onAdClicked(p0: Ad?) {

                    Log.i(TAG, "Native ad clicked")
                }

                override fun onMediaDownloaded(p0: Ad?) {
                    Log.i(TAG, "Native ad media downlaoded")

                }

                override fun onError(p0: Ad?, p1: AdError?) {
                    Log.i(TAG, "Native ad Error: ${p1?.errorMessage}")
                    adLoadedCallack?.invoke(p1?.errorMessage ?: "Error while loading adding")
                }

                override fun onAdLoaded(p0: Ad?) {
                    Log.i(TAG, "Native ad loaded")
                    if (p0 == null) {
                        Log.i(TAG, "Native ad loaded is null")
                        return
                    }
                    adLoadedCallack?.invoke(null)
                    if (verticalBannerAd) {
                        inflateBannerNativeViewVertical(nativeAdLayout, it, colorAccent)
                    } else {
                        inflateBannerNativeView(nativeAdLayout, it, colorAccent)
                    }

                }

                override fun onLoggingImpression(p0: Ad?) {
                    Log.i(TAG, "Native ad logging impression")

                }

            }).build())
        }

        return true;
    }

    private fun inflateBannerNativeView(
        nativeAdLayout: NativeAdLayout,
        nativeBannerAd: NativeBannerAd,
        colorAccent: Int
    ) {

        //remove any previous view added on nativeAdLayout
        nativeAdLayout.removeAllViews()

        nativeBannerAd.unregisterView()
        val binding: BannerNativeAdBinding =
            DataBindingUtil.inflate(
                LayoutInflater.from(context),
                R.layout.banner_native_ad,
                nativeAdLayout,
                false
            )
        nativeAdLayout.addView(binding.root)

        //bind adchoice icon
        val adOptionsView =
            AdOptionsView(nativeAdLayout.context, nativeBannerAd, nativeAdLayout)
        binding.adChoiceContainer.removeAllViews()
        binding.adChoiceContainer.addView(adOptionsView, 0)

        //bind other ad info
        binding.nativeAdTitle.text = nativeBannerAd.advertiserName
        binding.nativeAdSocialContext.text = nativeBannerAd.adSocialContext
        binding.nativeAdSponseredLabel.text = nativeBannerAd.sponsoredTranslation
        binding.nativeAdButton.text = nativeBannerAd.adCallToAction
        binding.nativeAdButton.isVisible = nativeBannerAd.hasCallToAction()

        //set accent
        binding.nativeAdButton.backgroundTintList = ColorStateList.valueOf(colorAccent)

        //registering click callbacks

        val clickableViews = ArrayList<View>()
        clickableViews.add(binding.nativeAdTitle)
        clickableViews.add(binding.nativeAdButton)
        nativeBannerAd.registerViewForInteraction(
            binding.root,
            binding.nativeAdIcon,
            clickableViews
        )
    }

    private fun inflateBannerNativeViewVertical(
        nativeAdLayout: NativeAdLayout,
        nativeBannerAd: NativeBannerAd,
        colorAccent: Int
    ) {

        //remove any previous view added on nativeAdLayout
        nativeAdLayout.removeAllViews()

        nativeBannerAd.unregisterView()
        val binding: BannerNativeAdVerticalBinding =
            DataBindingUtil.inflate(
                LayoutInflater.from(context),
                R.layout.banner_native_ad_vertical,
                nativeAdLayout,
                false
            )
        nativeAdLayout.addView(binding.root)

        //bind adchoice icon
        val adOptionsView: AdOptionsView =
            AdOptionsView(nativeAdLayout.context, nativeBannerAd, nativeAdLayout)
        binding.adChoiceContainer.removeAllViews()
        binding.adChoiceContainer.addView(adOptionsView, 0)

        //bind other ad info
        binding.nativeAdTitle.text = nativeBannerAd.advertiserName
        binding.nativeAdSocialContext.text = nativeBannerAd.adSocialContext
        binding.nativeAdSponseredLabel.text = nativeBannerAd.sponsoredTranslation
        binding.nativeAdButton.text = nativeBannerAd.adCallToAction
        binding.nativeAdButton.isVisible = nativeBannerAd.hasCallToAction()

        //set accent
        binding.nativeAdButton.backgroundTintList = ColorStateList.valueOf(colorAccent)

        //registering click callbacks

        val clickableViews = ArrayList<View>()
        clickableViews.add(binding.nativeAdTitle)
        clickableViews.add(binding.nativeAdButton)
        nativeBannerAd.registerViewForInteraction(
            binding.root,
            binding.nativeAdIcon,
            clickableViews
        )
    }


    fun showNativeAdOnDialog(
        binding: DialogNativeAdBinding,
        onAdCompletelyLoadedCallback: (() -> Unit)? = null,
        onAdFailureCallback: ((String) -> Unit)? = null
    ): Boolean {
        NativeAd(context, NATIVE_AD_ID).also {
            it.loadAd(
                it.buildLoadAdConfig().withAdListener(object : NativeAdListener {
                    override fun onAdClicked(p0: Ad?) {
                        Log.i(TAG, "Native Ad clicked")
                    }

                    override fun onMediaDownloaded(p0: Ad?) {
                        Log.i(TAG, "Native Ad media downloaded")
                        if (p0 == null || p0 != it) {
                            Log.i(TAG, "Native Ad media downloaded is null")
                            onAdFailureCallback?.invoke("Native Ad media downloaded is null")
                            return
                        }
                        inflateNativeAdDialogView(binding, it)
                        onAdCompletelyLoadedCallback?.invoke()
                    }

                    override fun onError(p0: Ad?, p1: AdError?) {
                        Log.i(TAG, "Native Ad error ${p1.toString()}")
                        onAdFailureCallback?.invoke(p1.toString())
                    }

                    override fun onAdLoaded(p0: Ad?) {
                        Log.i(TAG, "Native Ad loaded")
                        if (p0 == null || p0 != it) {
                            Log.i(TAG, "Native Ad media loaded is null")
                            onAdFailureCallback?.invoke("Native Ad media loaded is null")
                            return
                        }
                        it.downloadMedia()
                    }

                    override fun onLoggingImpression(p0: Ad?) {
                        Log.i(TAG, "Native Ad logging impression")
                    }

                })
                    .withMediaCacheFlag(NativeAdBase.MediaCacheFlag.ALL).build()
            )
        }
        return true;
    }

    private fun inflateNativeAdDialogView(
        binding: DialogNativeAdBinding,
        nativeAd: NativeAd
    ) {
        nativeAd.unregisterView()

        //bind adchoice icon
        val adOptionsView: AdOptionsView =
            AdOptionsView(binding.root.context, nativeAd, binding.dialogNativeAdLayout)
        binding.adChoiceContainer.removeAllViews()
        binding.adChoiceContainer.addView(adOptionsView, 0)

        //bind other ad info
        binding.nativeAdTitle.text = nativeAd.advertiserName
        binding.nativeAdSocialContext.text = nativeAd.adSocialContext
        binding.nativeAdSponseredLabel.text = nativeAd.sponsoredTranslation
        binding.nativeAdButton.text = nativeAd.adCallToAction
        binding.nativeAdButton.isVisible = nativeAd.hasCallToAction()
        binding.nativeAdBody.text = nativeAd.adBodyText
        //registering click callbacks


        val clickableViews = ArrayList<View>()
        clickableViews.add(binding.nativeAdTitle)
        clickableViews.add(binding.nativeAdButton)
        nativeAd.registerViewForInteraction(
            binding.root,
            binding.nativeAdMedia,
            binding.nativeAdIcon,
            clickableViews
        )
    }


    fun incrementCountAndShowNativeAdDialog(
        context: Context,
        closeClickCallback: (() -> Unit)? = null
    ) {
        incrementAdActivityCount()
        showNativeAdDialog(context, closeClickCallback)
    }

    fun showNativeAdDialog(
        context: Context,
        closeClickCallback: (() -> Unit)? = null
    ) {
        if (getAdActivityCount() % 8 != 0) {
            Log.i(
                TAG,
                "Show Ad dialog condition doesn't matches ${getAdActivityCount()}"
            )
            return
        }

        val binding: DialogNativeAdBinding = DataBindingUtil.inflate(
            LayoutInflater.from(context),
            R.layout.dialog_native_ad,
            null,
            false
        )
        val bottomSheetDialog = BottomSheetDialog(context, R.style.AppTheme_BottomSheetDialog)
        bottomSheetDialog.setContentView(binding.root)

        /*val builder = MaterialAlertDialogBuilder(context)
            .setView(binding.root)
            .setCancelable(false)
        val alertDialog = builder.create()*/

        binding.nativeAdCloseButton.setOnClickListener {
            bottomSheetDialog.dismiss()
            closeClickCallback?.invoke()
        }
        val shown = showNativeAdOnDialog(binding, {
            binding.nativeAdLoading.isVisible = false
            binding.nativeAdLinearLayoutWrapper.isVisible = true
        }, {
            bottomSheetDialog.dismiss()
        })
        if (shown) {
            bottomSheetDialog.show()
        }
    }


    fun destroyAdsIfActive() {
        interstitialAdView?.destroy()
    }
}

class AdBindingAdaper {

    companion object {

        @JvmStatic
        @BindingAdapter(
            value = ["showNativeAd", "adId", "showAdWithSomeDelay", "app:adColorAccent", "app:ad_onLoadAd", "app:ad_onAdLoaded", "verticalAd"],
            requireAll = false
        )
        fun showNativeAd(
            nativeAdLayout: NativeAdLayout,
            showNativeAd: Boolean,
            adId: String,
            showAdWithSomeDelay: Long,
            colorAccent: Int?,
            onLoadAdCallback: OnLoadAdCallback?,
            onAdLoadedCallback: OnAdLoadedCallback?,
            verticalAd: Boolean = false
        ) {
            if (showNativeAd) {
                Log.i("TAG", "showNativeAd: " + nativeAdLayout.getTag(R.id.isLoadingAd))
                val isCalled =
                    nativeAdLayout.getTag(R.id.isLoadingAd) == "${nativeAdLayout.id}_showNativeAd_called"
                if (!isCalled) {
                    nativeAdLayout.setTag(
                        R.id.isLoadingAd,
                        "${nativeAdLayout.id}_showNativeAd_called"
                    )
                    onLoadAdCallback?.onLoadAd()
                    Handler().postDelayed({
                        val adsManager = AdsManager.newInstance(nativeAdLayout.context)
                        adsManager.showNativeBannerAd(
                            adId,
                            nativeAdLayout,
                            colorAccent
                                ?: nativeAdLayout.context.getContextCompatColor(R.color.colorPrimary),
                            verticalAd
                        ) { error ->
                            if (error == null) {
                                //only make it visible when ad loaded successfully
                                CommonUtils.showViewAlphaAnimation(nativeAdLayout)
                            } else {
                                nativeAdLayout.setTag(R.id.isLoadingAd, null)
                                nativeAdLayout.visibility = View.INVISIBLE
                            }
                            onAdLoadedCallback?.onAdLoaded()
                        }

                    }, showAdWithSomeDelay)
                }
            } else {
                nativeAdLayout.visibility = View.INVISIBLE
            }
        }

    }


    interface OnAdLoadedCallback {
        fun onAdLoaded()
    }

    interface OnLoadAdCallback {
        fun onLoadAd()
    }


}

fun Context.showNativeAdDialogWithDelay(adEnabled: Boolean, delayMillis: Long = 1000) {
    if (adEnabled) {
        Handler().postDelayed({
            AdsManager(this).incrementCountAndShowNativeAdDialog(this)
        }, delayMillis)
    }
}