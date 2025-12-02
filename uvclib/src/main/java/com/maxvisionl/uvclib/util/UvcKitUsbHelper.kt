package com.maxvisionl.uvclib.util

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbDeviceConnection
import android.hardware.usb.UsbManager
import android.os.Build
import com.cl.uvckit.core.xu.XUControl

/**
 * UvcKit USB 连接辅助工具类
 * 简化 USB 设备的连接流程，无需手动处理广播接收器
 * 
 * 使用示例：
 * ```kotlin
 * val usbHelper = UvcKitUsbHelper(context)
 * usbHelper.connect(
 *     onConnected = { deviceInfo ->
 *         // 设备连接成功
 *         XUControl.setWhiteLight(deviceInfo, 50)
 *     },
 *     onError = { error ->
 *         // 连接失败
 *         Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
 *     }
 * )
 * name：cl
 * @since 2025-12-01
 */
class UvcKitUsbHelper(private val context: Context) {

    private val usbManager = context.getSystemService(Context.USB_SERVICE) as UsbManager
    private var usbConnection: UsbDeviceConnection? = null
    private var currentDevice: UsbDevice? = null
    private var deviceInfo: XUControl.UsbDeviceInfo? = null
    
    private var onConnectedCallback: ((XUControl.UsbDeviceInfo) -> Unit)? = null
    private var onErrorCallback: ((String) -> Unit)? = null
    private var onDisconnectedCallback: (() -> Unit)? = null
    
    private val ACTION_USB_PERMISSION = "com.cl.uvckit.USB_PERMISSION"
    
    private val usbReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                ACTION_USB_PERMISSION -> {
                    synchronized(this) {
                        val device = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            intent.getParcelableExtra(UsbManager.EXTRA_DEVICE, UsbDevice::class.java)
                        } else {
                            @Suppress("DEPRECATION")
                            intent.getParcelableExtra(UsbManager.EXTRA_DEVICE)
                        }
                        
                        if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                            device?.let { connectDeviceInternal(it) }
                        } else {
                            onErrorCallback?.invoke("USB权限被拒绝")
                        }
                    }
                }
                UsbManager.ACTION_USB_DEVICE_DETACHED -> {
                    disconnect()
                    onDisconnectedCallback?.invoke()
                }
            }
        }
    }
    
    private var receiverRegistered = false

    /**
     * 连接 USB 设备（自动选择第一个设备）
     * 
     * @param onConnected 连接成功回调
     * @param onError 连接失败回调
     * @param onDisconnected 设备断开回调（可选）
     */
    fun connect(
        onConnected: (XUControl.UsbDeviceInfo) -> Unit,
        onError: (String) -> Unit,
        onDisconnected: (() -> Unit)? = null
    ) {
        this.onConnectedCallback = onConnected
        this.onErrorCallback = onError
        this.onDisconnectedCallback = onDisconnected
        
        // 注册广播接收器
        registerReceiver()
        
        // 查找设备
        val deviceList = usbManager.deviceList
        if (deviceList.isEmpty()) {
            onError("未找到USB设备")
            return
        }
        
        val device = deviceList.values.firstOrNull()
        if (device != null) {
            requestPermissionAndConnect(device)
        } else {
            onError("未找到合适的USB设备")
        }
    }
    
    /**
     * 连接指定的 USB 设备
     * 
     * @param device USB 设备
     * @param onConnected 连接成功回调
     * @param onError 连接失败回调
     * @param onDisconnected 设备断开回调（可选）
     */
    fun connect(
        device: UsbDevice,
        onConnected: (XUControl.UsbDeviceInfo) -> Unit,
        onError: (String) -> Unit,
        onDisconnected: (() -> Unit)? = null
    ) {
        this.onConnectedCallback = onConnected
        this.onErrorCallback = onError
        this.onDisconnectedCallback = onDisconnected
        
        // 注册广播接收器
        registerReceiver()
        
        requestPermissionAndConnect(device)
    }
    
    /**
     * 获取所有 USB 设备列表
     */
    fun getDeviceList(): List<UsbDevice> {
        return usbManager.deviceList.values.toList()
    }
    
    /**
     * 获取当前连接的设备信息
     */
    fun getDeviceInfo(): XUControl.UsbDeviceInfo? {
        return deviceInfo
    }
    
    /**
     * 获取当前连接的 USB 设备
     */
    fun getCurrentDevice(): UsbDevice? {
        return currentDevice
    }
    
    /**
     * 检查是否已连接
     */
    fun isConnected(): Boolean {
        return deviceInfo != null && usbConnection != null
    }
    
    /**
     * 断开连接
     */
    fun disconnect() {
        XUControl.release()
        usbConnection?.close()
        usbConnection = null
        currentDevice = null
        deviceInfo = null
    }
    
    /**
     * 释放资源（在 Activity/Fragment 销毁时调用）
     */
    fun release() {
        disconnect()
        unregisterReceiver()
    }
    
    private fun registerReceiver() {
        if (receiverRegistered) return
        
        val filter = IntentFilter().apply {
            addAction(ACTION_USB_PERMISSION)
            addAction(UsbManager.ACTION_USB_DEVICE_DETACHED)
        }
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(usbReceiver, filter, Context.RECEIVER_EXPORTED)
        } else {
            context.registerReceiver(usbReceiver, filter)
        }
        
        receiverRegistered = true
    }
    
    private fun unregisterReceiver() {
        if (!receiverRegistered) return
        
        try {
            context.unregisterReceiver(usbReceiver)
            receiverRegistered = false
        } catch (e: Exception) {
            // 忽略未注册的异常
        }
    }
    
    private fun requestPermissionAndConnect(device: UsbDevice) {
        if (usbManager.hasPermission(device)) {
            connectDeviceInternal(device)
        } else {
            val pendingIntent = PendingIntent.getBroadcast(
                context, 0, Intent(ACTION_USB_PERMISSION),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            usbManager.requestPermission(device, pendingIntent)
        }
    }
    
    private fun connectDeviceInternal(device: UsbDevice) {
        // 先关闭已有连接
        disconnect()
        
        val connection = usbManager.openDevice(device)
        if (connection != null) {
            currentDevice = device
            usbConnection = connection
            deviceInfo = XUControl.UsbDeviceInfo(
                connection.fileDescriptor,
                device.vendorId,
                device.productId,
                device.deviceName,
                0,
                0
            )
            
            onConnectedCallback?.invoke(deviceInfo!!)
        } else {
            onErrorCallback?.invoke("无法打开设备")
        }
    }
    
    companion object {
        /**
         * 创建简化版本（只需一行代码连接设备）
         */
        fun quickConnect(
            context: Context,
            onConnected: (XUControl.UsbDeviceInfo) -> Unit,
            onError: (String) -> Unit = {}
        ): UvcKitUsbHelper {
            return UvcKitUsbHelper(context).apply {
                connect(onConnected, onError)
            }
        }
    }
}
