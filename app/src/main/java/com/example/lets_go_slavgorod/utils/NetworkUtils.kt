package com.example.lets_go_slavgorod.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log

/**
 * Утилиты для работы с сетевым соединением
 */
object NetworkUtils {
    
    private const val TAG = "NetworkUtils"
    
    /**
     * Проверяет доступность интернет-соединения
     */
    fun isNetworkAvailable(context: Context): Boolean {
        return try {
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            connectivityManager?.let { cm ->
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    val network = cm.activeNetwork
                    val capabilities = cm.getNetworkCapabilities(network)
                    val isConnected = capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
                    Log.d(TAG, "Network available (API 23+): $isConnected")
                    isConnected
                } else {
                    @Suppress("DEPRECATION")
                    val isConnected = cm.activeNetworkInfo?.isConnected == true
                    Log.d(TAG, "Network available (API <23): $isConnected")
                    isConnected
                }
            } ?: false
        } catch (e: Exception) {
            Log.e(TAG, "Error checking network availability", e)
            false
        }
    }
    
    /**
     * Проверяет, является ли соединение WiFi
     */
    fun isWifiConnection(context: Context): Boolean {
        return try {
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            connectivityManager?.let { cm ->
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    val network = cm.activeNetwork
                    val capabilities = cm.getNetworkCapabilities(network)
                    val isWifi = capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true
                    Log.d(TAG, "Is WiFi connection: $isWifi")
                    isWifi
                } else {
                    @Suppress("DEPRECATION")
                    val isWifi = cm.activeNetworkInfo?.type == ConnectivityManager.TYPE_WIFI
                    Log.d(TAG, "Is WiFi connection (API <23): $isWifi")
                    isWifi
                }
            } ?: false
        } catch (e: Exception) {
            Log.e(TAG, "Error checking WiFi connection", e)
            false
        }
    }
    
    /**
     * Проверяет, является ли соединение мобильным
     */
    fun isMobileConnection(context: Context): Boolean {
        return try {
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            connectivityManager?.let { cm ->
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    val network = cm.activeNetwork
                    val capabilities = cm.getNetworkCapabilities(network)
                    val isMobile = capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) == true
                    Log.d(TAG, "Is mobile connection: $isMobile")
                    isMobile
                } else {
                    @Suppress("DEPRECATION")
                    val isMobile = cm.activeNetworkInfo?.type == ConnectivityManager.TYPE_MOBILE
                    Log.d(TAG, "Is mobile connection (API <23): $isMobile")
                    isMobile
                }
            } ?: false
        } catch (e: Exception) {
            Log.e(TAG, "Error checking mobile connection", e)
            false
        }
    }
    
    /**
     * Получает тип соединения в виде строки
     */
    fun getConnectionType(context: Context): String {
        return when {
            isWifiConnection(context) -> "WiFi"
            isMobileConnection(context) -> "Mobile"
            isNetworkAvailable(context) -> "Other"
            else -> "None"
        }
    }
}
