package com.maxvision.uvcandroid

import android.content.Context
import android.hardware.usb.UsbDevice
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.enableEdgeToEdge
import androidx.databinding.DataBindingUtil
import com.jiangdg.ausbc.MultiCameraClient
import com.jiangdg.ausbc.base.CameraActivity
import com.jiangdg.ausbc.callback.ICameraStateCallBack
import com.jiangdg.ausbc.camera.CameraUVC
import com.jiangdg.ausbc.camera.bean.CameraRequest
import com.jiangdg.ausbc.render.env.RotateType
import com.jiangdg.ausbc.utils.Logger
import com.jiangdg.ausbc.widget.AspectRatioTextureView
import com.jiangdg.ausbc.widget.IAspectRatio
import com.maxvision.uvcandroid.databinding.ActivityMainBinding

class MainActivity :  CameraActivity() {


    private val PREVIEW_WIDTH = 1920
    private val PREVIEW_HEIGHT = 1080

    private lateinit var mViewBinding: ActivityMainBinding

    private val mDeviceInformation = arrayOf("Palm Vein Collector", "UVC", "LRCP Technology Co., Ltd.", "LRCP USB01")




    override fun getCameraView(): IAspectRatio? {
        val aspectRatioTextureView = AspectRatioTextureView(this)
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
        enableEdgeToEdge()
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
        return CameraUVC(ctx, device)
    }

    override fun getDefaultCamera(): UsbDevice? {
        val creatureType = 1
        var names: Array<String> = arrayOf()
        when (creatureType) {
            1 -> {
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