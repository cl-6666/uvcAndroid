package com.maxvision.uvcandroid.fragment

import android.content.Context
import android.graphics.SurfaceTexture
import android.hardware.usb.UsbDevice
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.TextureView
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.RelativeLayout
import androidx.fragment.app.Fragment
import com.jiangdg.ausbc.MultiCameraClient
import com.jiangdg.ausbc.callback.ICameraStateCallBack
import com.jiangdg.ausbc.callback.IDeviceConnectCallBack
import com.jiangdg.ausbc.camera.CameraUVC
import com.jiangdg.ausbc.camera.bean.CameraRequest
import com.jiangdg.ausbc.render.env.RotateType
import com.jiangdg.ausbc.utils.Logger
import com.jiangdg.ausbc.utils.SettableFuture
import com.jiangdg.ausbc.widget.AspectRatioTextureView
import com.jiangdg.ausbc.widget.IAspectRatio
import com.jiangdg.usb.USBMonitor
import com.maxvision.uvcandroid.databinding.FragmentIrisBinding
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

/**
 * name：zx
 * date：2025/1/21
 * desc：
 */
open class IrisFragment : Fragment(), ICameraStateCallBack {
    private lateinit var irisBinding: FragmentIrisBinding


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        irisBinding = FragmentIrisBinding.inflate(inflater, container, false)
        return irisBinding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        irisBinding.tv.text="红外摄像头"
        setView()

    }



    private var mCameraView: IAspectRatio? = null
    private var mCameraClient: MultiCameraClient? = null
    private val mCameraMap = hashMapOf<Int, MultiCameraClient.ICamera>()
    private var mCurrentCamera: SettableFuture<MultiCameraClient.ICamera>? = null
//    private var productName = "Iris_Camera" //Iris_Collector 红外   USB2.0 Camera 可见光 Face_Camera
    private var productName = "Palm Vein Collector"
    private val mRequestPermission: AtomicBoolean by lazy {
        AtomicBoolean(false)
    }


    private fun setView() {
        when (val cameraView = getCameraView()) {
            is TextureView -> {
                handleTextureView(cameraView)
                cameraView
            }

            is SurfaceView -> {
                handleSurfaceView(cameraView)
                cameraView
            }

            else -> {
                null
            }
        }.apply {
            mCameraView = this
            // offscreen render
            if (this == null) {
                registerMultiCamera()
                return
            }
        }.also { view ->
            getCameraViewContainer().apply {
                removeAllViews()
                addView(view, getViewLayoutParams(this))
            }
        }
    }






    override fun onDestroy() {
        super.onDestroy()
        unRegisterMultiCamera()
    }


    /**
     * Camera render view show gravity
     */
    protected open fun getGravity() = Gravity.CENTER


    /**
     * 设置摄像头
     */
    private fun getCameraRequest(): CameraRequest {
        return CameraRequest.Builder()
            .setPreviewWidth(1024)
            .setPreviewHeight(768)
            .setRenderMode(CameraRequest.RenderMode.OPENGL)
            .setDefaultRotateType(RotateType.ANGLE_0)
            .setAspectRatioShow(true)
            .setCaptureRawImage(false)
            .setRawPreviewData(true)
            .create()

    }


    private fun getCameraViewContainer(): ViewGroup {
        return irisBinding.fl
    }

    private fun getCameraView(): IAspectRatio {
        val textureView = AspectRatioTextureView(requireContext())
        textureView.setAspectRatio(
            1024,
            768
        )
        return textureView
    }

    private fun handleTextureView(textureView: TextureView) {
        textureView.surfaceTextureListener = object : TextureView.SurfaceTextureListener {
            override fun onSurfaceTextureAvailable(
                surface: SurfaceTexture,
                width: Int,
                height: Int
            ) {
                registerMultiCamera()
            }

            override fun onSurfaceTextureSizeChanged(
                surface: SurfaceTexture,
                width: Int,
                height: Int
            ) {
                surfaceSizeChanged(width, height)
            }

            override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
                unRegisterMultiCamera()
                return false
            }

            override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {
            }
        }
    }

    private fun handleSurfaceView(surfaceView: SurfaceView) {
        surfaceView.holder.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceCreated(holder: SurfaceHolder) {
                registerMultiCamera()
            }

            override fun surfaceChanged(
                holder: SurfaceHolder,
                format: Int,
                width: Int,
                height: Int
            ) {
                surfaceSizeChanged(width, height)
            }

            override fun surfaceDestroyed(holder: SurfaceHolder) {
                unRegisterMultiCamera()
            }
        })
    }


    protected fun registerMultiCamera() {
        mCameraClient = MultiCameraClient(requireContext(), object : IDeviceConnectCallBack {
            override fun onAttachDev(device: UsbDevice?) {
                device ?: return

                Logger.i("TAG",
                    "摄像头信息:" + "\nproductName:" + device.productName
//                            + "\nserialNumber:" + device.serialNumber
                            + "\ndeviceId:" + device.deviceId
                            + "\nproductId:" + device.productId
                            + "\nvendorId:" + device.vendorId
                            + "\ndeviceName:" + device.deviceName
                            + "\nmanufacturerName:" + device.manufacturerName
                            + "\nversion:" + device.version
                )

                if (productName != device.productName) {
                    return
                }

                if (mCameraMap.containsKey(device.deviceId)) {
                    return
                }
                generateCamera(requireContext(), device).apply {
                    mCameraMap[device.deviceId] = this
                }
                // Initiate permission request when device insertion is detected
                // If you want to open the specified camera, you need to override getDefaultCamera()
                if (mRequestPermission.get()) {
                    return
                }
                getDefaultCamera()?.apply {
                    if (vendorId == device.vendorId && productId == device.productId) {
                        Logger.i("zelax", "default camera pid: $productId, vid: $vendorId")
                        requestPermission(device)
                    }
                    return
                }
                requestPermission(device)
            }

            override fun onDetachDec(device: UsbDevice?) {
                mCameraMap.remove(device?.deviceId)?.apply {
                    setUsbControlBlock(null)
                }
                mRequestPermission.set(false)
                mCurrentCamera = null
            }

            override fun onConnectDev(device: UsbDevice?, ctrlBlock: USBMonitor.UsbControlBlock?) {
                device ?: return
                ctrlBlock ?: return
                mCameraMap[device.deviceId]?.apply {
                    setUsbControlBlock(ctrlBlock)
                }?.also { camera ->
                    mCurrentCamera = SettableFuture()
                    mCurrentCamera?.set(camera)
                    openCamera(mCameraView)
                    Logger.i(
                        "zelax",
                        "camera connection. pid: ${device.productId}, vid: ${device.vendorId}"
                    )
                }
            }

            override fun onDisConnectDec(
                device: UsbDevice?,
                ctrlBlock: USBMonitor.UsbControlBlock?
            ) {
                closeCamera()
                mRequestPermission.set(false)
                mCurrentCamera = null
            }

            override fun onCancelDev(device: UsbDevice?) {
                mRequestPermission.set(false)
                mCurrentCamera = null
            }
        })
        mCameraClient?.register()
    }

    /**
     * Get default camera
     *
     * @return Open camera by default, should be [UsbDevice]
     */
    protected open fun getDefaultCamera(): UsbDevice? {
        val deviceList: List<UsbDevice>? = getDeviceList()
        for (d in deviceList!!) {
//            if (DEVICES.contains(d.productName)) {
//                return d
//            }
            if (productName == d.productName) {
                return d
            }
        }
        return null
    }

    /**
     * Request permission
     *
     * @param device see [UsbDevice]
     */
    protected fun requestPermission(device: UsbDevice?) {
        mCameraClient?.requestPermission(device)
        mRequestPermission.set(true)
    }

    protected fun unRegisterMultiCamera() {
        mCameraMap.values.forEach {
            it.closeCamera()
        }
        mCameraMap.clear()
        mCameraClient?.unRegister()
        mCameraClient?.destroy()
        mCameraClient = null
    }

    private fun getDeviceList() = mCameraClient?.getDeviceList()

    protected open fun generateCamera(ctx: Context, device: UsbDevice): MultiCameraClient.ICamera {
        return CameraUVC(ctx, device,0)
    }


    protected fun openCamera(st: IAspectRatio? = null) {
        when (st) {
            is TextureView, is SurfaceView -> {
                st
            }

            else -> {
                null
            }
        }.apply {
            getCurrentCamera()?.setCameraStateCallBack(this@IrisFragment)
            getCurrentCamera()?.openCamera(this, getCameraRequest())
        }
    }

    protected fun closeCamera() {
        getCurrentCamera()?.closeCamera()
        getCurrentCamera()?.setCameraStateCallBack(null)
    }

    private fun surfaceSizeChanged(surfaceWidth: Int, surfaceHeight: Int) {
        getCurrentCamera()?.setRenderSize(surfaceWidth, surfaceHeight)
    }

    private fun getViewLayoutParams(viewGroup: ViewGroup): ViewGroup.LayoutParams {
        return when (viewGroup) {
            is FrameLayout -> {
                FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    getGravity()
                )
            }

            is LinearLayout -> {
                LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT
                ).apply {
                    gravity = getGravity()
                }
            }

            is RelativeLayout -> {
                RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.MATCH_PARENT,
                    RelativeLayout.LayoutParams.MATCH_PARENT
                ).apply {
                    when (getGravity()) {
                        Gravity.TOP -> {
                            addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE)
                        }

                        Gravity.BOTTOM -> {
                            addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE)
                        }

                        else -> {
                            addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE)
                            addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE)
                        }
                    }
                }
            }

            else -> throw IllegalArgumentException(
                "Unsupported container view, " +
                        "you can use FrameLayout or LinearLayout or RelativeLayout"
            )
        }
    }

    /**
     * Get current opened camera
     *
     * @return current camera, see [MultiCameraClient.ICamera]
     */
    private fun getCurrentCamera(): MultiCameraClient.ICamera? {
        return try {
            mCurrentCamera?.get(2, TimeUnit.SECONDS)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }


    override fun onCameraState(
        self: MultiCameraClient.ICamera,
        code: ICameraStateCallBack.State,
        msg: String?
    ) {
        when (code) {
            ICameraStateCallBack.State.OPENED -> {
                handleCameraOpened()
            }

            ICameraStateCallBack.State.CLOSED -> handleCameraClosed()
            ICameraStateCallBack.State.ERROR -> handleCameraError(msg)
        }
    }

    private fun handleCameraError(msg: String?) {
        Log.i("tag","红外摄摄像头开启失败$msg")
    }

    private fun handleCameraClosed() {
        Log.e("tag","红外摄摄像头已关闭")
    }

    private fun handleCameraOpened() {
        Log.e("tag","红外摄像头开启成功")


    }















}