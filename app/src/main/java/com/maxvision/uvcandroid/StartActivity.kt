package com.maxvision.uvcandroid

import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.hjq.permissions.OnPermissionCallback
import com.hjq.permissions.Permission
import com.hjq.permissions.XXPermissions
import com.jiangdg.ausbc.utils.ToastUtils.show
import com.maxvision.uvcandroid.fragment.PalmLinesFragment
import com.maxvision.uvcandroid.util.navigateTo

class StartActivity : AppCompatActivity() {


    private var palmLinesFragment: PalmLinesFragment? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        permissionAcquisition()
    }

    fun oneClick(view: View) {
        navigateTo<MainActivity>()
    }

    fun manyClick(view: View) {
        palmLines()
    }



    private fun palmLines() {
        // 检查是否已经有同名 Fragment 存在
        val existingFragment = supportFragmentManager.findFragmentByTag("LogicFragment")
        if (existingFragment == null) {
            palmLinesFragment = PalmLinesFragment.newInstance("serialId")
            supportFragmentManager.beginTransaction()
                .add(R.id.cameraViewContainer, palmLinesFragment!!, "LogicFragment")
                .commit()
        }
    }


    private fun permissionAcquisition() {
        XXPermissions.with(this)
            .permission(Permission.CAMERA)
            .permission(Permission.Group.STORAGE)
            .unchecked()
            .request(object : OnPermissionCallback {
                override fun onGranted(permissions: MutableList<String>, allGranted: Boolean) {
                    if (!allGranted) {
                        show("获取部分权限成功，但部分权限未正常授予")
                        return
                    }
                }

                override fun onDenied(permissions: MutableList<String>, doNotAskAgain: Boolean) {
                    if (doNotAskAgain) {
                        show("被永久拒绝授权，请手动授予录音和日历权限")
                        // 如果是被永久拒绝就跳转到应用权限系统设置页面
                        XXPermissions.startPermissionActivity(
                            this@StartActivity,
                            permissions
                        )
                    } else {
                        show("获取相机以及存储权限失败")
                    }
                }
            })
    }
}