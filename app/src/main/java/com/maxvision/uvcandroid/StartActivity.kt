package com.maxvision.uvcandroid

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.hjq.permissions.OnPermissionCallback
import com.hjq.permissions.Permission
import com.hjq.permissions.XXPermissions
import com.jiangdg.ausbc.utils.ToastUtils.show
import com.maxvision.uvcandroid.util.navigateTo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class StartActivity : AppCompatActivity() {



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


    private fun permissionAcquisition() {
        XXPermissions.with(this)
            .permission(Permission.CAMERA)
            .permission(Permission.MANAGE_EXTERNAL_STORAGE)
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
                    } else {
                        show("获取相机以及存储权限失败")
                    }
                }
            })
    }

    fun oneClick(view: View) {
        navigateTo<MainActivity>()
//        navigateTo<MultiChannelActivity>()
    }

    fun manyClick(view: View) {
        lifecycleScope.launch (Dispatchers.Main){
//            val transaction = supportFragmentManager.beginTransaction()
//            transaction.add(R.id.preview, faceFragment)
//            transacxtion.commit()
//            withContext(Dispatchers.IO){
//                delay(1000)
//            }
//            val transaction1 = supportFragmentManager.beginTransaction()
//            transaction1.add(R.id.preview2, irisFragment)
//            transaction1.commit()

//            val transaction1 = supportFragmentManager.beginTransaction()
//            transaction1.add(R.id.preview3, DemoMultiCameraFragment())
//            transaction1.commit()

        }


    }

}