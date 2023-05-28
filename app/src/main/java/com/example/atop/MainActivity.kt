package com.example.atop

import android.app.ActivityManager
import android.app.AlertDialog
import android.app.RecoverableSecurityException
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.widget.TextView

class MainActivity : AppCompatActivity() {

    private lateinit var dialog: AlertDialog
    private val overlayRequestCode = 1001
    private val overlayPermissionCode = 2001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        if (!checkOverlayPermissions()) {
            requestOverlayPermission()
        }
        else {
            startOverlayService()
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

    private fun startOverlayService() {
        if (!isServiceRunning()) {
            startService(Intent(this, Overlay::class.java))
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == overlayRequestCode) {
            if (checkOverlayPermissions()) {
                startOverlayService()
            }
            else {
                requestOverlayPermission()
            }
        }
    }









}