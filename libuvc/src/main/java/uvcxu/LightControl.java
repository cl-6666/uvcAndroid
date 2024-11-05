package uvcxu;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * name：yindong
 * date：2024/05/15
 * desc：基于uvc xu扩展协议的指令灯光操作
 */
public class LightControl {
    static {
        System.loadLibrary("uvclight");
    }

    private static final String DEFAULT_USBFS = "/dev/bus/usb";
    private static final int A2_DEVICE_VENDOR_ID = 8711;
    private static final int A2_DEVICE_PRODUCT_ID = 24;
    private static final String ACTION_USB_PERMISSION = "com.maxvision.USB_PERMISSION.light";
    private static PendingIntent permissionIntent;
    private OnDeviceStateCallback onDeviceStateCallback;

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            UsbManager usbManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
            final UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
            final boolean usbPermissionGrant = intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false);
            if (ACTION_USB_PERMISSION.equals(action)) {
                Log.e("LightControl", "com.maxvision.USB_PERMISSION.light");
                if (usbPermissionGrant) {
                    if (onDeviceStateCallback != null) {
                        onDeviceStateCallback.onDeviceConnect(device);
                    }
                }else {
                }
            }else if (UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action)) {
                Log.e("LightControl", "ACTION_USB_DEVICE_ATTACHED");
                if (usbPermissionGrant) {
                }else {
                    usbManager.requestPermission(device, permissionIntent);
                }
            }else if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
                Log.e("LightControl", "ACTION_USB_DEVICE_DETACHED");
            }

        }
    };


    /**
     * 从usb设备列表中选出A2设备并返回
     * @param context
     * @return
     */
    private UsbDevice selectA2UsbDevice(Context context) {
        UsbManager usbManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
        HashMap<String, UsbDevice> deviceList = usbManager.getDeviceList();
        Set<Map.Entry<String, UsbDevice>> entries = deviceList.entrySet();
        for (Map.Entry<String, UsbDevice> entry: entries) {
            String key = entry.getKey();
            UsbDevice usbDevice = entry.getValue();
            int productId = usbDevice.getProductId();
            int vendorId = usbDevice.getVendorId();
            Log.e("USB", "deviceName:"+ usbDevice.getDeviceName()+" key:"+key+" productId:"+ productId +" vendorId:"+vendorId);
            if (usbDevice.getVendorId() == A2_DEVICE_VENDOR_ID && usbDevice.getProductId() == A2_DEVICE_PRODUCT_ID) {
                return usbDevice;
            }
        }
        return null;
    }

    /**
     * A2设备usb权限校验
     * @param context
     * @return
     * @throws Exception
     */
    private UsbDevice checkA2UsbDevicePermission(Context context) throws Exception {
        UsbManager usbManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
        UsbDevice usbDevice = selectA2UsbDevice(context);
        if (usbDevice == null) {
            throw new Exception("未找到A2设备");
        }
        if (!usbManager.hasPermission(usbDevice)) {
            usbManager.requestPermission(usbDevice, permissionIntent);
        }
        return usbDevice;
    }

    /**
     * 设置旋转方向
     */
    public void setA2RotationAngle(Context context,int lightBrightness) {
        try {
            UsbManager usbManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
            UsbDevice usbDevice = checkA2UsbDevicePermission(context);
            if (usbManager.hasPermission(usbDevice)) {
                UsbDeviceConnection connection = usbManager.openDevice(usbDevice);
                if (connection != null) {
                    int fileDescriptor = connection.getFileDescriptor();
                    String name = usbDevice.getDeviceName();
                    final String[] v = !TextUtils.isEmpty(name) ? name.split("/") : null;
                    int busnum = 0;
                    int devnum = 0;
                    if (v != null) {
                        busnum = Integer.parseInt(v[v.length-2]);
                        devnum = Integer.parseInt(v[v.length-1]);
                    }
                    setRotationAngle(fileDescriptor, usbDevice.getVendorId(), usbDevice.getProductId(), getUSBFSName(usbDevice), busnum, devnum, lightBrightness);
                    connection.close();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 设置A2测距方法
     */
    public void setA2DistanceSettings(Context context,int lightBrightness) {
        try {
            UsbManager usbManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
            UsbDevice usbDevice = checkA2UsbDevicePermission(context);
            if (usbManager.hasPermission(usbDevice)) {
                UsbDeviceConnection connection = usbManager.openDevice(usbDevice);
                if (connection != null) {
                    int fileDescriptor = connection.getFileDescriptor();
                    String name = usbDevice.getDeviceName();
                    final String[] v = !TextUtils.isEmpty(name) ? name.split("/") : null;
                    int busnum = 0;
                    int devnum = 0;
                    if (v != null) {
                        busnum = Integer.parseInt(v[v.length-2]);
                        devnum = Integer.parseInt(v[v.length-1]);
                    }
                    setDistanceSettings(fileDescriptor, usbDevice.getVendorId(), usbDevice.getProductId(), getUSBFSName(usbDevice), busnum, devnum, lightBrightness);
                    connection.close();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
   }


    /**
     * 设置A2补光灯亮度
     * @param context
     * @param lightBrightness
     */
    public void setA2FillLightBrightness(Context context, int lightBrightness) {
        try {
            UsbManager usbManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
            UsbDevice usbDevice = checkA2UsbDevicePermission(context);
            if (usbManager.hasPermission(usbDevice)) {
                UsbDeviceConnection connection = usbManager.openDevice(usbDevice);
                if (connection != null) {
                    int fileDescriptor = connection.getFileDescriptor();
                    String name = usbDevice.getDeviceName();
                    final String[] v = !TextUtils.isEmpty(name) ? name.split("/") : null;
                    int busnum = 0;
                    int devnum = 0;
                    if (v != null) {
                        busnum = Integer.parseInt(v[v.length-2]);
                        devnum = Integer.parseInt(v[v.length-1]);
                    }
                    setFillLightBrightness(fileDescriptor, usbDevice.getVendorId(), usbDevice.getProductId(), getUSBFSName(usbDevice), busnum, devnum, lightBrightness);
                    connection.close();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 设置A2红外灯亮度
     * @param context
     * @param lightBrightness
     */
    public void setA2InfraredLightBrightness(Context context, int lightBrightness) {
        try {
            UsbManager usbManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
            UsbDevice usbDevice = checkA2UsbDevicePermission(context);
            if (usbManager.hasPermission(usbDevice)) {
                UsbDeviceConnection connection = usbManager.openDevice(usbDevice);
                if (connection != null) {
                    int fileDescriptor = connection.getFileDescriptor();
                    String name = usbDevice.getDeviceName();
                    final String[] v = !TextUtils.isEmpty(name) ? name.split("/") : null;
                    int busnum = 0;
                    int devnum = 0;
                    if (v != null) {
                        busnum = Integer.parseInt(v[v.length-2]);
                        devnum = Integer.parseInt(v[v.length-1]);
                    }
                    setInfraredLightBrightness(fileDescriptor, usbDevice.getVendorId(), usbDevice.getProductId(), getUSBFSName(usbDevice), busnum, devnum, lightBrightness);
                    connection.close();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void register(Context context, OnDeviceStateCallback callback) {
        if (permissionIntent == null) {
            if (Build.VERSION.SDK_INT >= 31) {
                permissionIntent = PendingIntent.getBroadcast(context, 0, new Intent(ACTION_USB_PERMISSION), PendingIntent.FLAG_IMMUTABLE);
            } else {
                permissionIntent = PendingIntent.getBroadcast(context, 0, new Intent(ACTION_USB_PERMISSION), 0);
            }
            final IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
            filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
            filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
            context.registerReceiver(receiver, filter);
        }
        onDeviceStateCallback = callback;
    }

    public void unRegister(Context context) {
        context.unregisterReceiver(receiver);
        permissionIntent = null;
        onDeviceStateCallback = null;
    }

    private static native int setDistanceSettings(int connectionFd, int vid, int pid, String usbFs, int busNum, int devNum, int lightBrightness);
    private static native int setFillLightBrightness(int connectionFd, int vid, int pid, String usbFs, int busNum, int devNum, int lightBrightness);
    private static native int setInfraredLightBrightness(int connectionFd, int vid, int pid, String usbFs, int busNum, int devNum, int lightBrightness);
    private static native int setRotationAngle(int connectionFd, int vid, int pid, String usbFs, int busNum, int devNum, int lightBrightness);


    private static final String getUSBFSName(final UsbDevice usbDevice) {
        String result = null;
        final String name = usbDevice.getDeviceName();
        final String[] v = !TextUtils.isEmpty(name) ? name.split("/") : null;
        if ((v != null) && (v.length > 2)) {
            final StringBuilder sb = new StringBuilder(v[0]);
            for (int i = 1; i < v.length - 2; i++)
                sb.append("/").append(v[i]);
            result = sb.toString();
        }
        if (TextUtils.isEmpty(result)) {
            Log.e("USB", "failed to get USBFS path, try to use default path:" + name);
            result = DEFAULT_USBFS;
        }
        return result;
    }

    public interface OnDeviceStateCallback {
        void onDeviceConnect(UsbDevice usbDevice);
    }

}
