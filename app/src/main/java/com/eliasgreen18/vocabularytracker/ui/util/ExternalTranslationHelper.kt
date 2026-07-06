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
            setType("text/plain")
            putExtra(Intent.EXTRA_TEXT, text)
            // Force the Google Translate app
            setPackage("com.google.android.apps.translate")
        }

        // Alternative: Browser URL if app is not installed
        val browserUri = Uri.parse("https://translate.google.com/?sl=$sourceLang&tl=$targetLang&text=${Uri.encode(text)}&op=translate")
        val browserIntent = Intent(Intent.ACTION_VIEW, browserUri)

        try {
            context.startActivity(sendIntent)
        } catch (e: Exception) {
            // If app is not installed, open in browser
            try {
                context.startActivity(browserIntent)
            } catch (e2: Exception) {
                // Ignore
            }
        }
    }

    /**
     * Attempts to open Reverso Context app. 
     * Uses a generic VIEW intent but forces the Reverso package to avoid the browser.
     */
    fun openReversoContext(context: Context, text: String, sourceLang: String = "en", targetLang: String = "es") {
        // Reverso app usually registers for these types of URLs
        val uri = Uri.parse("https://context.reverso.net/translation/$sourceLang-$targetLang/${Uri.encode(text)}")
        val appIntent = Intent(Intent.ACTION_VIEW, uri).apply {
            // Force the Reverso app specifically
            setPackage("com.softissimo.reverso.context")
        }

        try {
            context.startActivity(appIntent)
        } catch (e: Exception) {
            // If Reverso app is not installed, fallback to browser (without package)
            val browserIntent = Intent(Intent.ACTION_VIEW, uri)
            try {
                context.startActivity(browserIntent)
            } catch (e2: Exception) {
                // Ignore
            }
        }
    }
}
