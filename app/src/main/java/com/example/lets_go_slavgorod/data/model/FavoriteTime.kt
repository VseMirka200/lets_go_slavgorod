package com.example.lets_go_slavgorod.data.model

data class FavoriteTime(
    val id: String,
    val routeId: String,       // ID маршрута
    val routeNumber: String,   // Номер маршрута
    val routeName: String,     // Название маршрута
    val stopName: String,      // Название остановки
    val departureTime: String, // Время отправления
    val dayOfWeek: Int,        // День недели
    val departurePoint: String,// Пункт отправления
    val isActive: Boolean = true
)
