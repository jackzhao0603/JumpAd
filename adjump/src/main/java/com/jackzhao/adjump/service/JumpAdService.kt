package com.jackzhao.adjump.service

import android.accessibilityservice.AccessibilityService
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import com.jackzhao.adjump.AdJumpManager
import com.jackzhao.adjump.accessibilityhandler.AccessibilityHandler
import com.jackzhao.adjump.config.Config

class JumpAdService : AccessibilityService() {
    private val TAG = "JumpAdService"

    companion object {
        var jumpAdService: JumpAdService? = null
        private val accessibilityHandlers = HashSet<AccessibilityHandler>()
        var lastHandleTime = 0L

        fun isStart(): Boolean {
            return jumpAdService != null
        }

        fun addAccessibilityHandler(accessibilityInterface: AccessibilityHandler) {
            accessibilityHandlers.add(accessibilityInterface)
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (Config.IS_JUMP_ENABLE.getBoolean(baseContext) == false) {
            return
        }
        event?.let { event ->
            when (event.eventType) {
                AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED,
                AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED -> {
                    doHandleEvent(event)
                }
            }
        }
    }

    override fun onInterrupt() {
        jumpAdService = null
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        AdJumpManager.init(this)
        jumpAdService = this;
    }


    override fun onDestroy() {
        super.onDestroy()
        jumpAdService = null
    }


    fun doHandleEvent(event: AccessibilityEvent) {
        for (accessibilityHandler in accessibilityHandlers) {
            if (accessibilityHandler.needToHandleEvent(event)) {
                accessibilityHandler.onAccessibilityEvent(event)
            }
        }
    }

}