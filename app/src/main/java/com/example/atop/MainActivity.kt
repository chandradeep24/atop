package com.example.atop

import android.app.ActivityManager
import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings

class MainActivity : AppCompatActivity() {

    private lateinit var dialog: AlertDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
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









}