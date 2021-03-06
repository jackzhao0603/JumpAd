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
    private val needWaitPkgs = service.resources.getStringArray(R.array.need_wait_app)
    private val specialActivities = service.resources.getStringArray(R.array.special_activities)

    private var jumpRect: Rect? = null
    private val quene = LinkedList<AccessibilityNodeInfo>()

    private var lastApp = ""
    private var lastActivity = ""
    private var nowActivity = ""
    private var nowApp = ""
    private var nowActivityShowTime = 0L
    private var lastJumpTime = 0L
    private val context = service.baseContext

    private var nowPageHash = 0
    private var jumpPageHash = 0

    override fun needToHandleEvent(event: AccessibilityEvent): Boolean {
        if (event.source == null) {
            return false
        }
        var rect = Rect()
        event.source?.getBoundsInScreen(rect)
        if (rect.top == 0 && rect.left == 0) {
            nowPageHash = event.source.hashCode()
        }


        val nowTime = System.currentTimeMillis()
        var result = false
        if (lastApp.isEmpty()) {
            lastApp = event.packageName.toString()
        }
        if (lastApp == luncherAppPkg || nowActivity.lowercase().contains("splash")) {
            result = true
        }
        if (event.packageName.contains(".")) {
            val tmpActivity = getActivityName(event) ?: nowActivity
            if (nowActivity != tmpActivity) {
                lastActivity = nowActivity
                nowActivity = tmpActivity
                lastApp = nowApp
                nowApp = event.packageName as String
                nowActivityShowTime = nowTime
                Log.d(TAG, "needToHandleEvent: $lastApp/$lastActivity  --> $nowApp/$nowActivity")
            }
        }
        if (lastApp == luncherAppPkg || nowActivity.lowercase().contains("splash")) {
            result = true
        }
        if (jumpRect != null) {
            result = false
        }
        if (event.isScrollable) {
            result = false
        }
        if (AppManager.isSystemApp(context, nowApp) &&
            !AppManager.getMarketApps(context).contains(nowApp)
        ) {
            result = false
        }
        if (AppManager.isSystemApp(context, event.packageName.toString())) {
            result = false
        }
        if (ignorePkgs.contains(nowApp)) {
            result = false
        }
        if (nowTime - nowActivityShowTime > 10 * 1000) {
            result = false
        }
        if (nowTime - lastJumpTime < 5 * 1000) {
            result = false
        }

        if (jumpPageHash == nowPageHash &&
            needWaitPkgs.contains(nowApp)
        ) {
            result = true
        }
        if (specialActivities.contains(nowActivity)) {
            return true
        }
        return result
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        val rootNodeInfo = event.source
        rootNodeInfo?.let {
            if (VersionUtils.isAndroidN()) {
                quene.clear()
                extractJumpForN(rootNodeInfo)
                jumpRect?.let {
                    val point = Point(
                        (it.left + it.right) / 2,
                        (it.top + it.bottom) / 2
                    )
                    jumpRect = null
                    lastJumpTime = System.currentTimeMillis()
                    if (jumpPageHash == nowPageHash) {
                        tryToClickPoint(point)
                    }
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
                if (root.isEnabled &&
                    root.isVisibleToUser &&
                    !root.className.toString().lowercase().contains("switch") &&
                    !TextUtils.isEmpty(root.text)
                ) {
                    val str = root.text.toString()
                    for (jumpStr in jumpStrs) {
                        val tmp = str.replace(" ", "")
                        if (tmp.length <= 5) {
                            if (tmp.startsWith(jumpStr) || tmp.endsWith(jumpStr)) {
                                clickNodeByLocale(root)
                                return
                            }
                        }
                    }
                }
                if (root.isClickable && root.viewIdResourceName.contains("skip")) {
                    if (root.viewIdResourceName.contains("ad") ||
                        root.viewIdResourceName.contains("splash")
                    ) {
                        clickNodeByLocale(root)
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

    private fun clickNodeByLocale(node: AccessibilityNodeInfo) {
        Log.e(TAG, "clickNodeByLocale: $node")
        jumpRect = Rect()
        node.getBoundsInScreen(jumpRect)
        jumpPageHash = nowPageHash
        lastJumpTime = System.currentTimeMillis()
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
                            if (width < screenWidth / 10) {
                                if (screenWidth - rect.right < screenWidth / 16) {
                                    clickNode(child)
                                }
                                if (rect.left < screenWidth / 16) {
                                    clickNode(child)
                                }
                            }
                        }
                        if (child.isEnabled &&
                            child.isVisibleToUser &&
                            !child.className.toString().lowercase().contains("switch") &&
                            !TextUtils.isEmpty(child.text)
                        ) {
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