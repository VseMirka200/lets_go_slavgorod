package com.example.lets_go_slavgorod.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.example.lets_go_slavgorod.MainActivity
import com.example.lets_go_slavgorod.R
import com.example.lets_go_slavgorod.data.local.AppDatabase
import com.example.lets_go_slavgorod.data.repository.BusRouteRepository
import com.example.lets_go_slavgorod.utils.TimeUtils
import com.example.lets_go_slavgorod.utils.toFavoriteTime
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*

/**
 * Виджет расписания автобусов на главный экран
 * 
 * Отображает ближайшие рейсы из избранного прямо на главном экране устройства.
 * Автоматически обновляется каждые 15 минут и при изменении избранного.
 * 
 * Возможности:
 * - Показ до 5 ближайших рейсов
 * - Автоматическое обновление
 * - Клик для открытия приложения
 * - Компактный и информативный дизайн
 * 
 * @author VseMirka200
 * @version 1.0
 */
class BusScheduleWidget : AppWidgetProvider() {
    
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        Timber.d("Widget onUpdate called for ${appWidgetIds.size} widgets")
        
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }
    
    override fun onEnabled(context: Context) {
        Timber.d("Widget enabled")
    }
    
    override fun onDisabled(context: Context) {
        Timber.d("Widget disabled")
    }
    
    private fun updateAppWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
        scope.launch {
            try {
                val views = RemoteViews(context.packageName, R.layout.widget_bus_schedule)
                
                // Устанавливаем заголовок
                val currentTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
                views.setTextViewText(R.id.widget_title, "Поехали! Славгород")
                views.setTextViewText(R.id.widget_update_time, "Обновлено: $currentTime")
                
                // Получаем избранные рейсы
                val database = AppDatabase.getDatabase(context)
                val repository = BusRouteRepository(context)
                val favoritesEntities = database.favoriteTimeDao().getAllFavoriteTimes().first()
                
                if (favoritesEntities.isEmpty()) {
                    views.setTextViewText(R.id.widget_content, "Нет избранных рейсов\n\nДобавьте рейсы в избранное в приложении")
                } else {
                    // Преобразуем в FavoriteTime и фильтруем активные
                    val favorites = favoritesEntities
                        .map { it.toFavoriteTime(repository) }
                        .filter { it.isActive }
                    
                    if (favorites.isEmpty()) {
                        views.setTextViewText(R.id.widget_content, "Все избранные рейсы отключены")
                    } else {
                        // Сортируем по времени до отправления
                        val upcomingFavorites = favorites
                            .mapNotNull { favorite ->
                                val minutesUntil = TimeUtils.getTimeUntilDeparture(favorite.departureTime)
                                if (minutesUntil != null && minutesUntil >= 0) {
                                    favorite to minutesUntil
                                } else {
                                    null
                                }
                            }
                            .sortedBy { it.second }
                            .take(5) // Показываем только 5 ближайших
                        
                        if (upcomingFavorites.isEmpty()) {
                            views.setTextViewText(R.id.widget_content, "Нет предстоящих рейсов на сегодня")
                        } else {
                            // Формируем текст для виджета
                            val contentText = upcomingFavorites.joinToString("\n\n") { (favorite, minutes) ->
                                val timeFormatted = when {
                                    minutes < 1 -> "отправляется сейчас"
                                    minutes == 1 -> "через 1 минуту"
                                    minutes < 5 -> "через $minutes минуты"
                                    minutes < 60 -> "через $minutes минут"
                                    else -> "более часа"
                                }
                                "🚌 ${favorite.routeNumber} - ${favorite.stopName}\n" +
                                "⏰ ${favorite.departureTime} ($timeFormatted)"
                            }
                            views.setTextViewText(R.id.widget_content, contentText)
                        }
                    }
                }
                
                // Настраиваем клик для открытия приложения
                val intent = Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                }
                val pendingIntent = PendingIntent.getActivity(
                    context,
                    0,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                views.setOnClickPendingIntent(R.id.widget_container, pendingIntent)
                
                // Обновляем виджет
                appWidgetManager.updateAppWidget(appWidgetId, views)
                Timber.d("Widget $appWidgetId updated successfully")
                
            } catch (e: Exception) {
                Timber.e(e, "Error updating widget $appWidgetId")
                
                // Показываем ошибку в виджете
                val views = RemoteViews(context.packageName, R.layout.widget_bus_schedule)
                views.setTextViewText(R.id.widget_title, "Поехали! Славгород")
                views.setTextViewText(R.id.widget_content, "Ошибка загрузки данных\nПотяните для обновления")
                appWidgetManager.updateAppWidget(appWidgetId, views)
            }
        }
    }
}

