package com.anonymous.bgtest

import android.app.AppOpsManager
import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod

class UsageStatsModule(reactContext: ReactApplicationContext) : ReactContextBaseJavaModule(reactContext) {

    override fun getName(): String {
        return "UsageStats"
    }

    @ReactMethod
    fun checkPermission(promise: Promise) {
        val appOps = this.reactApplicationContext.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, android.os.Process.myUid(), this.reactApplicationContext.packageName)
        val granted = mode == AppOpsManager.MODE_ALLOWED
        promise.resolve(granted)
    }

    @ReactMethod
    fun requestPermission() {
        val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        this.reactApplicationContext.startActivity(intent)
    }

    @ReactMethod
    fun getUsageStats(promise: Promise) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val usageStatsManager = this.reactApplicationContext.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
            val time = System.currentTimeMillis()
            val usageStatsList = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, time - 1000 * 3600 * 24, time)
            if (usageStatsList != null && usageStatsList.isNotEmpty()) {
                val result = Arguments.createArray()
                for (usageStats in usageStatsList) {
                    val map = Arguments.createMap()
                    map.putString("packageName", usageStats.packageName)
                    map.putDouble("totalTimeInForeground", usageStats.totalTimeInForeground.toDouble())
                    result.pushMap(map)
                }
                promise.resolve(result)
            } else {
                promise.resolve(null)
            }
        } else {
            promise.reject("Unsupported", "This feature is not supported on this Android version")
        }
    }
}
