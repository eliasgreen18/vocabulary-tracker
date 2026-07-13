package com.eliasgreen18.vocabularytracker.util

import android.util.Log

/**
 * Vocabulary Tracker Centralized Logger
 */
object VTLog {
    private const val GLOBAL_TAG = "VT_APP"
    private const val DEBUG = true // Switch manually for now if BuildConfig is missing

    fun d(tag: String, message: String) {
        if (DEBUG) {
            Log.d(GLOBAL_TAG, "[$tag] $message")
        }
    }

    fun e(tag: String, message: String, throwable: Throwable? = null) {
        Log.e(GLOBAL_TAG, "[$tag] ERROR: $message", throwable)
    }

    fun i(tag: String, message: String) {
        Log.i(GLOBAL_TAG, "[$tag] INFO: $message")
    }
}
