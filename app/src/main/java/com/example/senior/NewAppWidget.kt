package com.example.senior

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.RemoteViews
import com.google.firebase.database.FirebaseDatabase


/**
 * Implementation of App Widget functionality.
 */
class NewAppWidget : AppWidgetProvider() {
    val ACTION_UID = "com.example.senior.UID"
    var uid=""
    var number=""
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId, number)
        }
    }

    override fun onEnabled(context: Context) {
        // Enter relevant functionality for when the first widget is created
    }

    override fun onDisabled(context: Context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        super.onReceive(context, intent)
        if (intent != null) {
            number=intent.getStringExtra("number")
        }
    }
}

internal fun updateAppWidget(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetId: Int,
    numberCare: String
) {
    val callIntent = Intent(Intent.ACTION_CALL)
    Log.d("intent", numberCare)
    callIntent.data = Uri.parse("tel:$numberCare")
    val pendingIntent = PendingIntent.getActivity(context, 0, callIntent, 0)
    val widgetText = context.getString(R.string.appwidget_text)
    val views = RemoteViews(context.packageName, R.layout.new_app_widget)
    views.setOnClickPendingIntent(R.id.appwidget_button,pendingIntent)
    appWidgetManager.updateAppWidget(appWidgetId, views)
}