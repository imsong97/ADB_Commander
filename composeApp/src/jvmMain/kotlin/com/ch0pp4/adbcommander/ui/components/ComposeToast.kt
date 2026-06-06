package com.ch0pp4.adbcommander.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

class ToastState {
    var message by mutableStateOf<String?>(null)
        private set

    fun show(msg: String) { message = msg }
    fun dismiss() { message = null }
}

@Composable
fun rememberToastState() = remember { ToastState() }

@Composable
fun ToastHost(
    state: ToastState,
    modifier: Modifier = Modifier,
    durationMs: Long = 1000L,
    content: @Composable () -> Unit,
) {
    LaunchedEffect(state.message) {
        if (state.message != null) {
            delay(durationMs.milliseconds)
            state.dismiss()
        }
    }

    var lastMessage by remember { mutableStateOf("") }
    if (state.message != null) lastMessage = state.message!!

    Box(modifier = modifier) {
        content()
        AnimatedVisibility(
            visible = state.message != null,
            modifier = Modifier.align(Alignment.BottomCenter),
            enter = fadeIn(),
            exit = fadeOut(),
        ) {
            Surface(
                modifier = Modifier.padding(bottom = 24.dp),
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.inverseSurface,
                shadowElevation = 4.dp,
            ) {
                Text(
                    text = lastMessage,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.inverseOnSurface,
                )
            }
        }
    }
}
