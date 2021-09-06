package com.jackzhao.adjump.accessibilityhandler

import android.graphics.Rect
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

abstract class AccessibilityHandler {
    private val TAG = "AccessibilityHandler"
    var screenHeight = 0
    var screenWidth = 0

    abstract fun needToHandleEvent(event: AccessibilityEvent): Boolean
    abstract fun onAccessibilityEvent(event: AccessibilityEvent)

    fun getActivityName(event: AccessibilityEvent): String? {
        var name = event.className as String
        var packages = event.packageName as String
        if (name.contains("com.")) {
            if (name.contains(packages)) {
                name = name.replace(packages, "")
                name = name.replace("..", "");
                if (name[0] == '.')
                    name = name.substring(1);
                if (screenHeight == 0) {
                    val rect = Rect()
                    event.source.getBoundsInScreen(rect)
                    screenHeight = rect.height()
                    screenWidth = rect.width()
                }
                return name
            }
        }
        return null
    }

    fun clickNode(node: AccessibilityNodeInfo) {
        node.performAction(AccessibilityNodeInfo.ACTION_CLICK)
    }

}