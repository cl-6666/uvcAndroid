package com.maxvision

import android.app.Application
import android.util.Log
import com.cl.uvckit.core.utils.UvcKitWrapper

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // 设置日志文件保存路径到应用的外部文件目录下的logs文件夹
        val logPath = "${getExternalFilesDir(null)?.absolutePath ?: filesDir.absolutePath}/logs"
        Log.i("uvcdemo", "logPath: $logPath")
        // 初始化 UvcKit 日志系统
        // 参数说明：
        // - this: Application 上下文
        // - true: 启用日志开关，会自动收集设备信息（品牌、型号、系统版本、内存等）
        // - logPath: 日志文件保存路径
        // 
        // 当日志开关为 true 时，会在应用启动时自动收集以下信息：
        // 1. 应用信息：版本号、包名
        // 2. 设备信息：品牌、厂商、型号
        // 3. 系统信息：Android 版本、SDK 版本
        // 4. 硬件信息：CPU 架构、内存大小
        // 5. 运行时信息：内存使用情况、处理器数量
        // 
        // 这些信息仅保存在本地日志文件中，方便排查问题
        UvcKitWrapper.init(this, true, logPath)
    }
}