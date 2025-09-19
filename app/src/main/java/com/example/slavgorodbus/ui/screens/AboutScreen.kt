package com.example.slavgorodbus.ui.screens

import android.content.ActivityNotFoundException
import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import android.util.Log
import com.example.slavgorodbus.BuildConfig
import com.example.slavgorodbus.R
import androidx.core.net.toUri

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(
    modifier: Modifier = Modifier
) {

    val developerSectionTitleText = stringResource(id = R.string.developer_section_title) // "Разработал: VseMirka200"

    val developerGitHubUrl = stringResource(id = R.string.developer_github_url_value)
    val linkTextGitHub = stringResource(id = R.string.link_text_github)

    // Строки для обратной связи через Telegram
    val feedbackSectionTitle = stringResource(id = R.string.feedback_section_title)
    val feedbackLinkText = stringResource(id = R.string.feedback_link_text)
    val feedbackTelegramBotUsername = stringResource(id = R.string.feedback_telegram_bot_username)

    val appVersion = BuildConfig.VERSION_NAME
    val feedbackTelegramUrl = "https://t.me/$feedbackTelegramBotUsername"

    Column(modifier = modifier.fillMaxSize()) {
        TopAppBar(
            title = {
                Text(
                    text = stringResource(id = R.string.about_screen_title),
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
            )
        )
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
                    .padding(bottom = 72.dp),
            ) {
                Text(
                    text = stringResource(id = R.string.app_name),
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Раздел Разработчик
                Text(
                    text = developerSectionTitleText,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Раздел Ссылки
                Text(
                    stringResource(id = R.string.links_section_title),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(8.dp))
                ClickableLinkText(
                    text = linkTextGitHub,
                    url = developerGitHubUrl
                )
                Spacer(modifier = Modifier.height(24.dp))

                // Раздел Обратная связь
                Text(
                    text = feedbackSectionTitle,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(8.dp))
                ClickableLinkText(
                    text = feedbackLinkText,
                    url = feedbackTelegramUrl
                )
            }

            Text(
                text = stringResource(R.string.app_version_label, appVersion),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
            )
        }
    }
}

@Composable
private fun ClickableLinkText(
    text: String,
    url: String,
    modifier: Modifier = Modifier
) {
    val localContext = LocalContext.current
    Text(
        text = text,
        style = MaterialTheme.typography.bodyLarge.copy(
            color = MaterialTheme.colorScheme.primary,
            textDecoration = TextDecoration.Underline,
        ),
        modifier = modifier.clickable {
            val intent = Intent(Intent.ACTION_VIEW, url.toUri())
            try {
                localContext.startActivity(intent)
            } catch (e: ActivityNotFoundException) {
                Log.w("AboutScreen", "No application can handle this URL: $url", e)
                android.widget.Toast.makeText(
                    localContext,
                    localContext.getString(R.string.error_no_browser),
                    android.widget.Toast.LENGTH_LONG
                ).show()
            } catch (e: Exception) {
                Log.e("AboutScreen", "Could not open URL: $url", e)
                android.widget.Toast.makeText(
                    localContext,
                    localContext.getString(R.string.error_cant_open_link),
                    android.widget.Toast.LENGTH_SHORT
                ).show()
            }
        }
    )
}