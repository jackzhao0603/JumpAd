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
    private var luncherAppPkg = AppManager.getLauncherPackageName(service)

    private val jumpStrs = service.resources.getStringArray(R.array.jump_key_words)
    private val ignorePkgs = service.resources.getStringArray(R.array.ignore_pkgs)
    private var jumpRect: Rect? = null
    private val quene = LinkedList<AccessibilityNodeInfo>()

    private var lastApp = ""
    private var lastActivity = ""
    private var nowActivity = ""
    private var nowApp = ""
    private val context = service.baseContext

    override fun needToHandleEvent(event: AccessibilityEvent): Boolean {
        var result = false
        if (lastApp == luncherAppPkg) {
            result = true
        }

        if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            if (event.packageName.contains(".")) {
                nowActivity = getActivityName(event) ?: nowActivity
                if (nowActivity != lastActivity) {
                    lastApp = nowApp
                    nowApp = event.packageName as String
                    lastActivity = nowActivity
                }
            }
        }
        if (jumpRect != null) {
            return false
        }
        if (event.isScrollable) {
            return false
        }
        if (AppManager.isSystemApp(context, nowApp)) {
            return false
        }
        if (ignorePkgs.contains(nowApp)) {
            return false
        }
        return result
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        val rootNodeInfo = event.source
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
//                if (root.isClickable && "android.view.View" == root.className) {
//                    var rect = Rect()
//                    root.getBoundsInScreen(rect)
//                    val width = rect.right - rect.left
//                    Log.e(TAG, "extractJumpForN: $rect --> $width")
//                    if (width < screenWidth / 20) {
//                        if (screenWidth - rect.right < screenWidth / 16) {
//                            clickNode(root)
//                        }
//                        if (rect.left < screenWidth / 16) {
//                            clickNode(root)
//                        }
//                    }
//                }
                if (root.isEnabled && root.isVisibleToUser && !TextUtils.isEmpty(root.text)) {
                    val str = root.text.toString()
                    for (jumpStr in jumpStrs) {
                        val tmp = str.replace(" ", "")
                        if (tmp.length <= 5) {
                            if (tmp.startsWith(jumpStr) || tmp.endsWith(jumpStr)) {
                                if (root.isClickable) {
                                    clickNode(root)
                                } else {
                                    jumpRect = Rect()
                                    root.getBoundsInScreen(jumpRect)
                                }
                                Log.e(TAG, "extractJumpForN: $str")
                                quene.clear()
                                return
                            }
                        }
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
                        if (child.isEnabled && child.isVisibleToUser && !TextUtils.isEmpty(child.text)) {
                            val str = child.text.toString()
                            for (jumpStr in jumpStrs) {
                                if (str.trim().startsWith(jumpStr) ||
                                    str.trim().endsWith(jumpStr)
                                ) {
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