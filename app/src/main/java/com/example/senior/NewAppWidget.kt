package com.example.senior

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.RemoteViews
import com.google.firebase.database.FirebaseDatabase


/**
 * Implementation of App Widget functionality.
 */
class NewAppWidget : AppWidgetProvider() {
    val ACTION_UID = "com.example.senior.UID"
    var uid=""
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        // There may be multiple widgets active, so update all of them
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onEnabled(context: Context) {
        // Enter relevant functionality for when the first widget is created
    }

    override fun onDisabled(context: Context) {
        // Enter relevant functionality for when the last widget is disabled
    }

}
//private fun getPendingIntent(context: Context, value: Int): PendingIntent {
    //1

    //2
    //val senior=SeniorService()
    //val uid=senior.uid
    //val ref= FirebaseDatabase.getInstance().getReference("/seniors/$uid")


   // return PendingIntent.getActivity(context, value, callIntent, 0)
//}

internal fun updateAppWidget(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetId: Int
) {
    val callIntent = Intent(Intent.ACTION_CALL)
    callIntent.data = Uri.parse("tel:513134497")
    val pendingIntent = PendingIntent.getActivity(context, 0, callIntent, 0)
    val widgetText = context.getString(R.string.appwidget_text)
    // Construct the RemoteViews object
    val views = RemoteViews(context.packageName, R.layout.new_app_widget)
    //views.setTextViewText(R.id.appwidget_text, widgetText)

    views.setOnClickPendingIntent(R.id.appwidget_button,pendingIntent)

    // Instruct the widget manager to update the widget
    appWidgetManager.updateAppWidget(appWidgetId, views)
}