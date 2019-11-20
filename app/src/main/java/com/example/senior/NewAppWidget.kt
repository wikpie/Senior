package com.example.senior

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.content.Intent.ACTION_SENDTO
import android.net.Uri
import android.widget.RemoteViews
import androidx.core.content.ContextCompat.startActivity


/**
 * Implementation of App Widget functionality.
 */
class NewAppWidget : AppWidgetProvider() {
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
private fun getPendingIntent(context: Context, value: Int): PendingIntent {
    //1

    //2
    val callIntent = Intent(Intent.ACTION_CALL)
    callIntent.data = Uri.parse("tel:786280342")

    return PendingIntent.getActivity(context, value, callIntent, 0)
}

internal fun updateAppWidget(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetId: Int
) {
    val widgetText = context.getString(R.string.appwidget_text)
    // Construct the RemoteViews object
    val views = RemoteViews(context.packageName, R.layout.new_app_widget)
    //views.setTextViewText(R.id.appwidget_text, widgetText)

    views.setOnClickPendingIntent(R.id.appwidget_button,getPendingIntent(context,0))

    // Instruct the widget manager to update the widget
    appWidgetManager.updateAppWidget(appWidgetId, views)
}