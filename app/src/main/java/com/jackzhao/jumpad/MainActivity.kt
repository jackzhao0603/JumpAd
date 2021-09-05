package com.jackzhao.jumpad

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Switch
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.jackzhao.adjump.AdJumpManager
import com.jackzhao.jumpad.ui.theme.JumpAdTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class MainActivity : ComponentActivity() {
    private var isAdJumpEnable: MutableState<Boolean>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            isAdJumpEnable = remember { mutableStateOf(AdJumpManager.isEnable(this)) }
            JumpAdTheme {
                // A surface container using the 'background' color from the theme
                Surface(color = MaterialTheme.colors.background) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Greeting(this@MainActivity, isAdJumpEnable!!)
                    }
                }
            }
        }

    }

}

@Composable
fun Greeting(activity: MainActivity, isAdJumpEnable: MutableState<Boolean>) {
    Switch(checked = isAdJumpEnable.value, onCheckedChange = {
        isAdJumpEnable.value = AdJumpManager.enableJump(activity, !isAdJumpEnable.value)
        GlobalScope.launch(Dispatchers.IO) {
            for (i in 1..100) {
                if (AdJumpManager.isAdJumpPermissionGranted(activity)) {
                    activity.startActivity(Intent(activity, MainActivity::class.java))
                    isAdJumpEnable.value = AdJumpManager.enableJump(activity, !isAdJumpEnable.value)
                } else {
                    delay(100)
                }
            }
        }
    })
}