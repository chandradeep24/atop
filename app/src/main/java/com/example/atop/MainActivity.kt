package com.example.atop

import android.app.ActivityManager
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var dialog: AlertDialog
    private lateinit var  launchButton: Button
    private var isServiceRunning = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        launchButton = findViewById(R.id.launchButton)

        if (!checkOverlayPermissions()) {
            requestOverlayPermission()
        }

        launchButton.setOnClickListener {
            toggleOverlayService()
        }


    }

    private fun isServiceRunning(): Boolean{
        val manager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager

        for (service in manager.getRunningServices(Int.MAX_VALUE)) {

            if (Overlay::class.java.name == service.service.className) {
                return true
            }
        }

        return false
    }

    private fun requestOverlayPermission() {

        val builder =  AlertDialog.Builder(this)
        builder.setCancelable(true)
        builder.setTitle("Screen Overlay Permission Required")
        builder.setMessage("Enable 'Draw over other apps' in settings")
        builder.setPositiveButton("Open Settings", DialogInterface.OnClickListener { dialog, which ->

            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            )

            startActivityForResult(intent, RESULT_OK)

        })
        dialog = builder.create()
        dialog.show()

    }

    private fun checkOverlayPermissions(): Boolean {
        return Settings.canDrawOverlays(this)
    }

    override fun onResume() {
        super.onResume()
        // Check if the service is running when the app resumes
        isServiceRunning = isServiceRunning()
        updateLaunchButtonText()
    }

    private fun updateLaunchButtonText() {
        if (isServiceRunning()) {
            launchButton.text = getString(R.string.stop_overlay)
        } else {
            launchButton.text = getString(R.string.launch_overlay)
        }
    }

    private fun toggleOverlayService() {
        if (!isServiceRunning) {
            // Check again if permissions were given
            if (!checkOverlayPermissions()) {
                requestOverlayPermission()
            } else {
                startService(Intent(this@MainActivity, Overlay::class.java))
                isServiceRunning = true
                updateLaunchButtonText()
            }
        } else {
            stopService(Intent(this@MainActivity, Overlay::class.java))
            isServiceRunning = false
            updateLaunchButtonText()
        }
    }


}