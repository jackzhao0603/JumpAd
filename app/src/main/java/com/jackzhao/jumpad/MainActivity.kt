package com.jackzhao.jumpad

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.jackzhao.adjump.AdJumpManager
import com.jackzhao.jumpad.ui.theme.JumpAdTheme
import com.jackzhao.jumpad.ui.theme.White
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
                Surface(color = White) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column() {
                            Greeting1(this@MainActivity, isAdJumpEnable!!)
                            Greeting2(this@MainActivity)
                        }
                    }
                }
            }
        }

    }

}

@Composable
fun Greeting1(activity: MainActivity, isAdJumpEnable: MutableState<Boolean>) {
    Switch(checked = isAdJumpEnable.value,
        modifier = Modifier.fillMaxWidth(),
        onCheckedChange = {
            if (!AdJumpManager.isAdJumpPermissionGranted(activity)) {
                GlobalScope.launch(Dispatchers.IO) {
                    for (i in 1..100) {
                        if (AdJumpManager.isAdJumpPermissionGranted(activity)) {
                            activity.startActivity(Intent(activity, MainActivity::class.java))
                            delay(200)
                            isAdJumpEnable.value =
                                AdJumpManager.enableJump(activity, true)
                            break
                        } else {
                            delay(100)
                        }
                    }
                }
            }
            isAdJumpEnable.value = AdJumpManager.enableJump(activity, !isAdJumpEnable.value)
        })
}

@Composable
fun Greeting2(activity: MainActivity) {
    Button(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp, 60.dp, 20.dp, 0.dp),
        onClick = {
            AdJumpManager.gotoBatteryConfig(activity)
        },
    ) {
        Text(stringResource(R.string.goto_battery), textAlign = TextAlign.Center)
    }
}