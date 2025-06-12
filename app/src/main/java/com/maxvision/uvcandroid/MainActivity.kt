package com.maxvision.uvcandroid

import android.content.Context
import android.hardware.usb.UsbDevice
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.enableEdgeToEdge
import androidx.databinding.DataBindingUtil
import com.jiangdg.ausbc.MultiCameraClient
import com.jiangdg.ausbc.base.CameraActivity
import com.jiangdg.ausbc.callback.ICameraStateCallBack
import com.jiangdg.ausbc.callback.IPreviewDataCallBack
import com.jiangdg.ausbc.camera.CameraUVC
import com.jiangdg.ausbc.camera.bean.CameraRequest
import com.jiangdg.ausbc.render.env.RotateType
import com.jiangdg.ausbc.utils.Logger
import com.jiangdg.ausbc.widget.AspectRatioTextureView
import com.jiangdg.ausbc.widget.AspectRatioTextureView2
import com.jiangdg.ausbc.widget.IAspectRatio
import com.maxvision.uvcandroid.databinding.ActivityMainBinding

class MainActivity :  CameraActivity() {


    val PREVIEW_WIDTH = 1280
    val PREVIEW_HEIGHT = 720

    private lateinit var mViewBinding: ActivityMainBinding

    private val mDeviceInformation = arrayOf("Palm Vein Collector", "UVC RGB", "LRCP Technology Co., Ltd.", "LRCP USB01")




    override fun getCameraView(): IAspectRatio? {
        val aspectRatioTextureView = AspectRatioTextureView2(this)
//        aspectRatioTextureView.setRotationAngle(180)
        aspectRatioTextureView.setAspectRatio(PREVIEW_WIDTH, PREVIEW_HEIGHT)
        return aspectRatioTextureView
    }

    override fun getCameraViewContainer(): ViewGroup? {
        return mViewBinding.cameraViewContainer
    }

    override fun initData() {
        super.initData()
    }
    override fun initView() {
        super.initView()
        // 设置 Toolbar
        setSupportActionBar(mViewBinding.toolbar)
        // 显示返回按钮
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        // 处理 Toolbar 的点击事件
        mViewBinding.toolbar.setNavigationOnClickListener {
            // 返回按钮的点击事件
            finish() // 关闭当前 Activity
        }
    }


    override fun getRootView(layoutInflater: LayoutInflater): View? {
        mViewBinding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        return mViewBinding.root
    }

    override fun onCameraState(
        self: MultiCameraClient.ICamera,
        code: ICameraStateCallBack.State,
        msg: String?
    ) {
        when (code) {
            ICameraStateCallBack.State.OPENED -> {
                Logger.i("Camera", "打开成功")
                self.addPreviewDataCallBack(object : IPreviewDataCallBack {
                    override fun onPreviewData(
                        data: ByteArray?,
                        width: Int,
                        height: Int,
                        format: IPreviewDataCallBack.DataFormat
                    ) {
//                       Log.i("TAG","当前的视频流："+data?.size)
                    }
                })
            }

            ICameraStateCallBack.State.CLOSED -> {
                Logger.i("Camera", "关闭成功")
            }

            ICameraStateCallBack.State.ERROR -> {
                Logger.e("Camera", "打开失败：$msg")
            }
        }
    }


    override fun getGravity(): Int = Gravity.CENTER

    override fun generateCamera(ctx: Context, device: UsbDevice): MultiCameraClient.ICamera {
        return CameraUVC(ctx, device,1)
    }

    override fun getDefaultCamera(): UsbDevice? {
        val creatureType = 0
        var names: Array<String> = arrayOf()
        when (creatureType) {
            0 -> {
                names = mDeviceInformation
            }
        }
        val deviceList = getDeviceList()
        deviceList?.forEach {
            if (names.contains(it.productName)) {
                return it
            }
        }
        return null
    }


    override fun getCameraRequest(): CameraRequest {
        return CameraRequest.Builder()
            .setPreviewWidth(PREVIEW_WIDTH)
            .setPreviewHeight(PREVIEW_HEIGHT)
            .setRenderMode(CameraRequest.RenderMode.OPENGL)
            .setDefaultRotateType(RotateType.ANGLE_0)
            .setAudioSource(CameraRequest.AudioSource.SOURCE_SYS_MIC)
            .setAspectRatioShow(true)
            .setCaptureRawImage(false)
            .setRawPreviewData(true)
            .create()
    }
}