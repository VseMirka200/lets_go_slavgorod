package com.example.lets_go_slavgorod.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.lets_go_slavgorod.MainActivity
import com.example.lets_go_slavgorod.R
import java.util.Locale

/**
 * Вспомогательный класс для работы с уведомлениями
 * 
 * Основные функции:
 * - Создание канала уведомлений
 * - Отображение уведомлений о времени отправления автобусов
 * - Обработка разрешений для уведомлений
 */
object NotificationHelper {
    // ID канала уведомлений
    private const val CHANNEL_ID = "bus_departure_channel"
    // ID канала для обновлений
    private const val UPDATE_CHANNEL_ID = "app_update_channel"
    // Название канала уведомлений
    private const val CHANNEL_NAME = "Уведомления об отправлении"
    // Название канала обновлений
    private const val UPDATE_CHANNEL_NAME = "Обновления приложения"
    // Базовый ID для уведомлений
    private const val NOTIFICATION_ID_BASE = 1000
    // ID уведомления об обновлении
    private const val UPDATE_NOTIFICATION_ID = 9999

    /**
     * Создает канал уведомлений для Android 8.0+
     * 
     * Настраивает канал с высоким приоритетом для уведомлений о времени отправления
     * 
     * @param context контекст приложения
     */
    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            
            // Канал для уведомлений об отправлении автобусов
            val departureChannel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Уведомления о скором отправлении автобуса"
            }
            notificationManager.createNotificationChannel(departureChannel)
            
            // Канал для уведомлений об обновлениях
            val updateChannel = NotificationChannel(
                UPDATE_CHANNEL_ID,
                UPDATE_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Уведомления о доступных обновлениях приложения"
            }
            notificationManager.createNotificationChannel(updateChannel)
            
            Log.d("NotificationHelper", "Notification channels created: $CHANNEL_ID, $UPDATE_CHANNEL_ID")
        }
    }

    /**
     * Отображает уведомление о времени отправления автобуса
     * 
     * Создает уведомление с информацией о маршруте, времени отправления и точке отправления.
     * Проверяет разрешения для Android 13+ и обрабатывает ошибки.
     * 
     * @param context контекст приложения
     * @param favoriteTimeId ID избранного времени
     * @param routeInfo информация о маршруте
     * @param departureTimeInfo время отправления
     * @param destinationInfo информация о пункте назначения (не используется)
     * @param departurePointInfo информация о точке отправления
     */
    fun showDepartureNotification(
        context: Context,
        favoriteTimeId: String,
        routeInfo: String,
        departureTimeInfo: String,
        @Suppress("UNUSED_PARAMETER") destinationInfo: String,
        departurePointInfo: String
    ) {
        createNotificationChannel(context.applicationContext)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val uniqueRequestId = (NOTIFICATION_ID_BASE.toString() + favoriteTimeId).hashCode()

        val pendingIntent = PendingIntent.getActivity(
            context,
            uniqueRequestId,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val smallIconResId = R.drawable.ic_stat_directions_bus
        val largeIconResId = R.drawable.ic_stat_directions_bus
        val combinedTitleText = "$routeInfo ${departureTimeInfo.lowercase(Locale.getDefault())}"
        val subTextParts = mutableListOf<String>()
        if (departurePointInfo.isNotBlank()) {
            subTextParts.add(departurePointInfo)
        }
        subTextParts.add("Не опаздывайте!")

        val contentSubText = subTextParts.joinToString(separator = ". ")
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(smallIconResId)
            .setLargeIcon(android.graphics.BitmapFactory.decodeResource(context.resources, largeIconResId))
            .setContentTitle(combinedTitleText)
            .setContentText(contentSubText)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .build()

        val notificationManager = NotificationManagerCompat.from(context)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                Log.w("NotificationHelper", "POST_NOTIFICATIONS permission not granted for favoriteTimeId: $favoriteTimeId. Notification will not be shown on Android 13+.")
                return
            }
        }

        notificationManager.notify(uniqueRequestId, notification)

        Log.i("NotificationHelper", "Notification shown with ID $uniqueRequestId for $favoriteTimeId. Combined Title: '$combinedTitleText', SubText: '$contentSubText'")
    }

    /**
     * Отображает уведомление о доступном обновлении приложения
     * 
     * Показывает уведомление с информацией о новой версии и кнопкой для скачивания.
     * При нажатии на уведомление открывается приложение.
     * 
     * @param context контекст приложения
     * @param versionName версия доступного обновления
     * @param releaseNotes описание изменений (опционально)
     */
    fun showUpdateNotification(
        context: Context,
        versionName: String,
        releaseNotes: String? = null
    ) {
        // Создаем Intent для открытия приложения
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            UPDATE_NOTIFICATION_ID,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val smallIconResId = R.drawable.ic_stat_directions_bus
        val title = "Доступно обновление $versionName"
        val contentText = releaseNotes?.takeIf { it.isNotBlank() } 
            ?: "Доступна новая версия приложения с улучшениями и исправлениями."

        val notification = NotificationCompat.Builder(context, UPDATE_CHANNEL_ID)
            .setSmallIcon(smallIconResId)
            .setContentTitle(title)
            .setContentText(contentText)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setCategory(NotificationCompat.CATEGORY_STATUS)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(contentText)
            )
            .build()

        val notificationManager = NotificationManagerCompat.from(context)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                Log.w("NotificationHelper", "POST_NOTIFICATIONS permission not granted for update notification. Notification will not be shown on Android 13+.")
                return
            }
        }

        notificationManager.notify(UPDATE_NOTIFICATION_ID, notification)

        Log.i("NotificationHelper", "Update notification shown for version $versionName")
    }
}