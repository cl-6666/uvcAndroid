package com.maxvision

import android.graphics.SurfaceTexture
import android.hardware.usb.UsbDevice
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.TextureView
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.cl.uvckit.UvcCameraCallback
import com.cl.uvckit.UvcCameraManager
import com.cl.uvckit.UvcConfig
import com.cl.uvckit.UvcError
import com.cl.uvckit.UvcPictureCallback
import com.cl.uvckit.UvcRecordingCallback
import com.cl.uvckit.core.usb.IFrameCallback
import com.cl.uvckit.core.usb.UVCCamera
import com.maxvision.uvcandroid.databinding.ActivitySimpleUvcBinding
import java.io.File
import java.nio.ByteBuffer
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.collections.isNotEmpty
import kotlin.let
import kotlin.text.trimIndent

/**
 * 简化的 UVC 摄像头演示界面
 * 
 * 这个示例展示了如何使用优化后的 UvcKit API：
 * 1. 使用 UvcConfig 配置摄像头参数
 * 2. 使用 UvcCameraManager 管理摄像头
 * 3. 使用简化的回调接口
 * 4. 展示所有核心功能：打开/关闭/拍照/录像/参数调整
 */
class SimpleUvcActivity : AppCompatActivity() {
    
    companion object {
        private const val TAG = "SimpleUvcActivity"
    }
    
    private lateinit var binding: ActivitySimpleUvcBinding
    private var cameraManager: UvcCameraManager? = null
    private var frameCount = 0L
    private var isRecording = false
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySimpleUvcBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        initCamera()
        setupPreview()
        setupButtons()
        updateUI()
    }
    
    /**
     * 初始化摄像头
     * 使用新的 UvcCameraManager API
     */
    private fun initCamera() {
        // 1. 创建配置
        val config = UvcConfig.Builder()
            .setPreviewSize(1280, 720)
            .setAutoSelectDevice(false) // 手动选择设备
            .setEnableDebugLog(true)
            .build()
        
        // 2. 创建管理器
        cameraManager = UvcCameraManager(config)
        
        // 3. 设置回调
        cameraManager?.setCallback(object : UvcCameraCallback {
            override fun onCameraOpened() {
                runOnUiThread {
                    Toast.makeText(this@SimpleUvcActivity, "✅ 摄像头已打开", Toast.LENGTH_SHORT).show()
                    
                    // 隐藏提示图标
                    binding.llNoPreview.visibility = View.GONE
                    
                    updateUI()
                    
                    // 设置帧回调（可选）
                    setupFrameCallback()
                }
            }
            
            override fun onCameraClosed() {
                runOnUiThread {
                    Toast.makeText(this@SimpleUvcActivity, "❌ 摄像头已关闭", Toast.LENGTH_SHORT).show()
                    
                    // 显示提示图标
                    binding.llNoPreview.visibility = View.VISIBLE
                    
                    frameCount = 0
                    updateUI()
                }
            }
            
            override fun onDeviceAttached(device: UsbDevice) {
                runOnUiThread {
                    Toast.makeText(this@SimpleUvcActivity, "🔌 设备已连接: ${device.deviceName}", Toast.LENGTH_SHORT).show()
                }
            }
            
            override fun onDeviceDetached(device: UsbDevice) {
                runOnUiThread {
                    Toast.makeText(this@SimpleUvcActivity, "🔌 设备已断开: ${device.deviceName}", Toast.LENGTH_SHORT).show()
                    updateUI()
                }
            }
            
            override fun onError(error: UvcError, message: String?) {
                runOnUiThread {
                    Toast.makeText(this@SimpleUvcActivity, "❌ 错误: $message", Toast.LENGTH_LONG).show()
                    Log.e(TAG, "Camera error: $error - $message")
                }
            }
        })
    }
    
    /**
     * 设置帧回调（演示如何获取 YUV 数据）
     */
    private fun setupFrameCallback() {
        cameraManager?.setFrameCallback(object : IFrameCallback {
            override fun onFrame(frame: ByteBuffer?) {
                frame?.let {
                    frameCount++
                    if (frameCount % 30 == 0L) { // 每30帧更新一次UI
                        runOnUiThread {
                            binding.tvFrameInfo.text = "帧数: $frameCount"
                            Log.i(TAG, "高度"+cameraManager?.getPreviewSize()?.height+"------宽度" + cameraManager?.getPreviewSize()?.width)
                        }
                    }
                }
            }
        }, UVCCamera.PIXEL_FORMAT_YUV)
    }
    
    /**
     * 设置预览视图
     * 处理 Surface 的生命周期
     */
    private fun setupPreview() {
        binding.textureView.surfaceTextureListener = object : TextureView.SurfaceTextureListener {
            override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
                Log.d(TAG, "Surface 可用: ${width}x${height}")
                // Surface 已准备好，可以在这里自动打开摄像头（如果需要）
            }
            
            override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
                Log.d(TAG, "Surface 销毁")
                return false
            }
            
            override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {}
            override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {}
        }
    }
    
    /**
     * 设置按钮点击事件
     */
    private fun setupButtons() {
        // 打开摄像头
        binding.btnOpen.setOnClickListener {
            openCamera()
        }
        
        // 关闭摄像头
        binding.btnClose.setOnClickListener {
            closeCamera()
        }
        
        // 重新打开摄像头
        binding.btnReopen.setOnClickListener {
            reopenCamera()
        }
        
        // 拍照
        binding.btnTakePicture.setOnClickListener {
            takePicture()
        }
        
        // 录像
        binding.btnRecord.setOnClickListener {
            toggleRecording()
        }
        
        // 调整亮度
        binding.btnAdjustBrightness.setOnClickListener {
            adjustBrightness()
        }
        
        // 返回
        binding.btnBack.setOnClickListener {
            finish()
        }
    }
    
    /**
     * 打开摄像头
     * 使用新的 UvcCameraManager API
     */
    private fun openCamera() {
        val surface = binding.textureView.surfaceTexture
        if (surface == null) {
            Toast.makeText(this, "Surface 未就绪", Toast.LENGTH_SHORT).show()
            return
        }
        
        val result = cameraManager?.openCamera(surface)
        if (result?.isFailure == true) {
            Toast.makeText(this, "打开失败: ${result.errorDescription}", Toast.LENGTH_LONG).show()
        }
    }
    
    /**
     * 关闭摄像头
     */
    private fun closeCamera() {
        val result = cameraManager?.closeCamera()
        if (result?.isFailure == true) {
            Toast.makeText(this, "关闭失败: ${result.errorDescription}", Toast.LENGTH_SHORT).show()
        }
    }
    
    /**
     * 重新打开摄像头
     * 演示如何正确处理关闭后重新打开的场景
     */
    private fun reopenCamera() {
        val result = cameraManager?.reopenCamera()
        if (result?.isFailure == true) {
            Toast.makeText(this, "重新打开失败: ${result.errorDescription}", Toast.LENGTH_LONG).show()
        }
    }
    
    /**
     * 拍照
     */
    private fun takePicture() {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val outputFile = File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "UVC_$timestamp.jpg")
        
        val result = cameraManager?.takePicture(outputFile, object : UvcPictureCallback {
            override fun onSuccess(file: File) {
                runOnUiThread {
                    Toast.makeText(this@SimpleUvcActivity, "📷 照片已保存: ${file.name}", Toast.LENGTH_LONG).show()
                }
            }
            
            override fun onError(errorCode: Int, message: String?) {
                runOnUiThread {
                    Toast.makeText(this@SimpleUvcActivity, "拍照失败: $message", Toast.LENGTH_SHORT).show()
                }
            }
        })
        
        if (result?.isFailure == true) {
            Toast.makeText(this, "拍照失败: ${result.errorDescription}", Toast.LENGTH_SHORT).show()
        }
    }
    
    /**
     * 切换录像状态
     */
    private fun toggleRecording() {
        if (isRecording) {
            stopRecording()
        } else {
            startRecording()
        }
    }
    
    /**
     * 开始录像
     */
    private fun startRecording() {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val outputFile = File(getExternalFilesDir(Environment.DIRECTORY_MOVIES), "UVC_$timestamp.mp4")
        
        val result = cameraManager?.startRecording(outputFile, object : UvcRecordingCallback {
            override fun onStart() {
                runOnUiThread {
                    isRecording = true
                    binding.btnRecord.text = "⏹ 停止录像"
                    Toast.makeText(this@SimpleUvcActivity, "🎥 开始录像", Toast.LENGTH_SHORT).show()
                }
            }
            
            override fun onSuccess(file: File) {
                runOnUiThread {
                    isRecording = false
                    binding.btnRecord.text = "🎥 开始录像"
                    Toast.makeText(this@SimpleUvcActivity, "视频已保存: ${file.name}", Toast.LENGTH_LONG).show()
                }
            }
            
            override fun onError(errorCode: Int, message: String?) {
                runOnUiThread {
                    isRecording = false
                    binding.btnRecord.text = "🎥 开始录像"
                    Toast.makeText(this@SimpleUvcActivity, "录像失败: $message", Toast.LENGTH_SHORT).show()
                }
            }
        })
        
        if (result?.isFailure == true) {
            Toast.makeText(this, "开始录像失败: ${result.errorDescription}", Toast.LENGTH_SHORT).show()
        }
    }
    
    /**
     * 停止录像
     */
    private fun stopRecording() {
        val result = cameraManager?.stopRecording()
        if (result?.isFailure == true) {
            Toast.makeText(this, "停止录像失败: ${result.errorDescription}", Toast.LENGTH_SHORT).show()
        }
    }
    
    /**
     * 调整亮度
     */
    private fun adjustBrightness() {
        val control = cameraManager?.getControl()
        if (control != null) {
            try {
                val currentBrightness = control.brightness
                val newBrightness = kotlin.comparisons.minOf(64, (currentBrightness * 1.2).toInt())
                control.brightness = newBrightness
                Toast.makeText(this, "亮度已调整: $newBrightness", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(this, "调整亮度失败: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "摄像头未打开", Toast.LENGTH_SHORT).show()
        }
    }
    
    /**
     * 更新 UI 状态
     */
    private fun updateUI() {
        val isOpened = cameraManager?.isOpened() == true
        val devices = cameraManager?.getDeviceList() ?: emptyList()
        val hasDevice = devices.isNotEmpty()
        
        binding.btnOpen.isEnabled = !isOpened && hasDevice
        binding.btnClose.isEnabled = isOpened
        binding.btnReopen.isEnabled = !isOpened && hasDevice
        binding.btnTakePicture.isEnabled = isOpened
        binding.btnRecord.isEnabled = isOpened
        binding.btnAdjustBrightness.isEnabled = isOpened
        
        // 更新状态文本
        binding.tvStatus.text = when {
            isOpened -> "✅ 摄像头已打开"
            hasDevice -> "⚠️ 摄像头已关闭"
            else -> "❌ 未连接设备"
        }
        
        // 更新设备信息
        if (devices.isNotEmpty()) {
            val device = devices[0]
            binding.tvDeviceInfo.text = """
                设备名称: ${device.deviceName}
                产品名称: ${device.productName ?: "未知"}
                厂商ID: ${device.vendorId}
                产品ID: ${device.productId}
            """.trimIndent()
        } else {
            binding.tvDeviceInfo.text = "暂无设备信息"
        }
        
        // 更新分辨率信息
        if (isOpened) {
            val size = cameraManager?.getPreviewSize()
            binding.tvResolution.text = if (size != null) {
                "分辨率: ${size.width}x${size.height}"
            } else {
                "分辨率: 未知"
            }
            binding.textureView.setAspectRatio(size!!.width, size.height)
        } else {
            binding.tvResolution.text = "分辨率: -"
        }

    }
    
    override fun onDestroy() {
        super.onDestroy()
        cameraManager?.release()
    }
}
