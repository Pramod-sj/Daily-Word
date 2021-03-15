package com.pramod.dailyword.framework.ui.donate

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.databinding.DataBindingUtil
import com.anjlab.android.iab.v3.BillingProcessor
import com.anjlab.android.iab.v3.TransactionDetails
import com.pramod.dailyword.BR
import com.pramod.dailyword.R
import com.pramod.dailyword.databinding.ActivityDonateBinding
import com.pramod.dailyword.framework.ui.common.BaseActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DonateActivity : BaseActivity<ActivityDonateBinding, DonateViewModel>() {

    override val layoutId: Int = R.layout.activity_donate
    override val viewModel: DonateViewModel by viewModels()
    override val bindingVariable: Int = BR.donateViewModel


    companion object {
        val TAG = DonateActivity::class.java.simpleName

        @JvmStatic
        fun openActivity(context: Context) {
            val intent = Intent(context, DonateActivity::class.java)
            context.startActivity(intent)
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_donate)
        setUpToolbar()
        setUpBilling()
        setUpDonateItemRecyclerView()
    }


    private fun setUpToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.let {
            it.title = null
        }
        binding.toolbar.setNavigationIcon(R.drawable.ic_round_back_arrow)
        binding.toolbar.setNavigationOnClickListener { onBackPressed() }
    }


    private lateinit var billingProcessor: BillingProcessor
    private fun setUpBilling() {
        billingProcessor = BillingProcessor(this, null, object : BillingProcessor.IBillingHandler {
            override fun onBillingInitialized() {

            }

            override fun onPurchaseHistoryRestored() {

            }

            override fun onProductPurchased(productId: String, details: TransactionDetails?) {
                //mViewModel.setMessage(Message.SnackBarMessage("Thank you so much :)"))
            }

            override fun onBillingError(errorCode: Int, error: Throwable?) {
                //mViewModel.setMessage(Message.SnackBarMessage("Something went wrong! Please try again!"))
            }

        })
    }

    private fun setUpDonateItemRecyclerView() {
        val donateItemAdapter = DonateItemAdapter { i: Int, donateItem: DonateItem ->
            if (billingProcessor.isPurchased(donateItem.itemPurchaseId)) {
                //mViewModel.setMessage(Message.SnackBarMessage("You have already donated this item, Thank you so much :)"))
            } else {
                billingProcessor.purchase(this, donateItem.itemPurchaseId)
            }
        }
        binding.donateRecyclerView.adapter = donateItemAdapter
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

}