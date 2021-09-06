package com.jackzhao.jumpad

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.jackzhao.adjump.AdJumpManager

class StartReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        AdJumpManager.mIsEnable = true
    }
}