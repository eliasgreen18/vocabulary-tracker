package com.eliasgreen18.vocabularytracker.ui.widget

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.updateAll

class RefreshWordAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        // Just trigger a re-render of all widgets of this type
        // The provideGlance method will be called again and fetch a new random word
        WordWidget().updateAll(context)
    }
}
