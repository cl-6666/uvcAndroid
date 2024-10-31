
介绍
-------

- Support opening multi-road camera;
- Support opening uvc camera on Android 4.4+;
- Support previewing 480p、720p、1080p，etc;
- Support adding effects with OpenGL ES 2.0;
- Support capture photo(`.jpg`)、viedo(`.mp4`/`.h264`/`yuv`) and audio(`pcm`/`mp3`/`aac`)
- Support rotating camera view;
- Support offscreen rendering;
- Support recording media along with acquiring YUV/RGBA/PCM/H.264/AAC stream, you can push it to your media server;
- Support acquiring all resolutions and usb devices, etc;
- Support acquiring system or device mic(UAC) audio data;
- Support armeabi-v7a, arm64-v8a, x86 & x86_64;

接入说明
-------

- **Get AUSBC**

首先，在你的根目录 **build.gradle** 或 **settings.gradle** 的 repositories 末尾添加它

```groovy
allprojects {
    repositories {
        google()
        jcenter()
        maven { url "https://jitpack.io" }
    }
}
``` 
```groovy
dependencies {
   implementation 'com.github.cl-6666.uvcAndroid:libausbc:v1.0.0'
}
```

- **简单使用**

只需要让你的Fragment或者Activity实现**Camera Fragment**或者**Camera Activity**即可

```kotlin
class DemoFragment: CameraFragment() {
    private var mViewBinding: FragmentUvcBinding? = null

    override fun getRootView(inflater: LayoutInflater, container: ViewGroup?): View? {
        if (mViewBinding == null) {
            mViewBinding = FragmentUvcBinding.inflate(inflater, container, false)
        }
        return mViewBinding?.root
    }

    // if you want offscreen render
    // please return null
    override fun getCameraView(): IAspectRatio? {
        return mViewBinding?.tvCameraRender
    }

    // if you want offscreen render
    // please return null, the same as getCameraView()
    override fun getCameraViewContainer(): ViewGroup? {
        return mViewBinding?.container
    }

    // camera open status callback
    override fun onCameraState(self: ICamera, 
                               code: ICameraStateCallBack.State,
                               msg: String?) {
        when (code) {
            ICameraStateCallBack.State.OPENED -> handleCameraOpened()
            ICameraStateCallBack.State.CLOSED -> handleCameraClosed()
            ICameraStateCallBack.State.ERROR -> handleCameraError()
        }
    }
    
    override fun getGravity(): Int = Gravity.TOP
}
```

`getRootView()`/`getCameraViewContainer()`/
`getCameraView()`，也就是fragment的root view、texture或者surface view以及它的container。当然，跟**CameraActivity**一样，现在你就能看到uvc相机的预览了。

- **高级用法**

如果您想要一些自定义配置，您可以在**DemoFragment**中这样做：

```kotlin
// 如果您想打开默认相机
// 请覆盖 getDefaultCamera()
override fun getDefaultCamera(): UsbDevice? {
    return super.getDefaultCamera()
}

// 如果您想自定义相机请求
// 请覆盖 getCameraRequest()
override fun getCameraRequest(): CameraRequest {
    return CameraRequest.Builder()
    .setPreviewWidth(1280) // camera preview width
    .setPreviewHeight(720) // camera preview height
    .setRenderMode(CameraRequest.RenderMode.OPENGL) // camera render mode
    .setDefaultRotateType(RotateType.ANGLE_0) // rotate camera image when opengl mode
    .setAudioSource(CameraRequest.AudioSource.SOURCE_AUTO) // set audio source
    .setAspectRatioShow(true) // aspect render,default is true
    .setCaptureRawImage(false) // capture raw image picture when opengl mode
    .setRawPreviewData(false)  // preview raw image when opengl mode
    .create()
}


```

当然，你也可以捕获**jpg**图像或**mp4**视频或**mp3**音频文件，并通过调用这些方法更新分辨率或不同的uvc摄像头并获取**H264/AAC/YUV/PCM**流：

```kotlin
// 捕获 jpg 图像
captureImage(callBack, savePath)
// 捕获 mp4 视频
captureVideoStart(callBack, path, durationInSec)
captureVideoStop()
// 仅捕获流（H.264/AAC）
captureStreamStart()
captureStreamStop()
// 捕获 mp4 视频
captureVideoStart(callBack, path, durationInSec)
captureVideoStop()
// 捕获 mp3 音频
captureAudioStart(callBack, path)
captureAudioStop()
// 实时播放麦克风
startPlayMic(callBack)
stopPlayMic()
// 旋转相机
// 基于打开 opengl es
setRotateType(type)
// 切换不同的相机
switchCamera(UsbDevice)
// 更新解决方案
updateResolution(width, height)
// 获取所有预览尺寸
getAllPreviewSizes(aspectRatio)
// 获取编码数据（H.264或AAC）
addEncodeDataCallBack(callBack)
// 获取原始数据（NV21 或 RGBA）
setPreviewDataCallBack(callBack)
// 获取当前相机
getCurrentCamera()
// 获取当前相机状态
isCameraOpened()
// 获取所有相机设备列表
getDeviceList()
```

相机配置

```kotlin
setZoom(0)
setSharpness(0)
setHue(0)
setSaturation(0)
setContrast(0)
setGamma(0)
setGain(0)
setAutoWhiteBalance(true)
setAutoFocus(true)
// send custom command to camera
sendCameraCommand(command)
```

为了实现更高级的功能，你甚至可以为你的相机添加一些**滤镜**。该库提供了一些默认的滤镜，例如**EffectBlackWhite**、**EffectSoul**和**EffectZoom**，未来还会添加更多的滤镜。当然​​，你也可以通过扩展**AbstractEffect**来实现自己的滤镜。例如
```kotlin
// 首先，扩展 AbstractEffect
class EffectBlackWhite(ctx: Context) : AbstractEffect(ctx) {

    override fun getId(): Int = ID

    override fun getClassifyId(): Int = CameraEffect.CLASSIFY_ID_FILTER

    override fun getVertexSourceId(): Int = R.raw.base_vertex

    override fun getFragmentSourceId(): Int = R.raw.effect_blackw_fragment

    companion object {
        const val ID = 100
    }
}

// 二、添加、更新或删除过滤器
addRenderEffect(effect)
removeRenderEffect(effect)
updateRenderEffect(classifyId, effect)
```

最后，如果你想实现流媒体，也许**IPusher**和**AusbcPusher**可以帮助你。

- 开启多路摄像头
如果你想开启多路摄像头，你可以让你的Fragment或者Activity继承**MultiCameraFragment**或者**MultiCameraActivity**。这样做就可以了，更多细节请查阅**DemoMultiCameraFragment**：
```kotlin
class DemoMultiCameraFragment : MultiCameraFragment(), ICameraStateCallBack {

    // 连接相机
    override fun onCameraAttached(camera: MultiCameraClient.Camera) {}
    
	// 相机被拆下
    override fun onCameraDetached(camera: MultiCameraClient.Camera) {}

    //连接相机
    override fun onCameraConnected(camera: MultiCameraClient.Camera) {
  		camera.openCamera(textureView, getCameraRequest())
        camera.setCameraStateCallBack(this)
    }

    // 相机断开连接
    override fun onCameraDisConnected(camera: MultiCameraClient.Camera) {
         camrea.closeCamera()
    }

    // 相机打开或关闭或错误
    override fun onCameraState(
        self: MultiCameraClient.Camera,
        code: ICameraStateCallBack.State,
        msg: String?) {
		when (code) {
            ICameraStateCallBack.State.OPENED -> handleCameraOpened()
            ICameraStateCallBack.State.CLOSED -> handleCameraClosed()
            ICameraStateCallBack.State.ERROR -> handleCameraError()
        }
    }

    override fun getRootView(inflater: LayoutInflater, container: ViewGroup?): View {
        return rootView
    }
    
// [可选]
// 如果您想打开指定的相机，
// 您需要让 isAutoRequestPermission() 为 false。
// 然后您需要在自己的 Fragment/Activity 中调用 requestPermission(device)
// 当 onAttachDev() 被调用时，默认为 true。
    protected fun isAutoRequestPermission() = true
}
```
