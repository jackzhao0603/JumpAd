package com.jackzhao.adjump.accessibilityhandler.impl

import android.content.Context
import android.graphics.Rect
import android.text.TextUtils
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.jackzhao.adjump.R
import com.jackzhao.adjump.accessibilityhandler.AccessibilityHandler
import com.jackzhao.appmanager.AppManager

class OpenScreenAdHandler(context: Context) : AccessibilityHandler() {
    private val TAG = "OpenScreenAdHandler"
    private var type = 0
    private var luncherAppPkg = ""
    private var targetActivity = ""
    private val jumpStr = context.getString(R.string.jump)

    private val keyList = listOf(
        "main",
        "splash",
        "loading",
        "login"
    )

    init {
        luncherAppPkg = AppManager.getLauncherPackageName(context)
    }

    override fun needToHandleEvent(event: AccessibilityEvent): Boolean {
        if (event.isScrollable) {
            return false
        }
        if (event.packageName.equals(luncherAppPkg)) {
            return false
        }
        targetActivity = getActivityName(event) ?: targetActivity
        Log.e(TAG, "needToHandleEvent: $targetActivity")
        for (key in keyList) {
            if (targetActivity.lowercase().contains(key)) {
                return true
            }
        }
        return false
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        val rootNodeInfo = event.source
        type = event.eventType
        rootNodeInfo?.let {
            extractString(rootNodeInfo)
        }
    }


    private fun extractString(rootNodeInfo: AccessibilityNodeInfo) {
        try {
            if (rootNodeInfo != null) {
                for (i in 0 until rootNodeInfo.childCount) {
                    var child: AccessibilityNodeInfo? = null
                    try {
                        child = rootNodeInfo.getChild(i) ?: continue
                        if (child.isClickable && "android.view.View" == child.className) {
                            var rect = Rect()
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
                            if (str.contains(jumpStr)) {
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
                                }
                            }
                        }
                        extractString(child)
                    } catch (e: ArrayIndexOutOfBoundsException) {
                    } catch (e: IllegalStateException) {
                    } catch (e: Exception) {
                    } finally {
                        tryRecycle(child)
                    }
                }
            }
        } catch (e: Exception) {
        }
    }

    private fun tryToClickChild(rootNodeInfo: AccessibilityNodeInfo) {
        var child: AccessibilityNodeInfo? = null
        for (i in 0 until rootNodeInfo.childCount) {
            try {
                child = rootNodeInfo.getChild(i) ?: continue
                if (child.isClickable) {
                    clickNode(child)
                }
                tryToClickChild(child)
            } catch (e: ArrayIndexOutOfBoundsException) {
            } catch (e: IllegalStateException) {
            } catch (e: Exception) {
            } finally {
                tryRecycle(child)
            }
        }
    }

    private fun tryToClickParent(
        rootNodeInfo: AccessibilityNodeInfo
    ) {
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

    private fun tryRecycle(info: AccessibilityNodeInfo?) {
        try {
            info?.recycle()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}