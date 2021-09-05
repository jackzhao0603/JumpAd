package com.jackzhao.adjump

import android.app.Activity
import android.content.Context
import com.jackzhao.appmanager.PermissionManager

object AdJumpManager {
    private const val TAG = "AdJumpManager"
    var isEnable = false
    fun isAdJumpEnable(context: Context): Boolean {
        isEnable = PermissionManager.checkSelfAccessbility(context)
        return isEnable
    }

    fun gotoAccessiblityConfig(activity: Activity) {
        PermissionManager.gotoAccessibilitySettings(activity)
    }
}