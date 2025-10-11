package com.example.lets_go_slavgorod.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorite_times")
data class FavoriteTimeEntity(
    @PrimaryKey
    val id: String,

    @ColumnInfo(name = "route_id")
    val routeId: String,

    @ColumnInfo(name = "route_number")
    val routeNumber: String,

    @ColumnInfo(name = "route_name")
    val routeName: String,

    @ColumnInfo(name = "stop_name")
    val stopName: String,

    @ColumnInfo(name = "departure_time")
    val departureTime: String,

    @ColumnInfo(name = "day_of_week")
    val dayOfWeek: Int,

    @ColumnInfo(name = "departure_point")
    val departurePoint: String,

    @ColumnInfo(name = "added_date")
    val addedDate: Long,

    @ColumnInfo(name = "is_active", defaultValue = "true")
    val isActive: Boolean = true
)