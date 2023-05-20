package com.example.atop

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.example.atop.ui.theme.AtopTheme
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import kotlin.math.roundToInt
import android.view.WindowManager
import androidx.annotation.RequiresApi
import android.provider.Settings
import androidx.activity.result.contract.ActivityResultContracts

class MainActivity : ComponentActivity() {
    private val resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val data: Intent? = result.data
        }
    }
    @RequiresApi(Build.VERSION_CODES.O)
    private fun requestOverlayPermission() {
        val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))
        resultLauncher.launch(intent)
    }

    companion object {
        private const val REQUEST_OVERLAY_PERMISSION = 1001
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AtopTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    BubbleOverlay()
                }
            }
        }

        if (!hasOverlayPermission()) {
            requestOverlayPermission()
        }
    }

    private fun hasOverlayPermission(): Boolean {
        return Settings.canDrawOverlays(this)
    }


    @RequiresApi(Build.VERSION_CODES.O)
    @Composable
    fun BubbleOverlay() {
        val context = LocalContext.current
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager

        // Create the ComposeView
        val bubbleOverlay = remember {
            ComposeView(context).apply {
                setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
                setContent {
                    MaterialTheme {
                        OverlayScreen()
                    }
                }
            }
        }

        // Add the overlay view using the WindowManager
        AndroidView({ bubbleOverlay }) { view ->
            val layoutParams = WindowManager.LayoutParams().apply {
                type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                        WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                        WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                width = WindowManager.LayoutParams.WRAP_CONTENT
                height = WindowManager.LayoutParams.WRAP_CONTENT
                x = 0
                y = 0
            }
            windowManager.addView(view, layoutParams)
        }
    }


    @Composable
    fun OverlayScreen() {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .offset {
                    IntOffset(0, 0)
                }
        ) {
            Box(
                modifier = Modifier
                    .height(50.dp)
                    .width(200.dp)
                    .background(color = Color.Transparent)
            ) {
                Text("Hello Compose!")
            }
        }
    }

}

