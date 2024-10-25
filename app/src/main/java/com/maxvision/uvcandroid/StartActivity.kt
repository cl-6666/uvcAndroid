package com.maxvision.uvcandroid

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.maxvision.uvcandroid.util.navigateTo

class StartActivity : AppCompatActivity() {

    private val ACTION_USB_PERMISSION = "com.example.USB_PERMISSION"
    private lateinit var usbManager: UsbManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_start)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        usbManager = getSystemService(Context.USB_SERVICE) as UsbManager
        // 创建 PendingIntent
        val permissionIntent = PendingIntent.getBroadcast(this, 0, Intent(ACTION_USB_PERMISSION), 0)
        // 注册广播接收器
        registerReceiver(usbReceiver, IntentFilter(ACTION_USB_PERMISSION))
        // 获取连接的 USB 设备列表
        val deviceList = usbManager.deviceList
        for (device in deviceList.values) {
            // 请求 USB 权限
            usbManager.requestPermission(device, permissionIntent)
        }
    }

    fun oneClick(view: View) {
        navigateTo<MainActivity>()
    }

    fun manyClick(view: View) {}


    private val usbReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val action = intent?.action
            if (ACTION_USB_PERMISSION == action) {
                synchronized(this) {
                    val usbDevice: UsbDevice? = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE)
                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        if (usbDevice != null) {
                            // 这里可以开始与 USB 设备的通信
                            Log.d("USB", "Permission granted for device: ${usbDevice.deviceName}")
                        } else {

                        }
                    } else {
                        Log.d("USB", "Permission denied for device: ${usbDevice?.deviceName}")
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // 取消注册广播接收器
        unregisterReceiver(usbReceiver)
    }
}