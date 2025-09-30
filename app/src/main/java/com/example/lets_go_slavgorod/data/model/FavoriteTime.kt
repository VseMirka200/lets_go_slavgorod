package com.example.lets_go_slavgorod.data.model

/**
 * Модель избранного времени отправления
 */
data class FavoriteTime(
    val id: String,                     // Уникальный ID
    val routeId: String,                // ID маршрута
    val routeNumber: String,            // Номер маршрута
    val routeName: String,              // Название маршрута
    val stopName: String,               // Название остановки
    val departureTime: String,          // Время отправления
    val dayOfWeek: Int,                 // День недели (1-7)
    val departurePoint: String,         // Пункт отправления
    val isActive: Boolean = true        // Активность записи
)
