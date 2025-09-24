/**
 * Менеджер для проверки и загрузки обновлений приложения
 * 
 * Этот класс отвечает за:
 * - Проверку доступности интернет-соединения
 * - Получение информации о последней версии с GitHub API
 * - Сравнение версий приложения
 * - Загрузку обновлений через браузер
 */
@file:Suppress("UNCHECKED_CAST")

package com.example.lets_go_slavgorod.updates

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.core.net.toUri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import org.json.JSONObject
import java.net.HttpURLConnection
import kotlin.math.max
import java.net.URL

/**
 * Менеджер для проверки и загрузки обновлений приложения
 * 
 * Основные функции:
 * - Проверка доступности интернет-соединения
 * - Запрос к GitHub API для получения информации о последнем релизе
 * - Сравнение версий приложения
 * - Запуск загрузки обновления через браузер
 */

@Suppress("DEPRECATION")
class UpdateManager(private val context: Context) {
    
    companion object {
        private const val TAG = "UpdateManager"
        private const val GITHUB_API_URL = "https://api.github.com/repos/VseMirka200/lets_go_slavgorod/releases/latest"
        private const val TIMEOUT_MS = 10000L
    }
    
    /**
     * Результат проверки обновлений
     */
    data class UpdateResult(
        val success: Boolean,
        val update: UpdateInfo? = null,
        val error: String? = null
    )
    
    /**
     * Информация об обновлении
     */
    data class UpdateInfo(
        val versionName: String,
        val downloadUrl: String,
        val releaseNotes: String
    )
    
    /**
     * Проверяет доступность интернет-соединения
     */
    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        return connectivityManager?.let { cm ->
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                val network = cm.activeNetwork
                val capabilities = cm.getNetworkCapabilities(network)
                capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
            } else {
                @Suppress("DEPRECATION")
                cm.activeNetworkInfo?.isConnected == true
            }
        } ?: false
    }
    
    /**
     * Получает информацию о последнем релизе с GitHub API
     */
    private suspend fun fetchLatestRelease(): UpdateInfo? = withContext(Dispatchers.IO) {
        var connection: HttpURLConnection? = null
        try {
            val url = URL(GITHUB_API_URL)
            connection = url.openConnection() as HttpURLConnection
            
            connection.apply {
                requestMethod = "GET"
                connectTimeout = TIMEOUT_MS.toInt()
                readTimeout = TIMEOUT_MS.toInt()
                setRequestProperty("Accept", "application/vnd.github.v3+json")
                setRequestProperty("User-Agent", "lets_go_slavgorod_App")
            }
            
            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                                val response = connection.inputStream.bufferedReader().use { it.readText() }
                                    val json = JSONObject(response)
                
                val versionName = json.optString("tag_name", "1.0.0")
                val assets = json.optJSONArray("assets")
                val downloadUrl = if (assets != null && assets.length() > 0) {
                    assets.getJSONObject(0).optString("browser_download_url", "")
                } else {
                    ""
                }
                val releaseNotes = json.optString("body", "")
                
                UpdateInfo(versionName, downloadUrl, releaseNotes)
            } else {
                Log.w(TAG, "GitHub API returned error code: $responseCode")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching latest release", e)
            null
        } finally {
            connection?.disconnect()
        }
    }
    
    /**
     * Сравнивает версии приложения
     */
    private fun compareVersions(currentVersion: String, latestVersion: String): Boolean {
        return try {
            val current = parseVersion(currentVersion)
            val latest = parseVersion(latestVersion)
            
            // Сравниваем версии
            for (i in 0 until max(current.size, latest.size)) {
                val currentPart = current.getOrNull(i) ?: 0
                val latestPart = latest.getOrNull(i) ?: 0
                
                when {
                    latestPart > currentPart -> return true
                    latestPart < currentPart -> return false
                }
            }
            false
        } catch (e: Exception) {
            Log.e(TAG, "Error comparing versions", e)
            false
        }
    }
    
    /**
     * Парсит версию в массив чисел
     */
    private fun parseVersion(version: String): List<Int> {
        return version.replace("v", "").split(".")
            .mapNotNull { it.toIntOrNull() }
    }
    
    /**
     * Получает текущую версию приложения
     */
    private fun getCurrentVersion(): String {
        return try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            packageInfo.versionName
        } catch (e: Exception) {
            Log.e(TAG, "Error getting current version", e)
            "1.0.0"
        }.toString()
    }
    
    /**
     * Проверяет наличие обновлений
     */
    suspend fun checkForUpdatesWithResult(): UpdateResult = withContext(Dispatchers.IO) {
        try {
            if (!isNetworkAvailable()) {
                return@withContext UpdateResult(false, error = "Нет интернет-соединения")
            }
            
            val currentVersion = getCurrentVersion()
            Log.d(TAG, "Current version: $currentVersion")
            
            val latestRelease = withTimeoutOrNull(TIMEOUT_MS) {
                fetchLatestRelease()
            }
            
            if (latestRelease == null) {
                return@withContext UpdateResult(false, error = "Не удалось получить информацию об обновлениях")
            }
            
            Log.d(TAG, "Latest version: ${latestRelease.versionName}")
            
            if (compareVersions(currentVersion, latestRelease.versionName)) {
                Log.i(TAG, "Update available: ${latestRelease.versionName}")
                UpdateResult(true, latestRelease)
            } else {
                Log.i(TAG, "No updates available")
                UpdateResult(true, null)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error checking for updates", e)
            UpdateResult(false, error = "Ошибка при проверке обновлений: ${e.message}")
        }
    }
    
    /**
     * Запускает загрузку обновления через браузер
     */
    fun downloadUpdate(updateInfo: UpdateInfo) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, updateInfo.downloadUrl.toUri())
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Error opening download URL", e)
        }
    }
}
