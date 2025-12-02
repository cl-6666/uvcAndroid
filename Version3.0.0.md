# UvcKit - Android UVC 摄像头集成库

UvcKit 是一个功能强大、易于使用的 Android UVC 摄像头集成库，支持 USB 摄像头的预览、拍照、录像等功能。

## 📋 目录

- [特性](#特性)
- [快速开始](#快速开始)
- [集成步骤](#集成步骤)
- [基本使用](#基本使用)
- [高级功能](#高级功能)
- [API 参考](#api-参考)
- [示例代码](#示例代码)
- [常见问题](#常见问题)

## ✨ 特性

- 🎯 **简化的 API** - 使用 Builder 模式，3 步完成集成
- 📱 **完整功能** - 支持预览、拍照、录像、参数调整
- 🔧 **灵活配置** - 支持多种分辨率、帧率配置
- 🛡️ **线程安全** - 所有操作都是线程安全的
- 📊 **统一错误处理** - 清晰的错误信息和状态管理
- 🔌 **自动设备管理** - 自动处理设备连接、权限请求
- 📷 **多摄像头支持** - 支持多个 UVC 设备同时使用
- 🎨 **现代化设计** - 基于最新的 Android 开发规范

## 🚀 快速开始

### 系统要求

- **Android API Level**: 21+ (Android 5.0+)
- **架构支持**: armeabi-v7a, arm64-v8a, x86, x86_64
- **权限要求**: USB 设备访问权限（自动处理）

### 添加依赖

#### 方式 1: Gradle 依赖（推荐）

在项目根目录的 `build.gradle` 文件中添加：

```gradle
allprojects {
    repositories {
        google()
        mavenCentral()
        maven { url 'https://jitpack.io' }
    }
}
```

在应用模块的 `build.gradle` 文件中添加：

```gradle
dependencies {
    implementation 'com.cl:UvcKit:1.0.10'
}
```

#### 方式 2: 本地 AAR 文件

1. 下载最新的 `uvckit-release.aar` 文件
2. 将 AAR 文件放到 `app/libs/` 目录下
3. 在 `build.gradle` 中添加：

```gradle
dependencies {
    implementation files('libs/uvckit-release.aar')
    
    // 必需的依赖
    implementation 'androidx.core:core:1.13.1'
    implementation 'androidx.appcompat:appcompat:1.6.1'
    implementation 'androidx.annotation:annotation:1.8.0'
    implementation 'cn.hutool:hutool-core:5.8.35'
    implementation 'com.github.cl-6666:ZLog:v2.0.0'
}
```

### 权限配置

在 `AndroidManifest.xml` 中添加必要权限：

```xml
<!-- USB 设备访问权限 -->
<uses-permission android:name="android.permission.USB_PERMISSION" />
<uses-feature android:name="android.hardware.usb.host" />

<!-- 文件存储权限（用于拍照和录像） -->
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />

<!-- Android 13+ 媒体权限 -->
<uses-permission android:name="android.permission.READ_MEDIA_IMAGES" />
<uses-permission android:name="android.permission.READ_MEDIA_VIDEO" />
```

## 📖 集成步骤

### 步骤 1: 初始化应用

在 `Application` 类中初始化 UvcKit：

```kotlin
class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // 初始化 UvcKit（可选）
        val logPath = "${getExternalFilesDir(null)?.absolutePath}/logs"
        UvcKitWrapper.init(this, true, logPath)
    }
}
```

### 步骤 2: 创建布局

```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical">

    <!-- 预览视图 -->
    <TextureView
        android:id="@+id/textureView"
        android:layout_width="match_parent"
        android:layout_height="0dp"
        android:layout_weight="1" />

    <!-- 控制按钮 -->
    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="horizontal"
        android:padding="16dp">

        <Button
            android:id="@+id/btnOpen"
            android:layout_width="0dp"
            android:layout_height="wrap_content"
            android:layout_weight="1"
            android:text="打开摄像头" />

        <Button
            android:id="@+id/btnTakePicture"
            android:layout_width="0dp"
            android:layout_height="wrap_content"
            android:layout_weight="1"
            android:text="拍照" />

        <Button
            android:id="@+id/btnRecord"
            android:layout_width="0dp"
            android:layout_height="wrap_content"
            android:layout_weight="1"
            android:text="录像" />

    </LinearLayout>

</LinearLayout>
```

### 步骤 3: 实现 Activity

```kotlin
class MainActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityMainBinding
    private var cameraManager: UvcCameraManager? = null
    private var isRecording = false
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        initCamera()
        setupPreview()
        setupButtons()
    }
    
    private fun initCamera() {
        // 1. 创建配置
        val config = UvcConfig.Builder()
            .setPreviewSize(1280, 720)
            .setAutoSelectDevice(false)
            .setEnableDebugLog(true)
            .setVideocntThreshold(1) // 新增：设置视频计数阈值
            .build()
        
        // 2. 创建管理器
        cameraManager = UvcCameraManager(config)
        
        // 3. 设置回调
        cameraManager?.setCallback(object : UvcCameraCallback {
            override fun onCameraOpened() {
                runOnUiThread {
                    Toast.makeText(this@MainActivity, "摄像头已打开", Toast.LENGTH_SHORT).show()
                    updateUI()
                }
            }
            
            override fun onCameraClosed() {
                runOnUiThread {
                    Toast.makeText(this@MainActivity, "摄像头已关闭", Toast.LENGTH_SHORT).show()
                    updateUI()
                }
            }
            
            override fun onError(error: UvcError, message: String?) {
                runOnUiThread {
                    Toast.makeText(this@MainActivity, "错误: $message", Toast.LENGTH_LONG).show()
                }
            }
        })
    }
    
    private fun setupPreview() {
        binding.textureView.surfaceTextureListener = object : TextureView.SurfaceTextureListener {
            override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
                // Surface 准备好，可以在这里自动打开摄像头
            }
            
            override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean = false
            override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {}
            override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {}
        }
    }
    
    private fun setupButtons() {
        binding.btnOpen.setOnClickListener { openCamera() }
        binding.btnTakePicture.setOnClickListener { takePicture() }
        binding.btnRecord.setOnClickListener { toggleRecording() }
    }
    
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
    
    private fun takePicture() {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val outputFile = File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "UVC_$timestamp.jpg")
        
        val result = cameraManager?.takePicture(outputFile, object : UvcPictureCallback {
            override fun onSuccess(file: File) {
                runOnUiThread {
                    Toast.makeText(this@MainActivity, "照片已保存: ${file.name}", Toast.LENGTH_LONG).show()
                }
            }
            
            override fun onError(errorCode: Int, message: String?) {
                runOnUiThread {
                    Toast.makeText(this@MainActivity, "拍照失败: $message", Toast.LENGTH_SHORT).show()
                }
            }
        })
        
        if (result?.isFailure == true) {
            Toast.makeText(this, result.errorDescription, Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun toggleRecording() {
        if (isRecording) {
            stopRecording()
        } else {
            startRecording()
        }
    }
    
    private fun startRecording() {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val outputFile = File(getExternalFilesDir(Environment.DIRECTORY_MOVIES), "UVC_$timestamp.mp4")
        
        val result = cameraManager?.startRecording(outputFile, object : UvcRecordingCallback {
            override fun onStart() {
                runOnUiThread {
                    isRecording = true
                    binding.btnRecord.text = "停止录像"
                    Toast.makeText(this@MainActivity, "开始录像", Toast.LENGTH_SHORT).show()
                }
            }
            
            override fun onSuccess(file: File) {
                runOnUiThread {
                    isRecording = false
                    binding.btnRecord.text = "录像"
                    Toast.makeText(this@MainActivity, "视频已保存: ${file.name}", Toast.LENGTH_LONG).show()
                }
            }
            
            override fun onError(errorCode: Int, message: String?) {
                runOnUiThread {
                    isRecording = false
                    binding.btnRecord.text = "录像"
                    Toast.makeText(this@MainActivity, "录像失败: $message", Toast.LENGTH_SHORT).show()
                }
            }
        })
        
        if (result?.isFailure == true) {
            Toast.makeText(this, result.errorDescription, Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun stopRecording() {
        cameraManager?.stopRecording()
    }
    
    private fun updateUI() {
        val isOpened = cameraManager?.isOpened() == true
        binding.btnOpen.isEnabled = !isOpened
        binding.btnTakePicture.isEnabled = isOpened
        binding.btnRecord.isEnabled = isOpened
    }
    
    override fun onDestroy() {
        super.onDestroy()
        cameraManager?.release()
    }
}
```

## 🔧 高级功能

### 1. 多摄像头支持

```kotlin
// 获取设备列表
val devices = cameraManager?.getDeviceList() ?: emptyList()

// 显示设备选择对话框
fun showDeviceSelectionDialog() {
    if (devices.isEmpty()) {
        Toast.makeText(this, "未找到 UVC 设备", Toast.LENGTH_SHORT).show()
        return
    }
    
    val deviceNames = devices.map { device ->
        "${device.productName ?: "未知设备"} (VID:${device.vendorId} PID:${device.productId})"
    }.toTypedArray()
    
    AlertDialog.Builder(this)
        .setTitle("选择摄像头")
        .setItems(deviceNames) { _, which ->
            openSpecificCamera(devices[which])
        }
        .setNegativeButton("取消", null)
        .show()
}

// 打开指定摄像头
private fun openSpecificCamera(device: UsbDevice) {
    val surface = binding.textureView.surfaceTexture ?: return
    val result = cameraManager?.openCameraWithDevice(device, surface)
    if (result?.isFailure == true) {
        Toast.makeText(this, result.errorDescription, Toast.LENGTH_LONG).show()
    }
}
```

### 2. 摄像头参数调整

```kotlin
fun adjustCameraParameters() {
    val control = cameraManager?.getControl()
    if (control != null) {
        try {
            // 调整亮度 (0-255)
            control.brightness = 128
            
            // 调整对比度 (0-255)
            control.contrast = 128
            
            // 调整饱和度 (0-255)
            control.saturation = 128
            
            // 自动白平衡
            control.setAutoWhiteBalance(true)
            
            // 自动曝光
            control.setAutoExposure(true)
            
            Toast.makeText(this, "参数已调整", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(this, "调整失败: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}
```

### 3. 获取 YUV 帧数据

```kotlin
fun setupFrameCallback() {
    cameraManager?.setFrameCallback(object : IFrameCallback {
        override fun onFrame(frame: ByteBuffer?) {
            frame?.let {
                val size = cameraManager?.getPreviewSize()
                val width = size?.width ?: 0
                val height = size?.height ?: 0
                
                // 处理 YUV 数据
                processYUVFrame(it, width, height)
            }
        }
    }, UVCCamera.PIXEL_FORMAT_YUV)
}

private fun processYUVFrame(frame: ByteBuffer, width: Int, height: Int) {
    // 在这里可以进行：
    // - 图像处理
    // - 人脸识别
    // - 二维码识别
    // - 其他计算机视觉任务
}
```

### 4. 支持的分辨率查询

```kotlin
fun getSupportedResolutions() {
    val sizes = cameraManager?.getSupportedSizes()
    sizes?.forEach { size ->
        Log.d(TAG, "支持的分辨率: ${size.width}x${size.height}")
    }
}
```

## 📚 API 参考

### UvcConfig 配置选项

| 方法 | 说明 | 默认值 |
|------|------|--------|
| `setPreviewSize(width, height)` | 设置预览分辨率 | 1280x720 |
| `setAutoSelectDevice(boolean)` | 是否自动选择设备 | false |
| `setEnableDebugLog(boolean)` | 是否启用调试日志 | false |
| `setFrameRate(fps)` | 设置帧率 | 30 |
| `setVideocntThreshold(threshold)` | 设置视频计数阈值 | 0 |

### UvcCameraManager 主要方法

| 方法 | 返回值 | 说明 |
|------|--------|------|
| `openCamera(SurfaceTexture)` | `UvcResult` | 打开第一个可用摄像头 |
| `openCameraWithDevice(UsbDevice, SurfaceTexture)` | `UvcResult` | 打开指定摄像头 |
| `closeCamera()` | `UvcResult` | 关闭摄像头 |
| `reopenCamera(int)` | `UvcResult` | 重新打开摄像头 |
| `takePicture(File, UvcPictureCallback)` | `UvcResult` | 拍照 |
| `startRecording(File, UvcRecordingCallback)` | `UvcResult` | 开始录像 |
| `stopRecording()` | `UvcResult` | 停止录像 |
| `getDeviceList()` | `List<UsbDevice>` | 获取设备列表 |
| `isOpened()` | `boolean` | 检查是否已打开 |
| `getControl()` | `UVCControl` | 获取参数控制器 |
| `release()` | `void` | 释放资源 |

### 错误类型 (UvcError)

| 错误类型 | 说明 |
|---------|------|
| `NONE` | 没有错误 |
| `INVALID_PARAMETER` | 无效参数 |
| `NO_DEVICE` | 未找到设备 |
| `ALREADY_OPENED` | 摄像头已打开 |
| `NOT_OPENED` | 摄像头未打开 |
| `OPEN_FAILED` | 打开失败 |
| `CLOSE_FAILED` | 关闭失败 |
| `PERMISSION_DENIED` | 权限被拒绝 |
| `DEVICE_BUSY` | 设备忙碌 |
| `OPERATION_FAILED` | 操作失败 |

## 💡 示例代码

完整的示例代码请参考：
- **基础示例**: `app/src/main/java/com/cl/test/SimpleUvcActivity.kt`
- **高级示例**: 查看项目中的 demo 模块

## ❓ 常见问题

### Q1: 摄像头无法打开怎么办？

**A**: 请检查以下几点：
1. 确认 USB 摄像头已正确连接
2. 检查是否授予了 USB 权限
3. 确认摄像头支持 UVC 协议
4. 查看日志获取详细错误信息

### Q2: 如何处理权限问题？

**A**: UvcKit 会自动处理 USB 权限请求，当用户拒绝权限时会触发 `onError` 回调：

```kotlin
override fun onError(error: UvcError, message: String?) {
    if (error == UvcError.PERMISSION_DENIED) {
        // 提示用户需要授权
        Toast.makeText(this, "需要 USB 权限才能使用摄像头", Toast.LENGTH_LONG).show()
    }
}
```

### Q3: 支持哪些摄像头？

**A**: UvcKit 支持所有符合 UVC (USB Video Class) 标准的摄像头，包括：
- 大部分 USB 网络摄像头
- 支持 UVC 的工业摄像头
- 部分手机的 USB 摄像头模式

### Q4: 如何优化性能？

**A**:
1. 选择合适的分辨率和帧率
2. 在不需要时及时关闭摄像头
3. 避免在主线程进行图像处理
4. 合理使用帧回调功能

### Q5: 如何调试问题？

**A**:
1. 启用调试日志：`setEnableDebugLog(true)`
2. 查看 Logcat 输出
3. 检查自动生成的日志文件
4. 使用 `UvcResult` 获取详细错误信息
```