package com.pramod.dailyword.ui.about_app.donate

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProviders
import com.anjlab.android.iab.v3.BillingProcessor
import com.anjlab.android.iab.v3.TransactionDetails
import com.pramod.dailyword.R
import com.pramod.dailyword.BR
import com.pramod.dailyword.SnackbarMessage
import com.pramod.dailyword.databinding.ActivityDonateBinding
import com.pramod.dailyword.helper.WindowPrefManager
import com.pramod.dailyword.ui.BaseActivity

class DonateActivity : BaseActivity<ActivityDonateBinding, DonateViewModel>() {


    companion object {
        @JvmStatic
        fun openActivity(context: Context) {
            val intent = Intent(context, DonateActivity::class.java)
            context.startActivity(intent)
        }
    }

    override fun getLayoutId(): Int = R.layout.activity_donate

    override fun getViewModel(): DonateViewModel =
        ViewModelProviders.of(this).get(
            DonateViewModel::class.java
        )

    override fun getBindingVariable(): Int = BR.donateViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setUpToolbar()
        setUpBilling()
        setUpDonateItemRecyclerView()
    }


    private fun setUpToolbar() {
        setSupportActionBar(mBinding.toolbar)
        supportActionBar?.let {
            it.title = null
        }
        mBinding.toolbar.setNavigationIcon(R.drawable.ic_round_back_arrow)
        mBinding.toolbar.setNavigationOnClickListener { onBackPressed() }
    }


    private lateinit var billingProcessor: BillingProcessor
    private fun setUpBilling() {
        billingProcessor = BillingProcessor(this, null, object : BillingProcessor.IBillingHandler {
            override fun onBillingInitialized() {

            }

            override fun onPurchaseHistoryRestored() {

            }

            override fun onProductPurchased(productId: String, details: TransactionDetails?) {
                mViewModel.setMessage(SnackbarMessage.init("Thank you so much :)"))
            }

            override fun onBillingError(errorCode: Int, error: Throwable?) {
                mViewModel.setMessage(SnackbarMessage.init("Something went wrong! Please try again!"))
            }

        })
    }

    private fun setUpDonateItemRecyclerView() {
        val donateItemAdapter = DonateItemAdapter { i: Int, donateItem: DonateItem ->
            if (billingProcessor.isPurchased(donateItem.itemPurchaseId)) {
                mViewModel.setMessage(SnackbarMessage.init("You have already donated a ${donateItem.title}, Thank you so much :)"))
            } else {
                billingProcessor.purchase(this, donateItem.itemPurchaseId)
            }
        }
        mBinding.donateRecyclerView.adapter = donateItemAdapter
        donateItemAdapter.submitList(mViewModel.donateItemList)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (!billingProcessor.handleActivityResult(requestCode, resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    override fun onDestroy() {
        billingProcessor.release()
        super.onDestroy()
    }

    override fun arrangeViewsForEdgeToEdge(view: View, insets: WindowInsetsCompat) {
        mBinding.appBar.setPadding(
            0, insets.systemWindowInsetTop, 0, 0
        )

        val paddingTop =
            insets.systemWindowInsetTop + mBinding.donateRecyclerView.paddingTop
        val paddingBottom = insets.systemWindowInsetBottom

        mBinding.donateRecyclerView.setPadding(
            0,
            paddingTop,
            0,
            paddingBottom
        )
    }
}