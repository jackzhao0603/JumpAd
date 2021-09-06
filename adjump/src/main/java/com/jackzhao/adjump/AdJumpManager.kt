package com.jackzhao.adjump

import android.app.Activity
import android.content.Context
import android.util.Log
import com.jackzhao.adjump.accessibilityhandler.impl.OpenScreenAdHandler
import com.jackzhao.adjump.service.JumpAdService
import com.jackzhao.appmanager.AppManager
import com.jackzhao.appmanager.PermissionManager

object AdJumpManager {
    private const val TAG = "AdJumpManager"
    var mIsEnable = false


    fun init(context: Context) {
        JumpAdService.addAccessibilityHandler(OpenScreenAdHandler(context))
    }

    fun isAdJumpPermissionGranted(context: Context): Boolean {
        mIsEnable = PermissionManager.checkSelfAccessbility(context)
        return mIsEnable
    }

    fun gotoAccessiblityConfig(activity: Activity) {
        PermissionManager.gotoAccessibilitySettings(activity)
    }

    fun enableJump(activity: Activity, isEnable: Boolean): Boolean {
        if (isEnable && !isAdJumpPermissionGranted(activity)) {
            gotoAccessiblityConfig(activity)
        } else {
            mIsEnable = isEnable
        }
        return mIsEnable
    }

    fun isEnable(activity: Activity): Boolean {
        if (!isAdJumpPermissionGranted(activity)) {
            return false
        }
        return mIsEnable
    }

    fun gotoBatteryConfig(activity: Activity) {
        PermissionManager.gotoBatteryOptimization(activity)
    }
}