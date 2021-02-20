package com.pramod.dailyword.framework.ui.donate

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.anjlab.android.iab.v3.BillingProcessor
import com.anjlab.android.iab.v3.TransactionDetails
import com.pramod.dailyword.R
import com.pramod.dailyword.databinding.ActivityDonateBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DonateActivity : AppCompatActivity() {
    lateinit var binding: ActivityDonateBinding
    val mViewModel: DonateViewModel by viewModels()


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