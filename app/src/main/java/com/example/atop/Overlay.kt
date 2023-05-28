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
import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.os.Process
import android.widget.TextView

class Overlay: Service() {

    private lateinit var overlayView: ViewGroup
    private lateinit var overlayLayoutParams: WindowManager.LayoutParams
    private var layoutType: Int? =null
    private lateinit var windowManager: WindowManager

    //Performance metrics
    private lateinit var monitoringHandlerThread: HandlerThread
    private lateinit var monitoringHandler: Handler
    private lateinit var monitoringRunnable: Runnable
    private val mainHandler = Handler(Looper.getMainLooper())

    private lateinit var gpuNumberTextView: TextView
    private lateinit var powerNumberTextView: TextView
    private lateinit var cpuNumberTextView: TextView
    private lateinit var memoryNumberTextView: TextView
    private lateinit var fpsNumberTextView: TextView
    private lateinit var fps1LowNumberTextView: TextView
    private lateinit var fps01LowNumberTextView: TextView

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

        //Monitoring thread init
        monitoringHandlerThread = HandlerThread("MonitoringThread", Process.THREAD_PRIORITY_BACKGROUND)
        monitoringHandlerThread.start()
        monitoringHandler = Handler(monitoringHandlerThread.looper)
        monitoringRunnable = Runnable { monitorPerformance() }

        //TextView init
        gpuNumberTextView = overlayView.findViewById(R.id.gpuNumber)
        powerNumberTextView = overlayView.findViewById(R.id.powerNumber)
        cpuNumberTextView = overlayView.findViewById(R.id.cpuNumber)
        memoryNumberTextView = overlayView.findViewById(R.id.memoryNumber)
        fpsNumberTextView = overlayView.findViewById(R.id.fpsNumber)
        fps1LowNumberTextView = overlayView.findViewById(R.id.fps1LowNumber)
        fps01LowNumberTextView = overlayView.findViewById(R.id.fps01LowNumber)

        monitoringHandler.post(monitoringRunnable)

        windowManager.addView(overlayView,overlayLayoutParams)
    }

    private fun monitorPerformance() {
        //Logic to calculate metrics
        val gpuUtilization =  0
        val powerConsumption = 0
        val cpuUtilization = 0
        val memoryUtilization = 0
        val currentFps = 0
        val fps1Low = 0
        val fps01Low = 0

        //Update TextViews
        mainHandler.post {
            gpuNumberTextView.text = gpuUtilization.toString()
            powerNumberTextView.text = powerConsumption.toString()
            cpuNumberTextView.text = cpuUtilization.toString()
            memoryNumberTextView.text = memoryUtilization.toString()
            fpsNumberTextView.text = currentFps.toString()
            fps1LowNumberTextView.text = fps1Low.toString()
            fps01LowNumberTextView.text = fps01Low.toString()
        }

        //Update Interval: 1s
        monitoringHandler.postDelayed(monitoringRunnable, 1000)
    }

    override fun onDestroy() {
        super.onDestroy()
        windowManager.removeView(overlayView)

        //Quit the thread
        monitoringHandler.removeCallbacks(monitoringRunnable)
        monitoringHandlerThread.quitSafely()
    }
}