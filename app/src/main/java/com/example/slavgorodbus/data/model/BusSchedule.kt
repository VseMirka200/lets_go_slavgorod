package com.example.slavgorodbus.data.model

data class BusSchedule(
    val id: String,
    val routeId: String,
    val stopName: String,
    val departureTime: String,
    val dayOfWeek: Int,
    val isWeekend: Boolean = false,
    val notes: String? = null,
    val departurePoint: String
)