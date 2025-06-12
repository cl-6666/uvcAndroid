package com.maxvision.uvcandroid;

import android.annotation.SuppressLint;
import android.hardware.usb.UsbConfiguration;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.TextureView;

import androidx.appcompat.app.AppCompatActivity;

import com.jiangdg.usb.USBMonitor;
import com.jiangdg.utils.Size;
import com.jiangdg.uvc.IFrameCallback;
import com.jiangdg.uvc.UVCCamera;

import java.nio.ByteBuffer;
import java.util.List;

/**
 * name：cl
 * date：2025/2/11
 * desc：多路摄像头
 */
public class MultiChannelActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    TextureView mTexture, mTexture2, mTexture3, mTexture4;
    USBMonitor usbMonitor;
    UVCCamera uvcCamera, uvcCamera2, uvcCamera3, uvcCamera4;

    int time;
    Handler handler = new Handler(Looper.getMainLooper());

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multi_channel);
        mTexture = findViewById(R.id.mTexture);
        mTexture2 = findViewById(R.id.mTexture2);
        mTexture3 = findViewById(R.id.mTexture3);
        mTexture4 = findViewById(R.id.mTexture4);

        mTexture.setRotation(-90);
        mTexture2.setRotation(-90);
        mTexture3.setRotation(-90);
        mTexture4.setRotation(-90);

        usbMonitor = new USBMonitor(this, new USBMonitor.OnDeviceConnectListener() {
            @Override
            public void onAttach(final UsbDevice device) {
                Log.d(TAG, "onAttach: ==" + device.toString());
                for (int j = 0; j < device.getConfigurationCount(); j++) {
                    UsbConfiguration configuration = device.getConfiguration(j);
                    for (int k = 0; k < configuration.getInterfaceCount(); k++) {
                        UsbInterface usbInterface = configuration.getInterface(k);
                        String interfaceName = usbInterface.getName();
                        if (interfaceName != null) {
                            Log.i("TAG", "USB Interface Name: " + interfaceName);
                            switch (interfaceName) {
                                case "Iris_Camera": {
                                        handler.postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                Log.d(TAG, "run: 发送 dev=" + device.getDeviceName());
                                                usbMonitor.requestPermission(device);
                                            }
                                        }, time++ * 500);
                                    break;
                                }
//                                case "Palm Vein Collector": {
//                                        handler.postDelayed(new Runnable() {
//                                            @Override
//                                            public void run() {
//                                                Log.d(TAG, "run: 发送 dev=" + device.getDeviceName());
//                                                usbMonitor.requestPermission(device);
//                                            }
//                                        }, time++ * 500);
//                                    break;
//                                }
                            }
                        } else {
                            Log.w("TAG", "USB Interface Name is null");
                        }
                    }
                }
            }

            @Override
            public void onDetach(UsbDevice device) {

            }


            @Override
            public void onConnect(UsbDevice usbDevice, USBMonitor.UsbControlBlock ctrlBlock, boolean b) {
                //打印执行次数
                Log.d(TAG, "onConnect: ==" + usbDevice.getDeviceName());
//                new Thread(new myRunnable(ctrlBlock,0)).start();

                for (int j = 0; j < usbDevice.getConfigurationCount(); j++) {
                    UsbConfiguration configuration = usbDevice.getConfiguration(j);
                    for (int k = 0; k < configuration.getInterfaceCount(); k++) {
                        UsbInterface usbInterface = configuration.getInterface(k);
                        String interfaceName = usbInterface.getName();
                        if (interfaceName != null) {
                            switch (interfaceName) {
                                case "UVC RGB": {
                                    new Thread(new myRunnable(ctrlBlock,0)).start();
                                    break;
                                }
                                case "Palm Vein Collector": {
                                    new Thread(new myRunnable(ctrlBlock,1)).start();
                                    break;
                                }
                            }
                        } else {
                            Log.w("TAG", "USB Interface Name is null");
                        }
                    }
                }
            }

            @Override
            public void onDisconnect(UsbDevice usbDevice, USBMonitor.UsbControlBlock usbControlBlock) {
                // TODO: 2018/12/1 根据pid vid 或name判断哪个相机断开连接
            }

            @Override
            public void onCancel(UsbDevice usbDevice) {

            }
        });
        usbMonitor.register();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            usbMonitor.unregister();
            uvcCamera.stopPreview();
            uvcCamera.close();
            uvcCamera2.stopPreview();
            uvcCamera2.close();
        } catch (Exception e) {
        }
    }

    class myRunnable implements Runnable {
        USBMonitor.UsbControlBlock usbControlBlock;
        int type=0;

        public myRunnable(USBMonitor.UsbControlBlock usbControlBlock, int i) {
            this.usbControlBlock = usbControlBlock;
            this.type=i;
        }

        @Override
        public void run() {
            final UVCCamera camera = new UVCCamera();
            try {
                camera.open(usbControlBlock,type);
                Log.d(TAG, "相机成功打开：" + camera.getDeviceName());
            } catch (Exception e) {
                Log.d(TAG, "开启相机错误！！！！" + camera.getDeviceName());
                return;
            }

            List<Size> supportedSizeList = camera.getSupportedSizeList();
            if (supportedSizeList != null) {
                for (Size size : supportedSizeList) {
                    Log.d(TAG, "run: size=" + size.width + "---" + size.height);
                }
            }
            setCameraParameter(camera);
            //根据不同相机接入name  或者根据pid vid 指定相机在那个view显示
//            if (usbControlBlock.getDeviceName().contains("usb/001/004")) {
//                camera.setPreviewTexture(mTexture.getSurfaceTexture());
//            } else if (usbControlBlock.getDeviceName().contains("usb/001/003")) {
//                camera.setPreviewTexture(mTexture2.getSurfaceTexture());
//            } else if (usbControlBlock.getDeviceName().contains("usb/003/004")) {
//                camera.setPreviewTexture(mTexture3.getSurfaceTexture());
//            } else if (usbControlBlock.getDeviceName().contains("usb/003/003")) {
//                camera.setPreviewTexture(mTexture4.getSurfaceTexture());
//           // camera.setFrameCallback(iFrameCallback, UVCCamera.PIXEL_FORMAT_YUV420SP);//设置数据回调
//                uvcCamera = camera;
//            }
            if (type==0){
                if (mTexture != null) {
                    camera.setPreviewTexture(mTexture.getSurfaceTexture());
                    camera.setFrameCallback(new IFrameCallback() {
                        @Override
                        public void onFrame(ByteBuffer frame) {
//                                                Log.i("TAG", "onFrame111数据: " + frame.get());
                        }
                    }, UVCCamera.PIXEL_FORMAT_YUV420SP);//设置数据回调
                }
            }else {
                if (mTexture2 != null) {
                    camera.setPreviewTexture(mTexture2.getSurfaceTexture());
                    camera.setFrameCallback(new IFrameCallback() {
                        @Override
                        public void onFrame(ByteBuffer frame) {
//                                                Log.i("TAG", "onFrame2222数据: " + frame.get());
                        }
                    }, UVCCamera.PIXEL_FORMAT_YUV420SP);//设置数据回调
                }
            }
            camera.startPreview();
        }
    }


    //相机参数  可根据不同相机设置不同参数，如果一个OTG接入多个相机 需注意带宽和帧率设置，否则出现带宽不够导致相机不显示数据
    private void setCameraParameter(UVCCamera camera) {
        try {
            //设置预览尺寸 根据设备自行设置
            camera.setPreviewSize(640,
                    480,
                    10,
                    15,
                    UVCCamera.FRAME_FORMAT_MJPEG,
                    // UVCCamera.FRAME_FORMAT_MJPEG,//此格式设置15帧生效
                    1.4f);
            Log.d(TAG, "**设置参数成功=" + camera.getDeviceName());
        } catch (final IllegalArgumentException e) {
            Log.d(TAG, "**设置参数失败=" + camera.getDeviceName());
            return;
        }
    }
}
