package com.jackzhao.adjump.service

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent

class JumpAdService: AccessibilityService() {
    override fun onAccessibilityEvent(p0: AccessibilityEvent?) {
    }

    override fun onInterrupt() {
    }

    override fun onServiceConnected() {
        super.onServiceConnected()

    }
}