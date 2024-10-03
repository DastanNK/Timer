package com.example.timer

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.timer.Constants.ACTION_SERVICE_CANCEL
import com.example.timer.Constants.ACTION_SERVICE_START
import com.example.timer.Constants.ACTION_SERVICE_STOP

@ExperimentalAnimationApi
@Composable
fun MainScreen(stopwatchService: StopwatchService) {
    val context = LocalContext.current
    val hours by stopwatchService.hours
    val minutes by stopwatchService.minutes
    val seconds by stopwatchService.second
    val currentState by stopwatchService.currentState
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center, modifier = Modifier.fillMaxSize().padding(32.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth().weight(9f)
        ) {
            AnimatedContent(
                targetState = hours,
                transitionSpec = { Animation(800).using(sizeTransform = SizeTransform(clip = true)) }) { targetState ->
                Text(
                    text = targetState,
                    style = TextStyle(fontSize = MaterialTheme.typography.bodyLarge.fontSize),
                    textAlign = TextAlign.Center
                )
            }
            AnimatedContent(
                targetState = minutes,
                transitionSpec = { Animation(800).using(sizeTransform = SizeTransform(clip = true)) }) { targetState ->
                Text(
                    text = targetState,
                    style = TextStyle(fontSize = MaterialTheme.typography.bodyLarge.fontSize),
                    textAlign = TextAlign.Center
                )
            }
            AnimatedContent(
                targetState = seconds,
                transitionSpec = { Animation(800).using(sizeTransform = SizeTransform(clip = true)) }) { targetState ->
                Text(
                    text = targetState,
                    style = TextStyle(fontSize = MaterialTheme.typography.bodyLarge.fontSize),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
    Row {
        Button(modifier = Modifier.weight(1f).fillMaxHeight(0.8f),
            onClick = {
                ServiceHelper.triggerForegroundService(
                    context,
                    if (currentState == StopwatchState.Started) ACTION_SERVICE_STOP else ACTION_SERVICE_START
                )
            }) {
            Text(text = if (currentState == StopwatchState.Started) "Stop"
            else if ((currentState == StopwatchState.Stopped)) "Resume"
            else "Start")
            }
        Button(modifier = Modifier.weight(1f).fillMaxHeight(0.8f),
            onClick = {
                ServiceHelper.triggerForegroundService(
                    context,
                    ACTION_SERVICE_CANCEL
                )
            },
            enabled = seconds != "00" && currentState != StopwatchState.Started) {
            Text(text = "Cancel")
        }

    }
}

@OptIn(ExperimentalAnimationApi::class)
fun Animation(duration: Int=800): ContentTransform {
    return slideInVertically(animationSpec = tween(duration)) { height -> height } + fadeIn(
        animationSpec = tween(
            duration
        )
    ) with slideOutVertically(animationSpec = tween(duration)) { height -> -height } + fadeOut(
        animationSpec = tween(
            duration
        )
    )
}