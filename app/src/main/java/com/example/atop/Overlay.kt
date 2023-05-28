package com.example.atop

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.WindowManager

class Overlay: Service() {

    private lateinit var overlayView: ViewGroup
    private lateinit var overlayLayoutParams: WindowManager.LayoutParams
    private var LAYOUT_TYPE: Int? =null
    private lateinit var windowManager: WindowManager

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }
    override fun onCreate() {
        super.onCreate()

        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        val inflater = baseContext.getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
        overlayView = inflater.inflate(R.layout.overlay,null) as ViewGroup
    }
}