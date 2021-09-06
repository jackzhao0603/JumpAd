package com.jackzhao.adjump.accessibilityhandler.impl

import android.accessibilityservice.AccessibilityService
import android.graphics.Rect
import android.text.TextUtils
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.jackzhao.adjump.R
import com.jackzhao.adjump.accessibilityhandler.AccessibilityHandler
import com.jackzhao.appmanager.AppManager
import com.jackzhao.appmanager.utils.VersionUtils
import java.util.*

class OpenScreenAdHandler(service: AccessibilityService) : AccessibilityHandler(service) {


    private val TAG = "OpenScreenAdHandler"
    private var type = 0
    private var luncherAppPkg = ""
    private var targetActivity = ""
    private val jumpStr = service.getString(R.string.jump)
    private var jumpRect: Rect? = null
    private val quene = LinkedList<AccessibilityNodeInfo>()
    private val keyList = listOf(
        "splash",
        "loading",
        "login"
    )

    init {
        luncherAppPkg = AppManager.getLauncherPackageName(service)
    }

    override fun needToHandleEvent(event: AccessibilityEvent): Boolean {
        if (jumpRect != null) {
            return false
        }
        targetActivity = getActivityName(event) ?: targetActivity
        if (event.isScrollable) {
            return false
        }
        if (event.packageName.equals(luncherAppPkg)) {
            return false
        }
        return true
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        val rootNodeInfo = event.source
        type = event.eventType
        rootNodeInfo?.let {
            if (VersionUtils.isAndroidN()) {
                extractJumpForN(rootNodeInfo)
                jumpRect?.let {
                    val point = Point(
                        (it.left + it.right) / 2,
                        (it.top + it.bottom) / 2
                    )
                    tryToClickPoint(rootNodeInfo, point)
                    android.os.Handler().postDelayed({ jumpRect = null }, 1000)
                }
            } else {
                extractJump(rootNodeInfo)
            }

        }
    }

    private fun extractJumpForN(rootNodeInfo: AccessibilityNodeInfo) {
        quene.offer(rootNodeInfo)
        while (!quene.isEmpty()) {
            var root: AccessibilityNodeInfo? = null
            try {
                val root = quene.poll() ?: continue
                for (i in 0 until root.childCount) {
                    quene.offer(root.getChild(i))
                }
                if (root.isClickable && "android.view.View" == root.className) {
                    for (key in keyList) {
                        if (targetActivity.lowercase().contains(key)) {
                            var rect = Rect()
                            root.getBoundsInScreen(rect)
                            val width = rect.right - rect.left
                            if (width < screenWidth / 8) {
                                if (screenWidth - rect.right < screenWidth / 16) {
                                    clickNode(root)
                                }
                                if (rect.left < screenWidth / 16) {
                                    clickNode(root)
                                }
                            }
                        }
                    }
                }
                if (root.isEnabled && root.isVisibleToUser && !TextUtils.isEmpty(root.text)) {
                    val str = root.text.toString()
                    if (str.contains(jumpStr)) {
                        jumpRect = Rect()
                        root.getBoundsInScreen(jumpRect)
                        return
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "extractJumpForN: ", e)
            } finally {
                tryRecycle(root)
            }
        }
    }

    private fun extractJump(rootNodeInfo: AccessibilityNodeInfo) {
        try {
            if (rootNodeInfo != null) {
                for (i in 0 until rootNodeInfo.childCount) {
                    var child: AccessibilityNodeInfo? = null
                    try {
                        child = rootNodeInfo.getChild(i) ?: continue
                        var rect = Rect()
                        if (child.isClickable && "android.view.View" == child.className) {
                            for (key in keyList) {
                                if (targetActivity.lowercase().contains(key)) {
                                    child.getBoundsInScreen(rect)
                                    val width = rect.right - rect.left
                                    if (width < screenWidth / 8) {
                                        if (screenWidth - rect.right < screenWidth / 16) {
                                            clickNode(child)
                                        }
                                        if (rect.left < screenWidth / 16) {
                                            clickNode(child)
                                        }
                                    }
                                }
                            }
                        }
                        if (child.isEnabled && child.isVisibleToUser && !TextUtils.isEmpty(child.text)) {
                            val str = child.text.toString()
                            if (str.contains(jumpStr)) {
                                child.getBoundsInScreen(rect)
                                if (child.isClickable) {
                                    clickNode(child)
                                } else {
                                    try {
                                        tryToClickParent(child)
                                    } catch (e: Exception) {
                                        Log.w(TAG, "extractString: ", e)
                                    }

                                    try {
                                        tryToClickChild(child)
                                    } catch (e: Exception) {
                                        Log.w(TAG, "extractString: ", e)
                                    }
                                    jumpRect = Rect()
                                    child.getBoundsInScreen(jumpRect)
                                }
                            }
                        }
                        extractJump(child)
                    } catch (e: Exception) {
                    } finally {
                        tryRecycle(child)
                    }
                }
            }
        } catch (e: Exception) {
        }
    }
}