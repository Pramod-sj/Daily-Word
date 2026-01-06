package com.pramod.dailyword.framework.helper.ads.rewards

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.pramod.dailyword.R
import com.pramod.dailyword.framework.firebase.FBRemoteConfig
import com.pramod.dailyword.framework.helper.ads.AdController
import com.pramod.dailyword.framework.ui.common.BaseActivity
import com.pramod.dailyword.framework.ui.common.BaseViewModel
import com.pramod.dailyword.framework.ui.common.CommonNavigationAction
import com.pramod.dailyword.framework.ui.common.Event
import com.pramod.dailyword.framework.ui.common.Message
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


@AndroidEntryPoint
class RewardAdDialogFragment : DialogFragment() {

    companion object {

        const val TAG = "RewardAdDialogFragment"

        const val CLOSE_DIALOG_IN_LOAD_FAILURE_SEC = 5

    }

    @Inject
    lateinit var fbRemoteConfig: FBRemoteConfig

    private val coroutineScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    private val buttonState = MutableStateFlow<RewardButtonState>(RewardButtonState.Loading)

    val adController: AdController by lazy {
        (requireActivity() as BaseActivity<*, *>).adController
    }

    private val viewModel: BaseViewModel by lazy {
        (requireActivity() as BaseActivity<*, *>).viewModel
    }

    private var invokeWatchRewardAd: Boolean = false

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val context = requireContext()
        val composeView = ComposeView(context).apply {
            setViewCompositionStrategy(
                ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed
            )
            setContent {
                val isRewardAdReady by buttonState.collectAsState()
                CompositionLocalProvider(FBRemoteConfigCompositionLocal provides fbRemoteConfig) {
                    RewardAdDisableAdsContent(
                        buttonState = isRewardAdReady,
                        onWatchAdClick = {
                            invokeWatchRewardAd = true
                            viewModel.setEvent(Event.Navigate(CommonNavigationAction.ShowRewardedAd))
                            dismissAllowingStateLoss()
                        },
                        onDismissClick = {
                            dismissAllowingStateLoss()
                        }
                    )
                }
            }
        }
        return MaterialAlertDialogBuilder(context)
            .setBackground(
                context.resources.getDrawable(
                    R.drawable.background_corner_24dp,
                    null
                )
            )
            .setView(composeView)
            .create()
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setDimAmount(0.9f)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        buttonState.value = RewardButtonState.Loading
        adController.loadRewardedAd(
            onLoaded = {
                buttonState.value = RewardButtonState.AdReady
            },
            onFailed = {
                coroutineScope.launch {

                    (CLOSE_DIALOG_IN_LOAD_FAILURE_SEC downTo 1).forEach { sec ->
                        buttonState.value =
                            RewardButtonState.AdFailedToLoad(sec)
                        delay(1000L)
                    }

                    dismissAllowingStateLoss()
                }
            }
        )
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        if (!invokeWatchRewardAd) {
            viewModel.setMessage(
                Message.SnackBarMessage(
                    message = "Ads help keep this project alive, Thanks for understanding.",
                    duration = Snackbar.LENGTH_LONG
                )
            )
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        coroutineScope.coroutineContext.cancelChildren()
    }
}

val FBRemoteConfigCompositionLocal = staticCompositionLocalOf<FBRemoteConfig?> { null }