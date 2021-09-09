package com.jackzhao.adjump.utils

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager


object Utils {
    fun getMarketApp(context: Context): String? {
        val intent = Intent()
        intent.action = "android.intent.action.MAIN"
        intent.addCategory("android.intent.category.APP_MARKET")
        val pm: PackageManager = context.packageManager
        val infos = pm.queryIntentActivities(intent, 0)
        val size = infos.size
        for (i in 0 until size) {
            val activityInfo = infos[i].activityInfo
            val packageName = activityInfo.packageName
            return packageName
        }
        return null
    }


}