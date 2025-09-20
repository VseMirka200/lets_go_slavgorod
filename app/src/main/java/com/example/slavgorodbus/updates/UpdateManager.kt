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

package com.example.slavgorodbus.updates

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
        /** Тег для логирования */
        private const val TAG = "UpdateManager"
        
        /** URL для получения информации о последнем релизе из GitHub API */
        private const val GITHUB_API_URL = "https://api.github.com/repos/VseMirka200/Lets_go_Slavgorod/releases/latest"
        
        /** Таймаут для HTTP запросов в миллисекундах */
        private const val REQUEST_TIMEOUT = 10000L // 10 секунд
        
        /** User-Agent для HTTP запросов к GitHub */
        private const val USER_AGENT = "SlavgorodBus/1.0.3"
    }
    
    /**
     * Данные о версии приложения
     * 
     * Содержит всю необходимую информацию о версии приложения,
     * полученную с GitHub API
     * 
     * @param versionName Название версии в формате "major.minor.patch" (например, "1.0.3")
     * @param versionCode Числовой код версии для внутреннего сравнения (например, 10003)
     * @param downloadUrl URL для скачивания APK файла с GitHub
     * @param releaseNotes Описание изменений в релизе (release notes)
     */
    data class AppVersion(
        val versionName: String,
        val versionCode: Int,
        val downloadUrl: String,
        val releaseNotes: String,
    )
    
    /**
     * Результат проверки обновлений
     * 
     * Инкапсулирует результат операции проверки обновлений,
     * включая информацию об успешности и возможные ошибки
     * 
     * @param success Успешность операции проверки обновлений
     * @param update Информация об доступном обновлении (null если обновлений нет)
     * @param error Описание ошибки (null если операция успешна)
     */
    data class UpdateResult(
        val success: Boolean,
        val update: AppVersion? = null,
        val error: String? = null,
    )
    
    /**
     * Проверяет наличие обновлений (упрощенная версия)
     * 
     * Выполняет проверку обновлений и возвращает только информацию об обновлении,
     * игнорируя детали об ошибках. Удобно для простых случаев использования.
     * 
     * @return Информация об обновлении или null, если обновлений нет или произошла ошибка
     */
    suspend fun checkForUpdates(): AppVersion? {
        val result = checkForUpdatesWithResult()
        return if (result.success) result.update else null
    }
    
    /**
     * Проверяет наличие обновлений с детальной информацией об ошибках
     * 
     * Основная функция для проверки обновлений. Выполняет следующие шаги:
     * 1. Проверяет доступность интернет-соединения
     * 2. Получает текущую версию приложения
     * 3. Отправляет запрос к GitHub API
     * 4. Парсит ответ и сравнивает версии
     * 5. Возвращает детальный результат операции
     * 
     * @return UpdateResult с информацией об успехе операции и возможных ошибках
     */
    suspend fun checkForUpdatesWithResult(): UpdateResult {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "Начинаем проверку обновлений с GitHub...")
                
                // Шаг 1: Проверяем доступность интернета перед отправкой запроса
                if (!isInternetAvailable()) {
                    Log.w(TAG, "Нет интернет-соединения")
                    return@withContext UpdateResult(
                        success = false,
                        error = "Нет интернет-соединения"
                    )
                }
                
                // Шаг 2: Получаем текущую версию приложения из PackageManager
                val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
                val currentVersionCode = packageInfo.versionCode
                val currentVersionName = packageInfo.versionName ?: "1.0.0" // Fallback если versionName null
                
                Log.d(TAG, "Текущая версия приложения: $currentVersionName (код: $currentVersionCode)")
                
                // Шаг 3: Выполняем HTTP запрос к GitHub API с таймаутом
                val result = withTimeoutOrNull(REQUEST_TIMEOUT) {
                    try {
                        // Создаем HTTP соединение с GitHub API
                        val url = URL(GITHUB_API_URL)
                        val connection = url.openConnection() as HttpURLConnection
                        
                        // Настраиваем параметры HTTP запроса
                        connection.requestMethod = "GET"
                        connection.setRequestProperty("Accept", "application/vnd.github.v3+json")
                        connection.setRequestProperty("User-Agent", USER_AGENT)
                        connection.connectTimeout = REQUEST_TIMEOUT.toInt()
                        connection.readTimeout = REQUEST_TIMEOUT.toInt()
                        
                        Log.d(TAG, "Отправляем запрос к GitHub API: $GITHUB_API_URL")
                        
                        // Шаг 4: Обрабатываем ответ от GitHub API в зависимости от HTTP статуса
                        when (connection.responseCode) {
                            HttpURLConnection.HTTP_OK -> {
                                // Успешный ответ - парсим JSON
                                val response = connection.inputStream.bufferedReader().use { it.readText() }
                                Log.d(TAG, "Получен ответ от GitHub API")
                                
                                try {
                                    val json = JSONObject(response)
                                    
                                    // Извлекаем название версии (убираем префикс "v" если есть)
                                    val latestVersion = json.getString("tag_name").removePrefix("v")
                                    
                                    // Проверяем наличие файлов для скачивания в релизе
                                    val assetsArray = json.getJSONArray("assets")
                                    if (assetsArray.length() == 0) {
                                        Log.w(TAG, "Нет файлов для скачивания в релизе")
                                        return@withTimeoutOrNull UpdateResult(success = false, error = "Нет файлов для скачивания")
                                    }
                                    
                                    // Получаем URL для скачивания и описание изменений
                                    val downloadUrl = assetsArray.getJSONObject(0).getString("browser_download_url")
                                    val releaseNotes = json.optString("body", "Нет описания изменений")
                                    
                                    Log.d(TAG, "Последняя версия на GitHub: $latestVersion")
                                    
                                    // Шаг 5: Сравниваем версии по строковому представлению
                                    val isNewVersionAvailable = isVersionNewer(latestVersion, currentVersionName)
                                    
                                    Log.d(TAG, "Сравнение версий: GitHub=$latestVersion, Текущая=$currentVersionName, Новее=$isNewVersionAvailable")
                                    
                                    // Возвращаем результат сравнения версий
                                    if (isNewVersionAvailable) {
                                        val update = AppVersion(
                                            versionName = latestVersion,
                                            versionCode = currentVersionCode + 1, // Увеличиваем код версии для новой версии
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
    
    /**
     * Проверяет доступность интернет-соединения
     * 
     * Использует современный API NetworkCapabilities для проверки доступности
     * различных типов сетевых подключений. Поддерживает Android 6.0+ (API 23+).
     * 
     * Проверяет следующие типы подключений:
     * - Wi-Fi (TRANSPORT_WIFI)
     * - Мобильный интернет (TRANSPORT_CELLULAR) 
     * - Ethernet (TRANSPORT_ETHERNET)
     * 
     * @return true если интернет доступен через любой из поддерживаемых типов подключения,
     *         false в противном случае или при ошибке
     */
    private fun isInternetAvailable(): Boolean {
        return try {
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

            // Получаем активную сеть
            val network = connectivityManager.activeNetwork ?: return false
            val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false

            // Проверяем доступность различных типов сетевых подключений
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка при проверке интернет-соединения", e)
            false
        }
    }
    
    /**
     * Запускает загрузку обновления через браузер
     * 
     * Создает Intent для открытия URL скачивания в браузере по умолчанию.
     * Пользователь может выбрать, как загрузить файл (через браузер, менеджер загрузок и т.д.)
     * 
     * @param version Информация об обновлении, содержащая URL для скачивания
     */
    fun downloadUpdate(version: AppVersion) {
        val intent = Intent(Intent.ACTION_VIEW, version.downloadUrl.toUri())
        context.startActivity(intent)
    }

    /**
     * Сравнивает две версии в формате "major.minor.patch"
     * 
     * Выполняет семантическое сравнение версий, разбивая их на компоненты
     * и сравнивая по порядку: major, minor, patch. Поддерживает версии
     * разной длины (например, "1.2" vs "1.2.0").
     * 
     * Примеры:
     * - "1.2.0" > "1.1.0" → true
     * - "1.1.0" > "1.2.0" → false  
     * - "1.1.0" = "1.1.0" → false
     * - "1.2" > "1.1.0" → true
     * 
     * @param version1 Первая версия для сравнения (может быть null)
     * @param version2 Вторая версия для сравнения (может быть null)
     * @return true если version1 новее version2, false в противном случае или при ошибке
     */
    private fun isVersionNewer(version1: String?, version2: String?): Boolean {
        return try {
            // Проверяем на null значения
            if (version1 == null || version2 == null) {
                Log.w(TAG, "Одна из версий null: version1=$version1, version2=$version2")
                return false
            }
            
            // Разбиваем версии на числовые компоненты
            val parts1 = version1.split(".").map { it.toInt() }
            val parts2 = version2.split(".").map { it.toInt() }
            
            // Дополняем до одинаковой длины нулями для корректного сравнения
            val maxLength = max(parts1.size, parts2.size)
            val v1 = parts1 + List(maxLength - parts1.size) { 0 }
            val v2 = parts2 + List(maxLength - parts2.size) { 0 }
            
            // Сравниваем версии по компонентам (major.minor.patch)
            for (i in v1.indices) {
                when {
                    v1[i] > v2[i] -> return true  // Первая версия новее
                    v1[i] < v2[i] -> return false // Вторая версия новее
                    // Если компоненты равны, переходим к следующему
                }
            }
            false // Версии полностью одинаковые
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка при сравнении версий: $version1 vs $version2", e)
            false
        }
    }

}

/**
 * Диалог для отображения информации об обновлении
 * 
 * Показывает пользователю информацию о доступном обновлении,
 * включая номер версии и описание изменений. Предоставляет
 * возможность скачать обновление или отложить его.
 * 
 * @param version Информация о новой версии приложения
 * @param onDismiss Колбэк, вызываемый при закрытии диалога (отмена)
 * @param onDownload Колбэк, вызываемый при нажатии кнопки скачивания
 */
@Composable
fun UpdateDialog(
    version: UpdateManager.AppVersion,
    onDismiss: () -> Unit,
    onDownload: () -> Unit,
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
            Button(onClick = { onDownload() }) {
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
