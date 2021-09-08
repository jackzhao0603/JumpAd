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

abstract class AccessibilityHandler(service: AccessibilityService) {
    data class Point(var x: Int, var y: Int)

    private val accessibilityService = service
    private val TAG = "AccessibilityHandler"
    var screenHeight = 0
    var screenWidth = 0

    abstract fun needToHandleEvent(event: AccessibilityEvent): Boolean
    abstract fun onAccessibilityEvent(event: AccessibilityEvent)

    fun getActivityName(event: AccessibilityEvent): String? {
        var tmp: CharSequence? = event.className ?: return null
        var name = tmp as String

        if (!name.startsWith("android.")) {
            if (screenHeight == 0 && event.source != null) {
                val rect = Rect()
                event.source.getBoundsInScreen(rect)
                screenHeight = rect.height()
                screenWidth = rect.width()
            }
            return name
        }
        return null
    }

    fun clickNode(node: AccessibilityNodeInfo) {
        node.performAction(AccessibilityNodeInfo.ACTION_FOCUS);
        node.performAction(AccessibilityNodeInfo.ACTION_CLICK)
    }

    fun tryToClickPoint(point: Point) {
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

    fun tryToClickChild(rootNodeInfo: AccessibilityNodeInfo) {
        var child: AccessibilityNodeInfo? = null
        for (i in 0 until rootNodeInfo.childCount) {
            try {
                child = rootNodeInfo.getChild(i) ?: continue
                if (child.isClickable) {
                    clickNode(child)
                }
                tryToClickChild(child)
            } catch (e: java.lang.Exception) {
                Log.w(TAG, "tryToClickChild: ", e)
            } finally {
                tryRecycle(child)
            }
        }
    }

    fun tryToClickParent(rootNodeInfo: AccessibilityNodeInfo) {
        var node = rootNodeInfo.parent
        while (!node.isClickable) {
            node = node.parent
            if (node == null) {
                return
            }
        }
        if (node.isClickable) {
            clickNode(node)
        }
    }

    fun tryRecycle(info: AccessibilityNodeInfo?) {
        try {
            info?.recycle()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}