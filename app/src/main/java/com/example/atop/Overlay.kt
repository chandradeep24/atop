package com.example.atop

import android.app.ActivityManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.BatteryManager
import android.os.Build
import android.os.Handler
import android.os.HandlerThread
import android.os.IBinder
import android.os.Looper
import android.os.Process
import android.view.Choreographer
import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.TextView
import java.text.DecimalFormat
import kotlin.math.abs


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

//    private lateinit var gpuNumberTextView: TextView
    private lateinit var powerNumberTextView: TextView
    private lateinit var cpuNumberTextView: TextView
    private lateinit var memoryNumberTextView: TextView
    private lateinit var fpsNumberTextView: TextView
    private lateinit var fps1LowNumberTextView: TextView
    private lateinit var fps01LowNumberTextView: TextView

    private var previousEnergy: Long = 0
    private var previousTime: Long = 0

    private val decimalFormat = DecimalFormat("0.00")
    private val integerFormat = DecimalFormat("0")

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
//        gpuNumberTextView = overlayView.findViewById(R.id.gpuNumber)
        powerNumberTextView = overlayView.findViewById(R.id.powerNumber)
        cpuNumberTextView = overlayView.findViewById(R.id.cpuNumber)
        memoryNumberTextView = overlayView.findViewById(R.id.memoryNumber)
        fpsNumberTextView = overlayView.findViewById(R.id.fpsNumber)
        fps1LowNumberTextView = overlayView.findViewById(R.id.fps1LowNumber)
        fps01LowNumberTextView = overlayView.findViewById(R.id.fps01LowNumber)

        monitoringHandler.post(monitoringRunnable)

        //FPS calculation
        Choreographer.getInstance().postFrameCallback(choreographerCallback)

        windowManager.addView(overlayView,overlayLayoutParams)
    }

    private fun monitorPerformance() {
        //Helper logic
        //Power
        val batteryManager = getSystemService(Context.BATTERY_SERVICE) as BatteryManager
        val batteryPercentage = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
        val minVoltage = 3.2
        val maxVoltage = 4.2
        val voltage = minVoltage + (maxVoltage - minVoltage) * (batteryPercentage / 100.0)
        val current = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW) / 1000000.0
        val energy = abs(current * voltage)

        //CPU
        val cpuUtilization = CpuInfo.getCpuUsageFromFreq()

        //MEM
        val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memoryInfo)
        val totalMemory = memoryInfo.totalMem
        val availableMemory = memoryInfo.availMem
        val usedMemory = totalMemory - availableMemory
        val memoryUtilization = usedMemory / (1024*1024)


        //GPU
//        val gpuUtilization =  0


        //FPS
//        val currentFps = 0
//        val fps1Low = 0
//        val fps01Low = 0

        //Update TextViews
        mainHandler.post {
//            val gpuText = getString(R.string.gpu_utilization, decimalFormat.format(gpuUtilization))
//            gpuNumberTextView.text = gpuText

            val powerText = getString(R.string.power_consumption, decimalFormat.format(energy))
            powerNumberTextView.text = powerText

            val cpuText = getString(R.string.cpu_utilization, decimalFormat.format(cpuUtilization))
            cpuNumberTextView.text = cpuText

            val memoryText = getString(R.string.memory_utilization, integerFormat.format(memoryUtilization))
            memoryNumberTextView.text = memoryText

//            fpsNumberTextView.text = currentFps.toString()
//            fps1LowNumberTextView.text = fps1Low.toString()
//            fps01LowNumberTextView.text = fps01Low.toString()
        }

        //Update Interval: 1s
        monitoringHandler.postDelayed(monitoringRunnable, 2000)
    }

    private val choreographerCallback = object : Choreographer.FrameCallback {
        private var lastFrameTimeNanos: Long = 0
        private var frameCount = 0
        private var fps1LowCount : Long = 0
        private var fps01LowCount : Long = 0
        private val fpsHistory: MutableList<Long> = mutableListOf()

        override fun doFrame(frameTimeNanos: Long) {
            if (lastFrameTimeNanos != 0L) {
                val frameTimeDiff = frameTimeNanos - lastFrameTimeNanos
                val frameRate = (1_000_000_000L / frameTimeDiff)

                fpsHistory.add(frameRate)

                fps1LowCount = calculatePercentileFps(fpsHistory, 1.0)
                fps01LowCount = calculatePercentileFps(fpsHistory, 0.1)

                // Update FPS and 1% Low, 0.1% Low TextViews
                mainHandler.post {
                    fpsNumberTextView.text = frameRate.toString()
                    fps1LowNumberTextView.text = fps1LowCount.toString()
                    fps01LowNumberTextView.text = fps01LowCount.toString()
                }
            }

            frameCount++
            lastFrameTimeNanos = frameTimeNanos

            Choreographer.getInstance().postFrameCallback(this)
        }

        private fun calculatePercentileFps(fpsHistory: List<Long>, percentile: Double): Long {
            val sortedFps = fpsHistory.sorted()
            val index = (percentile / 100.0 * sortedFps.size).toInt()
            return sortedFps[index]
        }
    }





    override fun onDestroy() {
        super.onDestroy()
        windowManager.removeView(overlayView)

        //Quit the thread
        Choreographer.getInstance().removeFrameCallback(choreographerCallback)
        monitoringHandler.removeCallbacks(monitoringRunnable)
        monitoringHandlerThread.quitSafely()
    }
}