package com.jackzhao.adjump.utils

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.Intent

object Utils {
    fun getLunchActivityName(context: Context, packageName: String?): String? {
        var pi: PackageInfo? = null
        pi = try {
            context.applicationContext.packageManager.getPackageInfo(packageName!!, 0)
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
            return null
        }
        val resolveIntent = Intent(Intent.ACTION_MAIN, null)
        resolveIntent.addCategory(Intent.CATEGORY_LAUNCHER)
        resolveIntent.setPackage(pi!!.packageName)
        val pManager = context.applicationContext.packageManager
        val apps = pManager.queryIntentActivities(
            resolveIntent,
            0
        )
        try {
            val ri = apps.iterator().next()
            if (ri != null) {
                val startappName = ri.activityInfo.packageName
                return ri.activityInfo.name
            }
        } catch (e: Exception) {
            return null
        }

        return null
    }
}