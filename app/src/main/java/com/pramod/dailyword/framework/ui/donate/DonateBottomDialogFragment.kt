package com.pramod.dailyword.framework.ui.donate

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.core.view.updatePadding
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.android.billingclient.api.ProductDetails
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.analytics.FirebaseAnalytics
import com.pramod.dailyword.R
import com.pramod.dailyword.databinding.DialogDonateBinding
import com.pramod.dailyword.framework.firebase.FBRemoteConfig
import com.pramod.dailyword.framework.helper.billing.BillingHelper
import com.pramod.dailyword.framework.helper.billing.PurchaseListener
import com.pramod.dailyword.framework.prefmanagers.PrefManager
import com.pramod.dailyword.framework.ui.common.ExpandingBottomSheetDialogFragment
import com.pramod.dailyword.framework.ui.common.Message
import com.pramod.dailyword.framework.ui.common.exts.doOnApplyWindowInsets
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class DonateBottomDialogFragment :
    ExpandingBottomSheetDialogFragment<DialogDonateBinding>(R.layout.dialog_donate),
    PurchaseListener {

    @Inject
    lateinit var fbRemoteConfig: FBRemoteConfig

    @Inject
    lateinit var prefManager: PrefManager

    private val viewModel: DonateViewModel by viewModels()

    override fun getBottomSheetBehaviorView(): View {
        return binding.cardBottomSheet
    }

    private val donateItemAdapter: DonateItemAdapter by lazy {
        DonateItemAdapter { i: Int, donateItem: DonateItem ->
            lifecycleScope.launch {
                billingHelper.buy(requireActivity(), donateItem.itemProductId)
            }
        }
    }

    private lateinit var billingHelper: BillingHelper


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.post {
            FirebaseAnalytics.getInstance(requireContext()).logEvent(
                FirebaseAnalytics.Event.SCREEN_VIEW,
                bundleOf(FirebaseAnalytics.Param.SCREEN_NAME to DonateBottomDialogFragment::class.simpleName)
            )
        }
        billingHelper = BillingHelper(
            requireContext(),
            viewModel.donateItemList.value?.map { it.itemProductId } ?: listOf()
        )
        billingHelper.addPurchaseListener(this)
        applyBottomInsetToScrollView()
        loadLottieAnimationFileFromUrl()
        binding.donateRecyclerView.adapter = donateItemAdapter
        viewModel.donateItemList.observe(viewLifecycleOwner) {
            donateItemAdapter.submitList(it)
        }
        observeMessage()
    }

    private fun loadLottieAnimationFileFromUrl() {
        /*binding.lottieBoyWorking?.setAnimationFromUrl(
            fbRemoteConfig.getDonatePageLottieFileUrl()
        )*/
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

    override fun onPurchased(sku: String) {
        viewModel.updateDonateItemStatus(sku, DonateItemState.PURCHASED)
        viewModel.setMessage(Message.SnackBarMessage(resources.getString(R.string.donate_purchase_success)))
        //setting has donated to true
        prefManager.setHasDonated(true)
    }

    override fun onPurchasedRestored(sku: String) {
        viewModel.updateDonateItemStatus(sku, DonateItemState.PURCHASED)
        //setting has donated to true
        prefManager.setHasDonated(true)
    }

    override fun onPurchasePending(sku: String) {
        viewModel.updateDonateItemStatus(sku, DonateItemState.PURCHASE_IN_PROCESS)
        viewModel.setMessage(Message.SnackBarMessage(resources.getString(R.string.donate_purchase_under_process)))
    }

    override fun onPurchaseError(message: String) {
        viewModel.setMessage(Message.SnackBarMessage(message))
    }

    override fun onBillingInitialized() {
        Timber.i("onBillingInitialized: ")
    }

    override fun onBillingError(message: String) {
        viewModel.setMessage(Message.SnackBarMessage(message))
    }

    override fun onBillingSkuDetailsAvailable(skuDetailsList: List<ProductDetails>) {
        viewModel.updateDonateItemPrice(skuDetailsList)
    }

    override fun onBillingPurchasesProcessed() {

    }

    override fun onDestroyView() {
        super.onDestroyView()
        billingHelper.close()
    }

    companion object {

        private val TAG = DonateBottomDialogFragment::class.java.simpleName

        fun show(supportFragmentManager: FragmentManager) {
            DonateBottomDialogFragment().show(supportFragmentManager, TAG)
        }
    }
}