package org.b4cklog.mobile.util

import android.content.Context
import androidx.core.content.edit

object AuthPrefs {
    private const val PREFS_NAME = "auth_prefs"
    private const val KEY_TOKEN = "token"

    fun saveToken(context: Context, token: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit {
            putString(KEY_TOKEN, token)
        }
    }

    fun getToken(context: Context): String? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_TOKEN, null)
    }

    fun clearToken(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit { remove(KEY_TOKEN) }
    }
}
