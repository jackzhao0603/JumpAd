package com.jackzhao.jumpad

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Switch
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
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
            isAdJumpEnable = remember { mutableStateOf(AdJumpManager.isAdJumpEnable(this)) }
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

    override fun onResume() {
        super.onResume()
        isAdJumpEnable?.value = AdJumpManager.isAdJumpEnable(this)
    }
}

@Composable
fun Greeting(activity: MainActivity, isAdJumpEnable: MutableState<Boolean>) {
    Switch(checked = isAdJumpEnable.value, onCheckedChange = {
        AdJumpManager.gotoAccessiblityConfig(activity)
        isAdJumpEnable.value = AdJumpManager.isAdJumpEnable(activity)
        GlobalScope.launch(Dispatchers.IO) {
            for (i in 1..100) {
                isAdJumpEnable.value = AdJumpManager.isAdJumpEnable(activity)
                if (isAdJumpEnable.value) {
                    activity.startActivity(Intent(activity, MainActivity::class.java))
                } else {
                    delay(100)
                }
            }
        }
    })
}