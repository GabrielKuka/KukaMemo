package com.katana.memo.memo.Widgets

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.desai.vatsal.mydynamictoast.MyDynamicToast
import com.katana.memo.memo.Activities.AddWidget
import com.katana.memo.memo.Helper.DatabaseHelper
import com.katana.memo.memo.R


class SimpleMemoWidget : AppWidgetProvider() {

    companion object {
        val homepage = "Homepage"
    }

    override fun onDeleted(context: Context?, appWidgetIds: IntArray) {
        super.onDeleted(context, appWidgetIds)
        val dbHelper: DatabaseHelper = DatabaseHelper(context)

        (0..appWidgetIds.size - 1)
                .map { appWidgetIds[it] }
                .forEach { dbHelper.removeKeyWidget(it) }

    }

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager?, appWidgetIds: IntArray) {

        for (i in 0..appWidgetIds.size - 1) {

            val currentWidgetId = appWidgetIds[i]

            MyDynamicToast.informationMessage(context, "" + currentWidgetId)

            val remoteViews: RemoteViews = RemoteViews(context.packageName, R.layout.widget_memo)

            val intent = Intent(context, AddWidget::class.java)
            intent.putExtra("widgetId", currentWidgetId)

            val pendingIntent = PendingIntent.getActivity(context, currentWidgetId, intent,
                    PendingIntent.FLAG_CANCEL_CURRENT)

            remoteViews.setOnClickPendingIntent(R.id.addMemoWidgetButton, pendingIntent)


            appWidgetManager?.updateAppWidget(currentWidgetId, remoteViews)

        }

        super.onUpdate(context, appWidgetManager, appWidgetIds)
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        super.onReceive(context, intent)

        if (intent?.action == homepage) {

            val i = Intent(context, AddWidget::class.java)

            context?.startActivity(i)
        }

    }

    fun getPendingSelfIntent(context: Context, action: String, widgetId: Int): PendingIntent {
        val intent = Intent(context, SimpleMemoWidget::class.java)
        intent.action = action
        intent.putExtra("widgetId", widgetId)
        return PendingIntent.getBroadcast(context, 0, intent, 0)
    }

}