package com.example.slavgorodbus.updates

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.util.Log
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class UpdateManager(private val context: Context) {
    
    companion object {
        private const val TAG = "UpdateManager"
        private const val GITHUB_API_URL = "https://api.github.com/repos/VseMirka200/Lets_go_Slavgorod/releases/latest"
        private const val REQUEST_TIMEOUT = 10000L // 10 секунд
        private const val USER_AGENT = "SlavgorodBus/1.0.3"
    }
    
    data class AppVersion(
        val versionName: String,
        val versionCode: Int,
        val downloadUrl: String,
        val releaseNotes: String
    )
    
    data class UpdateResult(
        val success: Boolean,
        val update: AppVersion? = null,
        val error: String? = null
    )
    
    suspend fun checkForUpdates(): AppVersion? {
        val result = checkForUpdatesWithResult()
        return if (result.success) result.update else null
    }
    
    suspend fun checkForUpdatesWithResult(): UpdateResult {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "Начинаем проверку обновлений с GitHub...")
                
                // Проверяем интернет-соединение
                if (!isInternetAvailable()) {
                    Log.w(TAG, "Нет интернет-соединения")
                    return@withContext UpdateResult(
                        success = false,
                        error = "Нет интернет-соединения"
                    )
                }
                
                // Получаем текущую версию приложения
                val currentVersion = context.packageManager
                    .getPackageInfo(context.packageName, 0).versionCode
                
                Log.d(TAG, "Текущая версия приложения: $currentVersion")
                
                // Выполняем запрос с таймаутом
                val result = withTimeoutOrNull(REQUEST_TIMEOUT) {
                    try {
                        val url = URL(GITHUB_API_URL)
                        val connection = url.openConnection() as HttpURLConnection
                        
                        connection.requestMethod = "GET"
                        connection.setRequestProperty("Accept", "application/vnd.github.v3+json")
                        connection.setRequestProperty("User-Agent", USER_AGENT)
                        connection.connectTimeout = REQUEST_TIMEOUT.toInt()
                        connection.readTimeout = REQUEST_TIMEOUT.toInt()
                        
                        Log.d(TAG, "Отправляем запрос к GitHub API: $GITHUB_API_URL")
                        
                        when (connection.responseCode) {
                            HttpURLConnection.HTTP_OK -> {
                                val response = connection.inputStream.bufferedReader().use { it.readText() }
                                Log.d(TAG, "Получен ответ от GitHub API")
                                
                                try {
                                    val json = JSONObject(response)
                                    
                                    val latestVersion = json.getString("tag_name").removePrefix("v")
                                    
                                    // Проверяем наличие assets
                                    val assetsArray = json.getJSONArray("assets")
                                    if (assetsArray.length() == 0) {
                                        Log.w(TAG, "Нет файлов для скачивания в релизе")
                                        return@withTimeoutOrNull UpdateResult(success = false, error = "Нет файлов для скачивания")
                                    }
                                    
                                    val downloadUrl = assetsArray.getJSONObject(0).getString("browser_download_url")
                                    val releaseNotes = json.optString("body", "Нет описания изменений")
                                    
                                    Log.d(TAG, "Последняя версия на GitHub: $latestVersion")
                                    
                                    // Парсим версию (предполагаем формат типа "1.0.0")
                                    val versionParts = latestVersion.split(".")
                                    val versionCode = if (versionParts.size >= 3) {
                                        versionParts[0].toInt() * 10000 + 
                                        versionParts[1].toInt() * 100 + 
                                        versionParts[2].toInt()
                                    } else {
                                        // Fallback для версий без patch номера
                                        versionParts[0].toInt() * 10000 + 
                                        versionParts[1].toInt() * 100
                                    }
                                    
                                    Log.d(TAG, "Код версии GitHub: $versionCode, текущий код: $currentVersion")
                                    
                                    if (versionCode > currentVersion) {
                                        val update = AppVersion(
                                            versionName = latestVersion,
                                            versionCode = versionCode,
                                            downloadUrl = downloadUrl,
                                            releaseNotes = releaseNotes
                                        )
                                        Log.i(TAG, "Найдено обновление: $latestVersion")
                                        UpdateResult(success = true, update = update)
                                    } else {
                                        Log.i(TAG, "Обновления не найдены")
                                        UpdateResult(success = true, update = null)
                                    }
                                } catch (e: Exception) {
                                    Log.e(TAG, "Ошибка при парсинге JSON ответа", e)
                                    UpdateResult(success = false, error = "Ошибка при обработке ответа сервера")
                                }
                            }
                            HttpURLConnection.HTTP_NOT_FOUND -> {
                                Log.w(TAG, "Репозиторий не найден")
                                UpdateResult(success = false, error = "Репозиторий не найден")
                            }
                            HttpURLConnection.HTTP_FORBIDDEN -> {
                                Log.w(TAG, "Превышен лимит запросов к GitHub API")
                                UpdateResult(success = false, error = "Превышен лимит запросов к GitHub API")
                            }
                            HttpURLConnection.HTTP_UNAVAILABLE -> {
                                Log.w(TAG, "Сервер недоступен")
                                UpdateResult(success = false, error = "Сервер недоступен, попробуйте позже")
                            }
                            else -> {
                                Log.w(TAG, "Ошибка HTTP: ${connection.responseCode}")
                                UpdateResult(success = false, error = "Ошибка сервера: ${connection.responseCode}")
                            }
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Ошибка при выполнении HTTP запроса", e)
                        UpdateResult(success = false, error = "Ошибка сети: ${e.message}")
                    }
                }
                
                if (result == null) {
                    Log.w(TAG, "Таймаут при проверке обновлений")
                    UpdateResult(success = false, error = "Таймаут соединения")
                } else {
                    result
                }
                
            } catch (e: Exception) {
                Log.e(TAG, "Ошибка при проверке обновлений", e)
                UpdateResult(
                    success = false,
                    error = "Ошибка: ${e.message}"
                )
            }
        }
    }
    
    private fun isInternetAvailable(): Boolean {
        return try {
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                val network = connectivityManager.activeNetwork ?: return false
                val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
                
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
            } else {
                // Для старых версий Android
                @Suppress("DEPRECATION")
                val networkInfo = connectivityManager.activeNetworkInfo
                networkInfo?.isConnectedOrConnecting == true
            }
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка при проверке интернет-соединения", e)
            false
        }
    }
    
    fun downloadUpdate(version: AppVersion) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(version.downloadUrl))
        context.startActivity(intent)
    }
    
    suspend fun testConnection(): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val url = URL("https://api.github.com")
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.connectTimeout = 5000
                connection.readTimeout = 5000
                
                val responseCode = connection.responseCode
                Log.d(TAG, "Тест подключения к GitHub: $responseCode")
                responseCode == HttpURLConnection.HTTP_OK
            } catch (e: Exception) {
                Log.e(TAG, "Ошибка при тесте подключения к GitHub", e)
                false
            }
        }
    }
}

@Composable
fun UpdateChecker(
    activity: Activity,
    onUpdateAvailable: (UpdateManager.AppVersion) -> Unit
) {
    var updateAvailable by remember { mutableStateOf<UpdateManager.AppVersion?>(null) }
    
    LaunchedEffect(Unit) {
        val updateManager = UpdateManager(activity)
        updateAvailable = updateManager.checkForUpdates()
    }
    
    updateAvailable?.let { version ->
        onUpdateAvailable(version)
    }
}

@Composable
fun UpdateDialog(
    version: UpdateManager.AppVersion,
    onDismiss: () -> Unit,
    onDownload: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "🔄 Доступно обновление",
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Text(
                text = "📱 Новая версия: ${version.versionName}\n\n📝 Изменения:\n${version.releaseNotes}\n\n💾 Обновление будет загружено через браузер",
                style = MaterialTheme.typography.bodyMedium
            )
        },
        confirmButton = {
            Button(onClick = onDownload) {
                Text("📥 Скачать")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("⏰ Позже")
            }
        }
    )
}
