package com.pramod.dailyword.framework.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.widget.Toast
import com.pramod.dailyword.R

class NetworkUtils {
    companion object {
        val TAG = NetworkUtils::class.java.simpleName


        fun isNetworkActive(context: Context): Boolean {
            val connectivityManager =
                context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val capabilities =
                    connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
                if (capabilities != null) {
                    when {
                        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> {
                            return true
                        }

                        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> {
                            return true
                        }

                        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> {
                            return true
                        }
                    }
                } else {
                    return false
                }
            } else {
                val activeNetworkInfo = connectivityManager.activeNetworkInfo
                return activeNetworkInfo != null && activeNetworkInfo.isConnected
            }
            return false
        }


        fun isVPNActive(context: Context): Boolean {
            val connectivityManager =
                context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val capabilities =
                    connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
                if (capabilities != null) {
                    return when {
                        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_VPN) -> true
                        else -> false
                    }
                }
                return false
            } else {
                val networks = connectivityManager.allNetworkInfo
                for (n in networks) {
                    if (n.isConnectedOrConnecting && n.type == ConnectivityManager.TYPE_VPN) {
                        return true
                    }
                }
                return false
            }

        }
    }
}

fun Context.safeNetworkCall(callback: () -> Unit) {
    if (!NetworkUtils.isNetworkActive(this)) {
        Toast.makeText(
            this,
            resources.getString(R.string.no_internet_connection),
            Toast.LENGTH_SHORT
        ).show()
        return
    }
    callback()
}