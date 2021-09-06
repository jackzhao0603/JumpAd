package com.jackzhao.adjump.accessibilityhandler

import android.graphics.Rect
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import java.lang.Exception

abstract class AccessibilityHandler {
    data class Point(var x: Int, var y: Int)

    private val TAG = "AccessibilityHandler"
    var screenHeight = 0
    var screenWidth = 0

    abstract fun needToHandleEvent(event: AccessibilityEvent): Boolean
    abstract fun onAccessibilityEvent(event: AccessibilityEvent)

    fun getActivityName(event: AccessibilityEvent): String? {
        var tmp: CharSequence? = event.className ?: return null
        var name = tmp as String

        var packages = event.packageName as String
        if (name.contains("com.")) {
            if (name.contains(packages)) {
                name = name.replace(packages, "")
                name = name.replace("..", "");
                if (name[0] == '.')
                    name = name.substring(1);
                if (screenHeight == 0 && event.source != null) {
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
        node.performAction(AccessibilityNodeInfo.ACTION_FOCUS);
        node.performAction(AccessibilityNodeInfo.ACTION_CLICK)
    }

    fun tryToClickPoint(rootNodeInfo: AccessibilityNodeInfo, point: Point) {
        for (i in 0 until rootNodeInfo.childCount) {
            try {
                val child = rootNodeInfo.getChild(i) ?: continue
                if (child.isClickable) {
                    var rect = Rect()
                    child.getBoundsInScreen(rect)
//                    if (
//                        screenHeight - rect.bottom < 300 &&
//                        rect.right - rect.left > 0 &&
//                        rect.right - rect.left < screenWidth / 6
//                    ) {
//                        Log.e(TAG, "tryToClickPoint: $rect")
//                        clickNode(child)
//                    } else {
//                        tryToClickPoint(child, point)
//                    }
                    Log.e(TAG, "tryToClickPoint: " + child.text)
                    tryToClickPoint(child, point)
                } else {
                    tryToClickPoint(child, point)
                }
            } catch (e: Exception) {
                Log.w(TAG, "tryToClickPoint: ", e)
            }
        }
    }

}