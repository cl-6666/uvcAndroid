package com.maxvision.uvcandroid

import android.app.Application
import com.jiangdg.ausbc.utils.Logger


/**
 * name：cl
 * date：2024/10/25
 * desc：
 */
class MyApplication : Application() {


    override fun onCreate() {
        super.onCreate()
        Logger.init(this)
    }
}