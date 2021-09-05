package com.jackzhao.adjump

import android.app.Activity
import android.content.Context
import android.provider.Settings
import android.text.TextUtils
import android.util.Log
import com.jackzhao.appmanager.PermissionManager

object AdJumpManager {
    private const val TAG = "AdJumpManager"
    fun isAdJumpEnable(context: Context): Boolean {
        val result = ArrayList<String>()
        val mStringColonSplitter = TextUtils.SimpleStringSplitter(':')
        val settingValue = Settings.Secure.getString(
            context.applicationContext.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        )
        if (settingValue != null) {
            mStringColonSplitter.setString(settingValue)
            while (mStringColonSplitter.hasNext()) {
                val accessabilityService = mStringColonSplitter.next()
                result.add(accessabilityService.split("/".toRegex()).toTypedArray()[0])
            }
        }
        return result.contains(context.packageName)
    }

    fun gotoAccessiblityConfig(activity: Activity) {
        PermissionManager.gotoAccessibilitySettings(activity)
    }
}