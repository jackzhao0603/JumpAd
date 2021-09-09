package com.jackzhao.adjump

import android.accessibilityservice.AccessibilityService
import android.app.Activity
import android.content.Context
import com.jackzhao.adjump.accessibilityhandler.impl.OpenScreenAdHandler
import com.jackzhao.adjump.config.Config
import com.jackzhao.adjump.service.JumpAdService
import com.jackzhao.appmanager.PermissionManager

object AdJumpManager {
    private const val TAG = "AdJumpManager"

    fun init(accessibilityService: AccessibilityService) {
        JumpAdService.addAccessibilityHandler(OpenScreenAdHandler(accessibilityService))
    }

    fun isAdJumpPermissionGranted(context: Context): Boolean {
        val result = PermissionManager.checkSelfAccessbility(context)
        Config.IS_JUMP_ENABLE[context] = result and (Config.IS_JUMP_ENABLE[context] == true)
        return result
    }

    private fun gotoAccessiblityConfig(activity: Activity) {
        PermissionManager.gotoAccessibilitySettings(activity)
    }

    fun enableJump(activity: Activity, isEnable: Boolean): Boolean {
        if (isEnable && !isAdJumpPermissionGranted(activity)) {
            gotoAccessiblityConfig(activity)
        } else {
            Config.IS_JUMP_ENABLE[activity] = isEnable
        }
        return Config.IS_JUMP_ENABLE.getBoolean(activity)
    }

    fun isEnable(activity: Activity): Boolean {
        if (!isAdJumpPermissionGranted(activity)) {
            return false
        }
        return Config.IS_JUMP_ENABLE.getBoolean(activity)
    }

    fun gotoBatteryConfig(activity: Activity) {
        PermissionManager.gotoBatteryOptimization(activity)
    }
}