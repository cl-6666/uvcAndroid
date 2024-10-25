package com.maxvision.uvcandroid.util

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

/**
 * name：cl
 * date：2024/7/4
 * desc：跳转工具类扩展函数
 */
// 无参数的页面跳转
inline fun <reified T : AppCompatActivity> AppCompatActivity.navigateTo() {
    val intent = Intent(this, T::class.java)
    startActivity(intent)
}

// 带数据的页面跳转
inline fun <reified T : AppCompatActivity> AppCompatActivity.navigateTo(vararg pairs: Pair<String, Any?>) {
    val intent = Intent(this, T::class.java)
    pairs.forEach {
        when (val value = it.second) {
            is Int -> intent.putExtra(it.first, value)
            is String -> intent.putExtra(it.first, value)
            is Boolean -> intent.putExtra(it.first, value)
            is Float -> intent.putExtra(it.first, value)
            is Double -> intent.putExtra(it.first, value)
            is Long -> intent.putExtra(it.first, value)
            is Bundle -> intent.putExtra(it.first, value)
            else -> throw IllegalArgumentException("Unsupported bundle component (${it.first} : ${value?.javaClass})")
        }
    }
    startActivity(intent)
}

// 带结果的页面跳转
inline fun <reified T : AppCompatActivity> AppCompatActivity.navigateForResult(
    requestCode: Int,
    vararg pairs: Pair<String, Any?>
) {
    val intent = Intent(this, T::class.java)
    pairs.forEach {
        when (val value = it.second) {
            is Int -> intent.putExtra(it.first, value)
            is String -> intent.putExtra(it.first, value)
            is Boolean -> intent.putExtra(it.first, value)
            is Float -> intent.putExtra(it.first, value)
            is Double -> intent.putExtra(it.first, value)
            is Long -> intent.putExtra(it.first, value)
            is Bundle -> intent.putExtra(it.first, value)
            else -> throw IllegalArgumentException("Unsupported bundle component (${it.first} : ${value?.javaClass})")
        }
    }
    startActivityForResult(intent, requestCode)
}