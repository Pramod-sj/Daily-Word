package com.pramod.dailyword.framework.ui.donate

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.view.updatePadding
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.viewModels
import com.anjlab.android.iab.v3.BillingProcessor
import com.anjlab.android.iab.v3.TransactionDetails
import com.google.android.material.snackbar.Snackbar
import com.pramod.dailyword.BuildConfig
import com.pramod.dailyword.R
import com.pramod.dailyword.databinding.ActivityDonateBinding
import com.pramod.dailyword.framework.firebase.FBRemoteConfig
import com.pramod.dailyword.framework.ui.common.BaseActivity
import com.pramod.dailyword.framework.ui.common.ExpandingBottomSheetDialogFragment
import com.pramod.dailyword.framework.ui.common.Message
import com.pramod.dailyword.framework.ui.common.exts.doOnApplyWindowInsets
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class DonateBottomDialogFragment :
    ExpandingBottomSheetDialogFragment<ActivityDonateBinding>(R.layout.activity_donate) {

    @Inject
    lateinit var fbRemoteConfig: FBRemoteConfig

    override fun getBottomSheetBehaviorView(): View {
        return binding.cardBottomSheet
    }

    override fun peekHeightFactor(): Float {
        return 0.8f
    }


    val viewModel: DonateViewModel by viewModels()

    private val billingProcessor: BillingProcessor by lazy {
        BillingProcessor(
            requireActivity(),
            BuildConfig.GOOGLE_LICENSE_KEY,
            object : BillingProcessor.IBillingHandler {
                override fun onBillingInitialized() {

                }

                override fun onPurchaseHistoryRestored() {

                }

                override fun onProductPurchased(productId: String, details: TransactionDetails?) {
                    viewModel.setMessage(Message.SnackBarMessage("Thank you so much :)"))
                }

                override fun onBillingError(errorCode: Int, error: Throwable?) {
                    viewModel.setMessage(Message.SnackBarMessage("Something went wrong! Please try again!"))
                }

            })
    }

    private val donateItemAdapter: DonateItemAdapter by lazy {
        DonateItemAdapter { i: Int, donateItem: DonateItem ->
            Log.i(TAG, ": " + i)
            if (billingProcessor.isPurchased(donateItem.itemPurchaseId)) {
                viewModel.setMessage(Message.SnackBarMessage("You have already donated this item, Thank you so much :)"))
            } else {
                billingProcessor.purchase(requireActivity(), donateItem.itemPurchaseId)
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        applyBottomInsetToScrollView()
        loadLottieAnimationFileFromUrl()
        binding.donateRecyclerView.adapter = donateItemAdapter
        donateItemAdapter.submitList(viewModel.donateItemList)
        binding.nestedScrollView.setOnScrollChangeListener(
            NestedScrollView.OnScrollChangeListener { v, scrollX, scrollY, oldScrollX, oldScrollY ->
                bottomSheetBehavior.isDraggable =
                    !binding.nestedScrollView.canScrollVertically(-1)
                Log.i(TAG, "onViewCreated: " + bottomSheetBehavior.isDraggable)
            })
        setMessageObserver()
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

    private fun setMessageObserver() {
        viewModel.message.observe(this) { message ->
            message?.let {
                when (it) {
                    is Message.SnackBarMessage -> {
                        handleSnackBarMessage(it)
                        Log.i(BaseActivity.TAG, "setSnackBarObserver: snackbar message")
                    }
                    is Message.ToastMessage -> {
                        Log.i(BaseActivity.TAG, "setSnackBarObserver: toast message")
                    }
                    is Message.DialogMessage -> {
                        Log.i(BaseActivity.TAG, "setSnackBarObserver: dialog message")
                    }
                }
            }

        }
    }

    private fun handleSnackBarMessage(it: Message.SnackBarMessage) {
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

                viewModel.setMessage(null)
                snackBar.removeCallback(this)
            }

            override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                super.onDismissed(transientBottomBar, event)
                viewModel.setMessage(null)
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