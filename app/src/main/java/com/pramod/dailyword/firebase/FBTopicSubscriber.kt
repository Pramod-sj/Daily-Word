package com.pramod.dailyword.firebase

import android.content.Context
import android.telephony.TelephonyManager
import android.util.Log
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import com.google.gson.Gson
import com.pramod.dailyword.db.model.IPInfo
import com.pramod.dailyword.helper.NotificationPrefManager
import com.pramod.dailyword.util.CommonUtils
import com.pramod.dailyword.util.LookupEnum
import com.pramod.dailyword.util.NetworkUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*

class FBTopicSubscriber {
    enum class SupportedFBTopicCounties {
        IN, //India
        US, //United States
        GB, //United Kingdom
    }

    enum class OperationStatus {
        SUCCESS,
        FAILED
    }

    companion object {
        private val TAG = FBTopicSubscriber::class.simpleName
        private val TOPIC_DAILY_WORD_NOTIFICATION = "daily_word_notification"
        private val TOPIC_COUNTRY_CODE = "country_code"

        fun toggleReceivingDailyWordNotification(
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
        }

        fun subscribeToDailyWordNotification() {
            subscribeTopic(TOPIC_DAILY_WORD_NOTIFICATION)
        }

        fun subscribeToCountry(context: Context) {

            GlobalScope.launch(Dispatchers.IO) {
                val ipService = NetworkUtils.getIPService()

                //Log.i(TAG, "subscribeToCountry: publicIP: $publicIP")

                //two letters
                val countryCode: String? = if (NetworkUtils.isVPNActive(context)) {

                    val publicIP = ipService.getPublicIp()

                    val isVPNActive = NetworkUtils.isVPNActive(context)
                    Log.i(TAG, "subscribeToCountry: isVPNActive: $isVPNActive")

                    if (publicIP != null && !isVPNActive) {
                        val ipInfo: IPInfo? = ipService.getIPDetails(publicIP)
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

        fun registerToCountryTopic() {

        }

        fun unregisterFromCountryTopic() {

        }
    }
}