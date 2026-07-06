package com.eliasgreen18.vocabularytracker.ui.util

import android.content.Context
import android.content.Intent
import android.net.Uri

object ExternalTranslationHelper {

    /**
     * Attempts to open Google Translate app with text pre-filled.
     * Uses ACTION_SEND which is the most reliable way to fill text in the official app.
     */
    fun openGoogleTranslate(context: Context, text: String, sourceLang: String = "en", targetLang: String = "es") {
        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
            setPackage("com.google.android.apps.translate")
        }

        // Alternative: Browser URL if app is not installed
        val browserUri = Uri.parse("https://translate.google.com/?sl=$sourceLang&tl=$targetLang&text=${Uri.encode(text)}&op=translate")
        val browserIntent = Intent(Intent.ACTION_VIEW, browserUri)

        try {
            context.startActivity(sendIntent)
        } catch (e: Exception) {
            try {
                context.startActivity(browserIntent)
            } catch (e2: Exception) {
                // Ignore
            }
        }
    }

    /**
     * Attempts to open Reverso Context app with text pre-filled.
     * Uses ACTION_SEND targeted at Reverso package to ensure auto-fill.
     */
    fun openReversoContext(context: Context, text: String, sourceLang: String = "en", targetLang: String = "es") {
        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
            // Force the Reverso app package
            setPackage("com.softissimo.reverso.context")
        }

        // Fallback: Browser URL if app is not installed
        val browserUri = Uri.parse("https://context.reverso.net/translation/$sourceLang-$targetLang/${Uri.encode(text)}")
        val browserIntent = Intent(Intent.ACTION_VIEW, browserUri)

        try {
            context.startActivity(sendIntent)
        } catch (e: Exception) {
            try {
                context.startActivity(browserIntent)
            } catch (e2: Exception) {
                // Ignore
            }
        }
    }
}
