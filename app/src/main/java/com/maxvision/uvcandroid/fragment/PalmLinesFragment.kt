package com.maxvision.uvcandroid.fragment

import android.content.Context
import android.hardware.usb.UsbDevice
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.jiangdg.ausbc.MultiCameraClient
import com.jiangdg.ausbc.base.CameraFragment
import com.jiangdg.ausbc.callback.ICameraStateCallBack
import com.jiangdg.ausbc.callback.IPreviewDataCallBack
import com.jiangdg.ausbc.camera.CameraUVC
import com.jiangdg.ausbc.camera.bean.CameraRequest
import com.jiangdg.ausbc.render.env.RotateType
import com.jiangdg.ausbc.utils.Logger
import com.jiangdg.ausbc.widget.AspectRatioTextureView
import com.jiangdg.ausbc.widget.IAspectRatio
import com.maxvision.uvcandroid.databinding.FragmentHiddenBinding
import java.io.File

/**
 * name：cl
 * date：2024/8/30
 * desc：
 */
class PalmLinesFragment : CameraFragment() {

    private val TAG = "PalmLinesFragment"

    private val PREVIEW_WIDTH = 1920
    private val PREVIEW_HEIGHT = 1080

    private lateinit var mViewBinding: FragmentHiddenBinding
    // 定义一个变量来记录上次保存图片的时间
    private var lastSaveTime = 0L
    // 1000毫秒（1秒）
    private val saveInterval = 1500
    //文件目录
    private var storageDir: File? = null

    //采集状态
    private var collectionStatus = false


    companion object {
        private const val ARG_ID = "arg_id"
        fun newInstance(id: String): PalmLinesFragment {
            val fragment = PalmLinesFragment()
            val args = Bundle()
            args.putString(ARG_ID, id)
            fragment.arguments = args
            return fragment
        }
    }

    private var id: String = ""

    //初始化NativeLib
//    private var nativeLib: NativeLib? =null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

//        nativeLib= NativeLib()
//        val configFilePath: String = requireContext().filesDir.path+"/palmvein/config.ini"
//        Timber.tag("TAG").i("算法版本号---%s", configFilePath)
//        nativeLib?.init(requireContext(),configFilePath)
//        Log.i("TAG","算法版本号---"+nativeLib?.getAlgVersion())
    }

    fun whetherToCollect(collection: Boolean) {
        collectionStatus = collection
        // 处理收到的消息
        Logger.i(TAG, "PalmLinesFragment收到采集信息$collection")
    }


    override fun onCameraState(
        self: MultiCameraClient.ICamera,
        code: ICameraStateCallBack.State,
        msg: String?
    ) {
        when (code) {
            ICameraStateCallBack.State.OPENED -> {
                Logger.i(TAG, "打开摄像头成")
                self.addPreviewDataCallBack(object : IPreviewDataCallBack {
                    override fun onPreviewData(
                        data: ByteArray?,
                        width: Int,
                        height: Int,
                        format: IPreviewDataCallBack.DataFormat
                    ) {
                        self.addPreviewDataCallBack(object : IPreviewDataCallBack {
                            override fun onPreviewData(
                                data: ByteArray?,
                                width: Int,
                                height: Int,
                                format: IPreviewDataCallBack.DataFormat
                            ) {
                                processPreviewData(data, width, height)
                            }
                        })
                    }
                })
            }

            ICameraStateCallBack.State.CLOSED -> {
                Logger.i(TAG, "关闭UVC视频成功")
            }

            ICameraStateCallBack.State.ERROR -> {
                Logger.i(TAG, "打开UVC视频失败")
            }
        }
    }

    private fun processPreviewData(data: ByteArray?, width: Int, height: Int) {
        if (!collectionStatus || data == null) return
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastSaveTime >= saveInterval) {
            saveImage(data!!, width, height)
            lastSaveTime = currentTime // 更新上次保存时间
        }
    }

    private fun saveImage(data: ByteArray, width: Int, height: Int) {

    }

    override fun getCameraView(): IAspectRatio? {
        val aspectRatioTextureView = AspectRatioTextureView(requireContext())
        aspectRatioTextureView.setAspectRatio(PREVIEW_WIDTH, PREVIEW_HEIGHT)
        return aspectRatioTextureView
    }


    override fun initData() {

    }

    override fun getCameraViewContainer(): ViewGroup? {
        return mViewBinding.cameraViewContainer
    }

    override fun getRootView(inflater: LayoutInflater, container: ViewGroup?): View {
        mViewBinding = FragmentHiddenBinding.inflate(inflater, container, false)
        return mViewBinding.root
    }

    override fun generateCamera(ctx: Context, device: UsbDevice): MultiCameraClient.ICamera {
        Log.i("TAG", "当前的device---$device")
        return CameraUVC(ctx, device,1,24) // 继续使用现有的 CameraUVC 类
    }


    override fun getGravity(): Int = Gravity.CENTER


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