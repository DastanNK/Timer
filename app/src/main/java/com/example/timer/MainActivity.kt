package com.example.timer

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.timer.ui.theme.TimerTheme
import dagger.hilt.android.AndroidEntryPoint
import java.util.*

@ExperimentalAnimationApi
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    lateinit var stopwatchService: StopwatchService
    private var isBound by mutableStateOf(false)
    private val connection=object: ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as StopwatchService.StopwatchBinder
            stopwatchService=binder.getService()
            isBound=true
        }
        override fun onServiceDisconnected(name: ComponentName?) {
            isBound=false
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TimerTheme {
                // A surface container using the 'background' color from the theme
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    if(isBound){
                        MainScreen(stopwatchService)
                    }
                }
            }
        }
    }

    private fun requestPermission(vararg permissions: String) {
        val requestPermissionLauncher=registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()){result->
            result.entries.forEach{
                Log.d("MainActivity", "${it.key} = ${it.value}")
            }
        }
        requestPermissionLauncher.launch(permissions.asList().toTypedArray())
    }

    override fun onStart() {
        super.onStart()
        Intent(this, StopwatchService::class.java).also { intent ->
            bindService(intent, connection, Context.BIND_AUTO_CREATE)
        }

    }

    override fun onStop() {
        super.onStop()
        unbindService(connection)
        isBound = false
    }
}