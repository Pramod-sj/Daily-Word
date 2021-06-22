package com.pramod.dailyword.framework.ui.donate

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.view.updatePadding
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.android.billingclient.api.SkuDetails
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.pramod.dailyword.R
import com.pramod.dailyword.databinding.DialogDonateBinding
import com.pramod.dailyword.framework.firebase.FBRemoteConfig
import com.pramod.dailyword.framework.helper.billing.BillingHelper
import com.pramod.dailyword.framework.helper.billing.BillingListener
import com.pramod.dailyword.framework.ui.common.ExpandingBottomSheetDialogFragment
import com.pramod.dailyword.framework.ui.common.Message
import com.pramod.dailyword.framework.ui.common.exts.doOnApplyWindowInsets
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class DonateBottomDialogFragment :
    ExpandingBottomSheetDialogFragment<DialogDonateBinding>(R.layout.dialog_donate) {

    @Inject
    lateinit var fbRemoteConfig: FBRemoteConfig

    private val viewModel: DonateViewModel by viewModels()

    override fun getBottomSheetBehaviorView(): View {
        return binding.cardBottomSheet
    }

    private val donateItemAdapter: DonateItemAdapter by lazy {
        DonateItemAdapter { i: Int, donateItem: DonateItem ->
            lifecycleScope.launch {
                /* if (billingHelper.isPurchased(donateItem.itemProductId)) {
                     showSnackBarMessage(Message.SnackBarMessage("You have already donated this item, Thank you so much :)"))
                 } else {*/
                if (billingHelper.isInAppPurchaseSupported()) {
                    billingHelper.buy(requireActivity(), donateItem.itemProductId)
                    //billingProcessor.purchase(requireActivity(), donateItem.itemProductId)
                } else {
                    viewModel.setMessage(
                        Message.SnackBarMessage(
                            "In-app purchase service not available, you need to update google play service",
                            duration = Snackbar.LENGTH_LONG
                        )
                    )
                }
                /*}*/
            }
        }
    }

    private val billingHelper: BillingHelper by lazy {
        BillingHelper(
            requireContext(),
            viewLifecycleOwner,
            viewModel.donateItemList.value?.map { it.itemProductId } ?: listOf()
        )
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        billingHelper.setBillingListener(object : BillingListener {
            override fun onBillingInitialized() {
                Log.i(TAG, "onBillingInitialized: ")
            }

            override fun onBillingError(message: String) {
                Log.i(TAG, "onBillingError: $message")
                viewModel.setMessage(Message.SnackBarMessage(message))
            }

            override fun onSkuDetailsAvailable(skuDetailsList: List<SkuDetails>) {
                Log.i(TAG, "onSkuDetailsAvailable: ${Gson().toJson(skuDetailsList)}")
                viewModel.updateDonateItemPrice(skuDetailsList)
            }

            override fun onPurchased(sku: String) {
                Log.i(TAG, "onPurchased: $sku")
                viewModel.updateDonateItemStatus(sku, DonateItemState.PURCHASED)
            }

            override fun onPurchasedRestored(sku: String) {
                Log.i(TAG, "onPurchasedRestored: $sku")
                viewModel.updateDonateItemStatus(sku, DonateItemState.PURCHASED)
            }

            override fun onPurchasePending(sku: String) {
                Log.i(TAG, "onPurchasePending: $sku")
                viewModel.updateDonateItemStatus(sku, DonateItemState.PURCHASE_IN_PROCESS)
            }


        })
        applyBottomInsetToScrollView()
        loadLottieAnimationFileFromUrl()
        binding.donateRecyclerView.adapter = donateItemAdapter
        viewModel.donateItemList.observe(viewLifecycleOwner) {
            donateItemAdapter.submitList(it)
        }
        observeMessage()
    }

    private fun loadLottieAnimationFileFromUrl() {
        binding.lottieBoyWorking?.setAnimationFromUrl(
            fbRemoteConfig.getDonatePageLottieFileUrl()
        )
    }

    private fun applyBottomInsetToScrollView() {
        binding.nestedScrollView.doOnApplyWindowInsets { view, windowInsets, initialPadding, initialMargin ->
            binding.nestedScrollView.updatePadding(bottom = windowInsets.systemWindowInsetBottom)
        }
    }

    override fun onStateChanged(bottomSheet: View, newState: Int) {
        super.onStateChanged(bottomSheet, newState)
        //make bottom sheet dialog draggable when scroll view is not scroll
        bottomSheetBehavior.isDraggable =
            !binding.nestedScrollView.canScrollVertically(-1)
    }

    private fun observeMessage() {
        viewModel.message.observe(viewLifecycleOwner) { message ->
            message?.let {
                if (it is Message.SnackBarMessage) {
                    showSnackBarMessage(it)
                }
            }
        }
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