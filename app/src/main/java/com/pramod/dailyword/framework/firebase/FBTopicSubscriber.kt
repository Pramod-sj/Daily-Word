package com.pramod.dailyword.framework.firebase

import android.content.Context
import android.util.Log
import com.google.firebase.messaging.FirebaseMessaging
import com.pramod.dailyword.business.data.network.abstraction.IPInfoNetworkDataSource
import com.pramod.dailyword.framework.helper.CountryCodeFinder
import com.pramod.dailyword.framework.prefmanagers.PrefManager
import com.pramod.dailyword.framework.util.LookupEnum
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FBTopicSubscriber @Inject constructor(
    @ApplicationContext private val context: Context,
    private val prefManager: PrefManager,
    private val countryCodeFinder: CountryCodeFinder,
    private val ipInfoNetworkDataSource: IPInfoNetworkDataSource
) {


    enum class OperationStatus {
        SUCCESS,
        FAILED
    }


    fun subscribeToDailyWordNotification() {
        subscribeTopic(TOPIC_DAILY_WORD_NOTIFICATION)
    }

    fun subscribeToCountry(coroutineScope: CoroutineScope) {

        coroutineScope.launch(Dispatchers.IO) {

            try {

                //two letters country code
                val countryCode: String? = countryCodeFinder.getCountryCode()


                val supportedCountryCode =
                    (if (countryCode != null && isCountryCodeSupported(countryCode)) {
                        //if country code is supported then return country code
                        countryCode
                    } else {
                        //else return OTHERS
                        SupportedFBTopicCounties.OTHERS.name
                    }).toUpperCase(Locale.US)

                //unsubscribe to all other countries if subscribed

                for (countryIsoEnum in LookupEnum.getAllEnumExcept(
                    SupportedFBTopicCounties::class.java,
                    supportedCountryCode
                )) {
                    unsubscribeTopic(countryIsoEnum.name)
                    Log.i(TAG, "unsubscribeToCountry: ${countryIsoEnum.name}")
                }

                //if country code is supported then
                Log.i(TAG, "subscribeToCountry: $supportedCountryCode")
                subscribeTopic(supportedCountryCode)

                prefManager.setCountryCode(supportedCountryCode)

            } catch (e: Exception) {
                Log.i(TAG, "subscribeToCountry: ${e.message}")
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
                        "Successfully subscribed to $topic",
                        OperationStatus.SUCCESS
                    )
                } else {
                    Log.i(TAG, "subscribeTopic: $topic Failed ${it.exception.toString()}")
                    listener?.invoke(
                        "Failed to subscribe from $topic. Reason:${it.exception?.message}",
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
                        "Successfully unsubscribed from $topic",
                        OperationStatus.SUCCESS
                    )
                } else {
                    Log.i(
                        TAG,
                        "unsubscribeTopic: $topic : Failed ${it.exception.toString()}"
                    )
                    listener?.invoke(
                        "Failed to unsubscribe from $topic. Reason:${it.exception?.message}",
                        OperationStatus.FAILED
                    )
                }
            }
    }


    companion object {
        private val TAG = FBTopicSubscriber::class.simpleName
        private const val TOPIC_DAILY_WORD_NOTIFICATION = "daily_word_notification"
        private const val TOPIC_COUNTRY_CODE = "country_code"
        private const val TOPIC_TEST_DEVICE = "test_device"

    }
}