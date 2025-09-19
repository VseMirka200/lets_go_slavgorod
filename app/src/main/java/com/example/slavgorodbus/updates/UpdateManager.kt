package com.example.slavgorodbus.updates

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
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
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class UpdateManager(private val context: Context) {
    
    data class AppVersion(
        val versionName: String,
        val versionCode: Int,
        val downloadUrl: String,
        val releaseNotes: String
    )
    
    suspend fun checkForUpdates(): AppVersion? {
        return withContext(Dispatchers.IO) {
            try {
                // Получаем текущую версию приложения
                val currentVersion = context.packageManager
                    .getPackageInfo(context.packageName, 0).versionCode
                
                // URL для получения информации о последнем релизе
                val apiUrl = "https://api.github.com/repos/VseMirka200/Lets_go_Slavgorod/releases/latest"
                val url = URL(apiUrl)
                val connection = url.openConnection() as HttpURLConnection
                
                connection.requestMethod = "GET"
                connection.setRequestProperty("Accept", "application/vnd.github.v3+json")
                
                if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                    val response = connection.inputStream.bufferedReader().use { it.readText() }
                    val json = JSONObject(response)
                    
                    val latestVersion = json.getString("tag_name").removePrefix("v")
                    val downloadUrl = json.getJSONArray("assets")
                        .getJSONObject(0)
                        .getString("browser_download_url")
                    val releaseNotes = json.getString("body")
                    
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
                    
                    if (versionCode > currentVersion) {
                        AppVersion(
                            versionName = latestVersion,
                            versionCode = versionCode,
                            downloadUrl = downloadUrl,
                            releaseNotes = releaseNotes
                        )
                    } else null
                } else null
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }
    
    fun downloadUpdate(version: AppVersion) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(version.downloadUrl))
        context.startActivity(intent)
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
                text = "Доступно обновление",
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Text(
                text = "Версия ${version.versionName}\n\n${version.releaseNotes}",
                style = MaterialTheme.typography.bodyMedium
            )
        },
        confirmButton = {
            Button(onClick = onDownload) {
                Text("Обновить")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Позже")
            }
        }
    )
}
