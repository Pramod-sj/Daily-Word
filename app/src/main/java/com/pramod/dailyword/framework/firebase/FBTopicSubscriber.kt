package com.pramod.dailyword.framework.firebase

import android.content.Context
import android.util.Log
import com.google.firebase.messaging.FirebaseMessaging
import com.pramod.dailyword.business.data.network.abstraction.IPInfoNetworkDataSource
import com.pramod.dailyword.business.domain.model.IPInfo
import com.pramod.dailyword.framework.util.CommonUtils
import com.pramod.dailyword.framework.util.LookupEnum
import com.pramod.dailyword.framework.util.NetworkUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*

class FBTopicSubscriber constructor(
    private var context: Context,
    private val ipInfoNetworkDataSource: IPInfoNetworkDataSource
) {


    enum class SupportedFBTopicCounties {
        IN, //India
        US, //United States
        GB, //United Kingdom
    }

    enum class OperationStatus {
        SUCCESS,
        FAILED
    }


    fun subscribeToDailyWordNotification() {
        subscribeTopic(TOPIC_DAILY_WORD_NOTIFICATION)
    }

    fun subscribeToCountry() {

        GlobalScope.launch(Dispatchers.IO) {
            //Log.i(TAG, "subscribeToCountry: publicIP: $publicIP")

            //two letters
            val countryCode: String? = if (NetworkUtils.isVPNActive(context)) {
                var publicIP: String? = null
                try {
                    publicIP = ipInfoNetworkDataSource.getPublicIp()
                } catch (e: Exception) {
                    Log.i(TAG, "subscribeToCountry: $e")
                }

                val isVPNActive = NetworkUtils.isVPNActive(context)
                Log.i(TAG, "subscribeToCountry: isVPNActive: $isVPNActive")

                if (publicIP != null && !isVPNActive) {
                    val ipInfo: IPInfo? = ipInfoNetworkDataSource.getIPDetails(publicIP)
                    if (ipInfo != null && "success" == ipInfo.status) {
                        ipInfo.countryCode
                    } else {
                        CommonUtils.getCountryCodeFromTelephoneManager(context)
                    }

                } else {

                    CommonUtils.getCountryCodeFromTelephoneManager(context)

                }
            } else {
                CommonUtils.getCountryCodeFromTelephoneManager(context)
            }


            countryCode?.let {
                var countryCodeUpperCase = it.toUpperCase(Locale.US)
                Log.i(TAG, "subscribeToCountry: $countryCodeUpperCase")
                if (!SupportedFBTopicCounties.values()
                        .contains(
                            LookupEnum.lookUp(
                                SupportedFBTopicCounties::class.java,
                                countryCodeUpperCase
                            )
                        )
                ) {
                    countryCodeUpperCase = SupportedFBTopicCounties.US.name
                }


                //unsubscribe to all other countries if subscribed

                for (countryIsoEnum in LookupEnum.getAllEnumExcept(
                    SupportedFBTopicCounties::class.java,
                    countryCodeUpperCase
                )) {
                    unsubscribeTopic(countryIsoEnum.name)
                    Log.i(TAG, "unsubscribeToCountry: ${countryIsoEnum.name}")
                }

                //if country code is supported then
                Log.i(TAG, "subscribeToCountry: $countryCodeUpperCase")
                subscribeTopic(countryCodeUpperCase)


            }
        }


    }


    fun subscribeToTestDevice(context: Context) {
        subscribeTopic(TOPIC_TEST_DEVICE)
    }


    fun subscribeTopic(
        topic: String,
        listener: ((String, OperationStatus) -> Unit)? = null
    ) {
        FirebaseMessaging.getInstance().subscribeToTopic(TOPIC_DAILY_WORD_NOTIFICATION)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    Log.i(TAG, "subscribeTopic: $topic :Success")
                    listener?.invoke(
                        "You'll now receive daily word notification every day",
                        OperationStatus.SUCCESS
                    )
                } else {
                    Log.i(TAG, "subscribeTopic: $topic Failed ${it.exception.toString()}")
                    listener?.invoke(
                        "There's some issue while registering you for notification service, Try again!",
                        OperationStatus.FAILED
                    )
                }
            }
    }

    fun unsubscribeTopic(
        topic: String,
        listener: ((String, OperationStatus) -> Unit)? = null
    ) {
        FirebaseMessaging.getInstance().unsubscribeFromTopic(topic)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    Log.i(TAG, "unsubscribeTopic: $topic : Success")
                    listener?.invoke(
                        "Your notification service is successfully disale",
                        OperationStatus.SUCCESS
                    )
                } else {
                    Log.i(
                        TAG,
                        "unsubscribeTopic: $topic : Failed ${it.exception.toString()}"
                    )
                    listener?.invoke(
                        "There's some issue while disaling notification service, Try again!",
                        OperationStatus.FAILED
                    )
                }
            }
    }


    companion object {
        private val TAG = FBTopicSubscriber::class.simpleName
        private val TOPIC_DAILY_WORD_NOTIFICATION = "daily_word_notification"
        private val TOPIC_COUNTRY_CODE = "country_code"
        private val TOPIC_TEST_DEVICE = "test_device"

        /*fun toggleReceivingDailyWordNotification(
            notificationPrefManager: NotificationPrefManager,
            listener: ((String, OperationStatus) -> Unit)? = null
        ) {
            if (notificationPrefManager.isNotificationEnabled()) {
                unsubscribeTopic(TOPIC_DAILY_WORD_NOTIFICATION) { s, operationStatus ->
                    listener?.invoke(s, operationStatus)
                }
            } else {
                subscribeTopic(TOPIC_DAILY_WORD_NOTIFICATION) { s, operationStatus ->
                    listener?.invoke(s, operationStatus)
                }
            }
        }*/

    }
}