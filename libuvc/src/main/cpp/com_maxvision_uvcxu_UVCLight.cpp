//
// Created by yindong on 2024/05/15.
//

#include "com_maxvision_uvcxu_UVCLight.h"
#include <jni.h>
#include <stdio.h>
#include <libuvc/libuvc.h>
#include <libuvc_internal.h>
#include <fcntl.h>
#include <errno.h>
#include <sys/ioctl.h>
#include <linux/videodev2.h>
#include <linux/uvcvideo.h>




extern "C"
JNIEXPORT jint JNICALL
Java_com_maxvision_uvcxu_LightControl_setDistanceSettings(JNIEnv *env, jclass thisClz, jint connectionFd,
                                                             jint vid, jint pid, jstring usbfs, jint busNum, jint devNum, jint lightBrightNess) {
    uvc_context_t *ctx;
    uvc_device_t *dev;
    uvc_device_handle_t *devh;
    uvc_error_t res;

    // 初始化libuvc
    res = uvc_init2(&ctx, NULL, env->GetStringUTFChars(usbfs, NULL));
    if (res < 0) {
        LOGE("uvc_init error\n");
        return -1;
    }

    // 打开第一个UVC设备
    res = uvc_get_device_with_fd(ctx, &dev, vid, pid, NULL, connectionFd, busNum, devNum);
    if (res < 0) {
        LOGE("uvc_find_device error\n");
        uvc_exit(ctx);
        return -1;
    }

    // 打开设备句柄
    res = uvc_open(dev, &devh, 0);
    if (res < 0) {
        LOGE("uvc_open error\n");
        uvc_unref_device(dev);
        uvc_exit(ctx);
        return -1;
    }

    const uvc_extension_unit *xus = uvc_get_extension_units(devh);

    do {
        uint8_t uintid = xus->bUnitID;
        if (uintid == 0x06) {
            uint8_t unit_id = uintid;
            uint8_t control_id = 1;
//				int value = 0x01640000; //大端
            uint8_t light = lightBrightNess & 0x000000FF;
//				uint32_t value = 0x09XX0000;
            uint32_t value = 0x09 + (light << 8);
            int ret = uvc_set_ctrl(devh, unit_id, control_id, &value, sizeof(value));
            LOGE("uvc_set_ctrl ret=%d sizeof(value)=%d", ret, sizeof(value));
            break;
        }
        xus = xus->next;
    } while(xus);

    // 关闭设备句柄
    uvc_close(devh);
    // 释放设备
    uvc_unref_device(dev);
    // 退出libuvc
    uvc_exit(ctx);
    return 0;
}


extern "C"
JNIEXPORT jint JNICALL
Java_com_maxvision_uvcxu_LightControl_setFillLightBrightness(JNIEnv *env, jclass thisClz, jint connectionFd,
                                                             jint vid, jint pid, jstring usbfs, jint busNum, jint devNum, jint lightBrightNess) {
    uvc_context_t *ctx;
    uvc_device_t *dev;
    uvc_device_handle_t *devh;
    uvc_error_t res;

    // 初始化libuvc
    res = uvc_init2(&ctx, NULL, env->GetStringUTFChars(usbfs, NULL));
    if (res < 0) {
        LOGE("uvc_init error\n");
        return -1;
    }

    // 打开第一个UVC设备
    res = uvc_get_device_with_fd(ctx, &dev, vid, pid, NULL, connectionFd, busNum, devNum);
    if (res < 0) {
        LOGE("uvc_find_device error\n");
        uvc_exit(ctx);
        return -1;
    }

    // 打开设备句柄
    res = uvc_open(dev, &devh, 0);
    if (res < 0) {
        LOGE("uvc_open error\n");
        uvc_unref_device(dev);
        uvc_exit(ctx);
        return -1;
    }

    const uvc_extension_unit *xus = uvc_get_extension_units(devh);

    do {
        uint8_t uintid = xus->bUnitID;
        if (uintid == 0x06) {
            uint8_t unit_id = uintid;
            uint8_t control_id = 1;
//				int value = 0x01640000; //大端
            uint8_t light = lightBrightNess & 0x000000FF;
//				uint32_t value = 0x00003201; //小端，需要采用小端  0x16 表示亮度（0x64最亮 0x00熄灭） 0x01 标识哪种灯
            uint32_t value = 0x01 + (light << 8);
            int ret = uvc_set_ctrl(devh, unit_id, control_id, &value, sizeof(value));
            LOGE("uvc_set_ctrl ret=%d sizeof(value)=%d", ret, sizeof(value));
            break;
        }
        xus = xus->next;
    } while(xus);

    // 关闭设备句柄
    uvc_close(devh);
    // 释放设备
    uvc_unref_device(dev);
    // 退出libuvc
    uvc_exit(ctx);
    return 0;
}


//int xioctl(int fd, int request, void *arg) {
//    int r;
//    do {
//        r = ioctl(fd, request, arg);
//        if (r < 0) {
//            LOGD("ioctl failed: %d %s", errno, strerror(errno));
//        }
//    } while (-1 == r && EINTR == errno);
//    return r;
//}

//extern "C"
//JNIEXPORT jint JNICALL
//Java_com_maxvision_uvctest_UVCCameraTest_lightControlV4l2(JNIEnv *env, jobject thiz,
//                                                          jint connectFd, jint uint_id,
//                                                          jint selector, jbyteArray data,
//                                                          jint direction) {
////    const char *device_path = env->GetStringUTFChars(dev_path, 0);
////    int fd = open(device_path, O_RDWR);
////    if (fd < 0) {
////        LOGD("Cannot open device: %s", device_path);
////        env->ReleaseStringUTFChars(dev_path, device_path);
////        return -1;
////    }
//
//    int fd = connectFd;
//    struct uvc_xu_control_query query;
//    jbyte *data_ptr = env->GetByteArrayElements(data, NULL);
//    jsize data_len = env->GetArrayLength(data);
//
//    query.unit = uint_id;
//    query.selector = selector;
//    query.query = (direction == 0) ? UVC_SET_CUR : UVC_GET_CUR;
//    query.size = data_len;
//    query.data = (unsigned char *)data_ptr;
//
//    fd = dup(fd);
//
//    int ret = xioctl(fd, UVCIOC_CTRL_QUERY, &query);
//    if (ret < 0) {
//        LOGD("UVC XU control transfer failed: %d", errno);
//    } else if (ret == 0) {
//        LOGD("UVC XU control transfer suc");
//    }
//
//    env->ReleaseByteArrayElements(data, data_ptr, 0);
////    env->ReleaseStringUTFChars(dev_path, device_path);
////    close(fd);
//    return ret;
//}


extern "C"
JNIEXPORT jint JNICALL
Java_com_maxvision_uvcxu_LightControl_setInfraredLightBrightness(JNIEnv *env, jclass clazz,
                                                                 jint connectionFd, jint vid,
                                                                 jint pid, jstring usbFs,
                                                                 jint busNum, jint devNum,
                                                                 jint lightBrightness) {
    uvc_context_t *ctx;
    uvc_device_t *dev;
    uvc_device_handle_t *devh;
    uvc_error_t res;

    // 初始化libuvc
    res = uvc_init2(&ctx, NULL, env->GetStringUTFChars(usbFs, NULL));
    if (res < 0) {
        LOGE("uvc_init error\n");
        return -1;
    }

    // 打开第一个UVC设备
    res = uvc_get_device_with_fd(ctx, &dev, vid, pid, NULL, connectionFd, busNum, devNum);
    if (res < 0) {
        LOGE("uvc_find_device error\n");
        uvc_exit(ctx);
        return -1;
    }

    // 打开设备句柄
    res = uvc_open(dev, &devh, 0);
    if (res < 0) {
        LOGE("uvc_open error\n");
        uvc_unref_device(dev);
        uvc_exit(ctx);
        return -1;
    }

    const uvc_extension_unit *xus = uvc_get_extension_units(devh);

    do {
        uint8_t uintid = xus->bUnitID;
        if (uintid == 0x06) {
            uint8_t unit_id = uintid;
            uint8_t control_id = 1;
//				int value = 0x01640000; //大端
            uint8_t light = lightBrightness & 0x000000FF;
//				uint32_t value = 0x00003201; //小端，需要采用小端  0x16 表示亮度（0x64最亮 0x00熄灭） 0x01 标识哪种灯
            uint32_t value0 = 0x02 + (light << 8);
            int ret0 = uvc_set_ctrl(devh, unit_id, control_id, &value0, sizeof(value0));
            LOGE("uvc_set_ctrl ret0=%d sizeof(value)=%d", ret0, sizeof(value0));

            uint32_t value1 = 0x03 + (light << 8);
            int ret1 = uvc_set_ctrl(devh, unit_id, control_id, &value1, sizeof(value1));
            LOGE("uvc_set_ctrl ret1=%d sizeof(value)=%d", ret1, sizeof(value1));
            break;
        }
        xus = xus->next;
    } while(xus);

    // 关闭设备句柄
    uvc_close(devh);
    // 释放设备
    uvc_unref_device(dev);
    // 退出libuvc
    uvc_exit(ctx);
    return 0;
}



extern "C"
JNIEXPORT jint JNICALL
Java_com_maxvision_uvcxu_LightControl_setRotationAngle(JNIEnv *env, jclass clazz,
                                                                 jint connectionFd, jint vid,
                                                                 jint pid, jstring usbFs,
                                                                 jint busNum, jint devNum,
                                                                 jint lightBrightness) {
    uvc_context_t *ctx;
    uvc_device_t *dev;
    uvc_device_handle_t *devh;
    uvc_error_t res;

    // 初始化libuvc
    res = uvc_init2(&ctx, NULL, env->GetStringUTFChars(usbFs, NULL));
    if (res < 0) {
        LOGE("uvc_init error\n");
        return -1;
    }

    // 打开第一个UVC设备
    res = uvc_get_device_with_fd(ctx, &dev, vid, pid, NULL, connectionFd, busNum, devNum);
    if (res < 0) {
        LOGE("uvc_find_device error\n");
        uvc_exit(ctx);
        return -1;
    }

    // 打开设备句柄
    res = uvc_open(dev, &devh, 0);
    if (res < 0) {
        LOGE("uvc_open error\n");
        uvc_unref_device(dev);
        uvc_exit(ctx);
        return -1;
    }

    const uvc_extension_unit *xus = uvc_get_extension_units(devh);

    do {
        uint8_t uintid = xus->bUnitID;
        if (uintid == 0x06) {
            uint8_t unit_id = uintid;
            uint8_t control_id = 1;
            uint8_t light = lightBrightness & 0x000000FF;
            uint32_t value = 0x10 + (light << 8);
            int ret = uvc_set_ctrl(devh, unit_id, control_id, &value, sizeof(value));
            LOGE("uvc_set_ctrl ret=%d sizeof(value)=%d", ret, sizeof(value));
            break;
        }
        xus = xus->next;
    } while(xus);


    // 关闭设备句柄
    uvc_close(devh);
    // 释放设备
    uvc_unref_device(dev);
    // 退出libuvc
    uvc_exit(ctx);
    return 0;
}