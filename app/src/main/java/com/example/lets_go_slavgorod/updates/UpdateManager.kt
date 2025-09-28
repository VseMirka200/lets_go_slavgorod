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

import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import timber.log.Timber
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import kotlin.math.max

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
            val network = cm.activeNetwork
            val capabilities = cm.getNetworkCapabilities(network)
            capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
        } ?: false
    }
    
    /**
     * Получает информацию о последнем релизе с GitHub API
     */
    @SuppressLint("TimberArgCount")
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
                setRequestProperty("Connection", "close")
            }
            
            val responseCode = connection.responseCode
            Timber.d("GitHub API response code: $responseCode")
            
            when (responseCode) {
                HttpURLConnection.HTTP_OK -> {
                    val response = connection.inputStream.bufferedReader().use { it.readText() }
                    if (response.isBlank()) {
                        Timber.w("Empty response from GitHub API")
                        return@withContext null
                    }
                    
                    val json = JSONObject(response)
                    
                    val versionName = json.optString("tag_name", "").trim()
                    val assets = json.optJSONArray("assets")
                    val downloadUrl = if (assets != null && assets.length() > 0) {
                        assets.getJSONObject(0).optString("browser_download_url", "").trim()
                    } else {
                        ""
                    }
                    val releaseNotes = json.optString("body", "").trim()
                    
                    // Валидация полученных данных
                    if (versionName.isBlank()) {
                        Timber.w("Empty version name in response")
                        return@withContext null
                    }
                    
                    Timber.d("Successfully parsed release info: version=$versionName, hasDownloadUrl=${downloadUrl.isNotBlank()}")
                    UpdateInfo(versionName, downloadUrl, releaseNotes)
                }
                HttpURLConnection.HTTP_NOT_FOUND -> {
                    Timber.w("Repository not found (404)")
                    null
                }
                HttpURLConnection.HTTP_FORBIDDEN -> {
                    Timber.w("Access forbidden (403) - rate limit or permissions")
                    null
                }
                HttpURLConnection.HTTP_UNAVAILABLE -> {
                    Timber.w("Service unavailable (503)")
                    null
                }
                else -> {
                    Timber.w("GitHub API returned unexpected error code: $responseCode")
                    null
                }
            }
        } catch (e: java.net.SocketTimeoutException) {
            Timber.e(e, "Timeout while fetching latest release")
            null
        } catch (e: java.net.UnknownHostException) {
            Timber.e(e, "Unknown host while fetching latest release")
            null
        } catch (e: java.net.ConnectException) {
            Timber.e(e, "Connection failed while fetching latest release")
            null
        } catch (e: org.json.JSONException) {
            Timber.e(e, "JSON parsing error while fetching latest release")
            null
        } catch (e: Exception) {
            Timber.e(e, "Unexpected error while fetching latest release")
            null
        } finally {
            try {
                connection?.disconnect()
            } catch (e: Exception) {
                Timber.w(e, "Error disconnecting from GitHub API")
            }
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
            Timber.e(e, "Error comparing versions")
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
            Timber.e(e, "Error getting current version")
            "1.0.0"
        }.toString()
    }
    
    /**
     * Проверяет наличие обновлений
     */
    suspend fun checkForUpdatesWithResult(): UpdateResult = withContext(Dispatchers.IO) {
        try {
            if (!isNetworkAvailable()) {
                Timber.w("No network connection available")
                return@withContext UpdateResult(false, error = "Нет интернет-соединения")
            }
            
            val currentVersion = getCurrentVersion()
            Timber.d("Current version: $currentVersion")
            
            val latestRelease = withTimeoutOrNull(TIMEOUT_MS) {
                fetchLatestRelease()
            }
            
            if (latestRelease == null) {
                Timber.w("Failed to fetch latest release information")
                return@withContext UpdateResult(false, error = "Не удалось получить информацию об обновлениях")
            }
            
            // Валидация полученных данных
            if (latestRelease.versionName.isBlank()) {
                Timber.w("Invalid version name received: '${latestRelease.versionName}'")
                return@withContext UpdateResult(false, error = "Получена некорректная информация о версии")
            }
            
            if (latestRelease.downloadUrl.isBlank()) {
                Timber.w("No download URL available for version: ${latestRelease.versionName}")
                return@withContext UpdateResult(false, error = "Ссылка для загрузки недоступна")
            }
            
            Timber.d("Latest version: ${latestRelease.versionName}")
            
            if (compareVersions(currentVersion, latestRelease.versionName)) {
                Timber.i("Update available: ${latestRelease.versionName}")
                UpdateResult(true, latestRelease)
            } else {
                Timber.i("No updates available")
                UpdateResult(true, null)
            }
        } catch (e: java.net.SocketTimeoutException) {
            Timber.e(e, "Network timeout during update check")
            UpdateResult(false, error = "Превышено время ожидания. Проверьте соединение.")
        } catch (e: java.net.UnknownHostException) {
            Timber.e(e, "Unknown host during update check")
            UpdateResult(false, error = "Не удалось подключиться к серверу обновлений")
        } catch (e: java.net.ConnectException) {
            Timber.e(e, "Connection failed during update check")
            UpdateResult(false, error = "Ошибка подключения к серверу")
        } catch (e: Exception) {
            Timber.e(e, "Unexpected error during update check")
            UpdateResult(false, error = "Неожиданная ошибка при проверке обновлений: ${e.message}")
        }
    }

}
