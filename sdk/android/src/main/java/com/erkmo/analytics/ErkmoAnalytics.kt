package com.erkmo.analytics

import android.content.Context
import android.provider.Settings
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.util.UUID

object ErkmoAnalytics {
    private var siteId: String = ""
    private var apiUrl: String = "https://t.erkmo.com/track"
    private var sessionId: String = ""
    private const val sdkVersion = "android-0.1"

    fun init(context: Context, siteId: String, apiUrl: String = "https://t.erkmo.com/track") {
        this.siteId = siteId
        this.apiUrl = apiUrl
        this.sessionId = loadOrCreateSessionId(context)
    }

    fun track(context: Context, event: String, properties: Map<String, Any> = emptyMap()) {
        send(context, event, properties, null)
    }

    fun screen(context: Context, name: String, properties: Map<String, Any> = emptyMap()) {
        send(context, "screen_view", properties, name)
    }

    private fun send(context: Context, event: String, properties: Map<String, Any>, screenName: String?) {
        if (siteId.isBlank()) return
        val payload = JSONObject()
        payload.put("site_id", siteId)
        payload.put("event", event)
        payload.put("timestamp", System.currentTimeMillis())
        payload.put("session_id", sessionId)
        payload.put("platform", "android")
        payload.put("sdk_version", sdkVersion)
        payload.put("app_name", context.applicationInfo.loadLabel(context.packageManager).toString())
        payload.put("app_id", context.packageName)
        payload.put("app_version", context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "0.0.0")
        payload.put("app_build", context.packageManager.getPackageInfo(context.packageName, 0).longVersionCode.toString())
        payload.put("device_id", Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID) ?: UUID.randomUUID().toString())
        if (!screenName.isNullOrBlank()) payload.put("screen_name", screenName)

        val props = JSONObject()
        for ((k, v) in properties) {
            props.put(k, v)
        }
        payload.put("properties", props)

        Thread {
            try {
                val connection = (URL(apiUrl).openConnection() as HttpURLConnection).apply {
                    requestMethod = "POST"
                    setRequestProperty("Content-Type", "application/json")
                    doOutput = true
                }
                OutputStreamWriter(connection.outputStream).use { it.write(payload.toString()) }
                connection.inputStream.close()
                connection.disconnect()
            } catch (_: Exception) {
                // Ignore network errors to avoid crashing the host app
            }
        }.start()
    }

    private fun loadOrCreateSessionId(context: Context): String {
        val prefs = context.getSharedPreferences("erkmo_analytics", Context.MODE_PRIVATE)
        val existing = prefs.getString("session_id", null)
        if (!existing.isNullOrBlank()) return existing
        val newId = "session_${UUID.randomUUID()}"
        prefs.edit().putString("session_id", newId).apply()
        return newId
    }
}
