package com.example.slavgorodbus.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log

@Suppress("DEPRECATION")
class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        Log.i("AlarmReceiver", "onReceive triggered.")

        if (context == null) {
            Log.e("AlarmReceiver", "Context is null. Cannot proceed.")
            return
        }
        if (intent == null) {
            Log.e("AlarmReceiver", "Intent is null. Cannot proceed.")
            return
        }

        val action = intent.action
        val extras: Bundle? = intent.extras

        Log.d("AlarmReceiver", "Received alarm. Action: $action")
        if (extras != null) {
            Log.d("AlarmReceiver", "Extras content:")
            for (key in extras.keySet()) {
                Log.d("AlarmReceiver", "  Key: $key, Value: ${extras.get(key)}")
            }
        } else {
            Log.d("AlarmReceiver", "Extras bundle is null.")
        }

        val favoriteId = intent.getStringExtra("FAVORITE_ID")
        val routeInfo = intent.getStringExtra("ROUTE_INFO") ?: "Ваш автобус (default)"
        val departureTimeInfo = intent.getStringExtra("DEPARTURE_TIME_INFO") ?: "неизвестно (default)"
        val destinationInfo = intent.getStringExtra("DESTINATION_INFO") ?: ""
        val departurePointInfo = intent.getStringExtra("DEPARTURE_POINT_INFO") ?: ""

        Log.d(
            "AlarmReceiver",
            "Parsed data: favoriteId='$favoriteId', routeInfo='$routeInfo', " +
                    "departureTimeInfo='$departureTimeInfo', destinationInfo='$destinationInfo', " +
                    "departurePointInfo='$departurePointInfo'"
        )

        if (favoriteId == null) {
            Log.e("AlarmReceiver", "Critical: Favorite ID is missing in the intent. Notification will not be shown.")
            return
        }

        try {
            Log.d("AlarmReceiver", "Attempting to show notification for favoriteId: $favoriteId")
            NotificationHelper.showDepartureNotification(
                context,
                favoriteId,
                routeInfo,
                departureTimeInfo,
                destinationInfo,
                departurePointInfo
            )
            Log.i("AlarmReceiver", "NotificationHelper.showDepartureNotification was called successfully for favoriteId: $favoriteId.")
        } catch (e: Exception) {
            Log.e("AlarmReceiver", "Error calling NotificationHelper.showDepartureNotification for favoriteId: $favoriteId", e)
        }

        Log.d("AlarmReceiver", "Finished processing for favoriteId: $favoriteId.")
    }
}