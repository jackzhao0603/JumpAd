package com.jackzhao.adjump.accessibilityhandler

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.graphics.Rect
import android.os.Build
import android.os.Handler
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import androidx.annotation.RequiresApi
import java.lang.Exception

abstract class AccessibilityHandler(service: AccessibilityService) {
    data class Point(var x: Int, var y: Int)

    val accessibilityService = service
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            clickOnScreen(accessibilityService,
                point.x.toFloat(),
                point.y.toFloat(),
                object : AccessibilityService.GestureResultCallback() {

                }
            )
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun clickOnScreen(
        service: AccessibilityService,
        x: Float,
        y: Float,
        callback: AccessibilityService.GestureResultCallback,
        handler: Handler? = null
    ) {
        val p = Path()
        p.moveTo(x, y)
        gestureOnScreen(service, p, callback = callback, handler = handler)
    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun gestureOnScreen(
        service: AccessibilityService,
        path: Path,
        startTime: Long = 0,
        duration: Long = 100,
        callback: AccessibilityService.GestureResultCallback,
        handler: Handler? = null
    ) {
        val builder = GestureDescription.Builder()
        builder.addStroke(GestureDescription.StrokeDescription(path, startTime, duration))
        val gesture = builder.build()
        service.dispatchGesture(gesture, callback, handler)
    }

}