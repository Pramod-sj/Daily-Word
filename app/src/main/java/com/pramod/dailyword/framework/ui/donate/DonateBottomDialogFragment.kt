package com.pramod.dailyword.framework.ui.donate

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.view.updatePadding
import androidx.fragment.app.FragmentManager
import com.anjlab.android.iab.v3.BillingProcessor
import com.anjlab.android.iab.v3.TransactionDetails
import com.google.android.material.snackbar.Snackbar
import com.pramod.dailyword.BuildConfig
import com.pramod.dailyword.R
import com.pramod.dailyword.databinding.DialogDonateBinding
import com.pramod.dailyword.framework.firebase.FBRemoteConfig
import com.pramod.dailyword.framework.helper.BillingHelper
import com.pramod.dailyword.framework.ui.common.ExpandingBottomSheetDialogFragment
import com.pramod.dailyword.framework.ui.common.Message
import com.pramod.dailyword.framework.ui.common.exts.doOnApplyWindowInsets
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class DonateBottomDialogFragment :
    ExpandingBottomSheetDialogFragment<DialogDonateBinding>(R.layout.dialog_donate) {

    @Inject
    lateinit var fbRemoteConfig: FBRemoteConfig

    override fun getBottomSheetBehaviorView(): View {
        return binding.cardBottomSheet
    }

    private val billingProcessor: BillingProcessor by lazy {
        BillingProcessor.newBillingProcessor(
            requireActivity(),
            BuildConfig.GOOGLE_LICENSE_KEY,
            BuildConfig.MERCHANT_ID,
            object : BillingProcessor.IBillingHandler {
                override fun onBillingInitialized() {

                }

                override fun onPurchaseHistoryRestored() {
                    Log.i(TAG, "onPurchaseHistoryRestored: ")
                }

                override fun onProductPurchased(productId: String, details: TransactionDetails?) {
                    Log.i(TAG, "onProductPurchased: ")
                    showSnackBarMessage(Message.SnackBarMessage("Thank you so much :)"))
                }

                override fun onBillingError(errorCode: Int, error: Throwable?) {
                    Log.i(TAG, "onBillingError: " + error.toString())
                    showSnackBarMessage(Message.SnackBarMessage("Something went wrong! Please try again!"))
                }

            })
    }

    private val donateItemAdapter: DonateItemAdapter by lazy {
        DonateItemAdapter { i: Int, donateItem: DonateItem ->
            if (billingProcessor.isPurchased(donateItem.itemProductId)) {
                showSnackBarMessage(Message.SnackBarMessage("You have already donated this item, Thank you so much :)"))
            } else {
                if (BillingProcessor.isIabServiceAvailable(context)) {
                    billingHelper.purchase(requireActivity(), donateItem.itemProductId)
                    //billingProcessor.purchase(requireActivity(), donateItem.itemProductId)
                } else {
                    showSnackBarMessage(
                        Message.SnackBarMessage(
                            "In-app purchase service not available, you need to update google play service",
                            duration = Snackbar.LENGTH_LONG
                        )
                    )
                }
            }
        }
    }

    private lateinit var billingHelper: BillingHelper


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initBillingHelper()
        applyBottomInsetToScrollView()
        loadLottieAnimationFileFromUrl()
        binding.donateRecyclerView.adapter = donateItemAdapter
        donateItemList.observe(this) {
            donateItemAdapter.submitList(it)
        }
    }

    private fun initBillingHelper() {

        billingHelper = BillingHelper.newInstance(
            requireContext(),
            viewLifecycleOwner,
            onBillingInitialized = {
                Log.i(TAG, "onBillingInitialized: ")
                donateItemList.value?.let { localDonateItem ->
                    val list = ArrayList(localDonateItem.map { it.itemProductId })
                    val purchaseList = billingProcessor.getPurchaseListingDetails(list)
                    purchaseList?.let {
                        val localDonateItemMutable = localDonateItem.toMutableList()
                        for (sku in purchaseList) {
                            localDonateItem.find { sku.productId == it.itemProductId }?.let {
                                localDonateItemMutable[localDonateItem.indexOf(it)] =
                                    DonateItem(
                                        it.itemProductId,
                                        it.drawableId,
                                        it.title,
                                        sku.priceText,
                                        it.color
                                    )
                            }
                        }
                        donateItemList.value = localDonateItemMutable
                    }
                }
            },
            onBillingError = {
                showSnackBarMessage(Message.SnackBarMessage(it))
            }
        )

    }

    private fun loadLottieAnimationFileFromUrl() {
        binding.lottieThankYou.setAnimationFromUrl(
            fbRemoteConfig.getThankYouLottieFileUrl()
        )
    }

    private fun applyBottomInsetToScrollView() {
        binding.nestedScrollView.doOnApplyWindowInsets { view, windowInsets, initialPadding, initialMargin ->
            binding.nestedScrollView.updatePadding(bottom = windowInsets.systemWindowInsetBottom)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (!billingProcessor.handleActivityResult(requestCode, resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    override fun onStateChanged(bottomSheet: View, newState: Int) {
        super.onStateChanged(bottomSheet, newState)
        //make bottom sheet dialog draggable when scroll view is not scroll
        bottomSheetBehavior.isDraggable =
            !binding.nestedScrollView.canScrollVertically(-1)
    }

    override fun onDestroy() {
        billingProcessor.release()
        super.onDestroy()
    }

    private fun showSnackBarMessage(it: Message.SnackBarMessage) {
        if (it.isShown) {
            return
        }
        val snackBar = Snackbar.make(
            binding.root,
            it.message,
            it.duration
        ).setAnimationMode(it.animation)
        it.action?.let { action ->
            snackBar.setAction(action.name) { v ->
                it.action.callback?.invoke()
            }
        }
        it.anchorId?.let { id ->
            snackBar.setAnchorView(id)
        }
        snackBar.addCallback(object : Snackbar.Callback() {
            override fun onShown(sb: Snackbar?) {
                super.onShown(sb)

                it.isShown = true

                snackBar.removeCallback(this)
            }

            override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                super.onDismissed(transientBottomBar, event)

                snackBar.removeCallback(this)
            }
        })
        snackBar.show()

    }

    companion object {

        val TAG = DonateBottomDialogFragment::class.java.simpleName

        fun show(supportFragmentManager: FragmentManager) {
            DonateBottomDialogFragment().show(supportFragmentManager, TAG)
        }
    }
}