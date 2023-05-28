package com.example.atop

import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.WindowManager

class Overlay: Service() {

    private lateinit var overlayView: ViewGroup
    private lateinit var overlayLayoutParams: WindowManager.LayoutParams
    private var layoutType: Int? =null
    private lateinit var windowManager: WindowManager

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }
    override fun onCreate() {
        super.onCreate()

        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        val inflater = baseContext.getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
        overlayView = inflater.inflate(R.layout.overlay,null) as ViewGroup


        layoutType = if (Build.VERSION.SDK_INT >=  Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else WindowManager.LayoutParams.TYPE_TOAST

        overlayLayoutParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            layoutType!!,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
            PixelFormat.TRANSLUCENT
        )

        overlayLayoutParams.gravity = Gravity.TOP or Gravity.START
        overlayLayoutParams.x = 0
        overlayLayoutParams.y = 0

        windowManager.addView(overlayView,overlayLayoutParams)
    }
}