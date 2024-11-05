cmake_minimum_required(VERSION 3.18.1)

project("uvc_jni")

include_directories(${CMAKE_SOURCE_DIR}/include/libuvc ${CMAKE_SOURCE_DIR}/include/rapidjson ${CMAKE_SOURCE_DIR}/include/libusb ${CMAKE_SOURCE_DIR}/include)

add_library(uvc_jni
        SHARED
        _onload.cpp Parameters.cpp serenegiant_usb_UVCCamera.cpp utilbase.cpp
        UVCButtonCallback.cpp UVCCamera.cpp UVCPreview.cpp UVCStatusCallback.cpp)

find_library(log-lib
        log)

add_library(jpegturbo
        SHARED
        IMPORTED)
set_target_properties(jpegturbo
        PROPERTIES
        IMPORTED_LOCATION
        ${CMAKE_SOURCE_DIR}/../jniLibs/${CMAKE_ANDROID_ARCH_ABI}/libjpeg-turbo1500.so)

add_library(uacaudio
        SHARED
        IMPORTED)
set_target_properties(uacaudio
        PROPERTIES
        IMPORTED_LOCATION
        ${CMAKE_SOURCE_DIR}/../jniLibs/${CMAKE_ANDROID_ARCH_ABI}/libUACAudio.so)

add_library(usb100
        SHARED
        IMPORTED)
set_target_properties(usb100
        PROPERTIES
        IMPORTED_LOCATION
        ${CMAKE_SOURCE_DIR}/../jniLibs/${CMAKE_ANDROID_ARCH_ABI}/libusb100.so)

add_library(libuvc
        SHARED
        IMPORTED)
set_target_properties(libuvc
        PROPERTIES
        IMPORTED_LOCATION
        ${CMAKE_SOURCE_DIR}/../jniLibs/${CMAKE_ANDROID_ARCH_ABI}/libuvc.so)


add_library(uvclight
        SHARED
        com_maxvision_uvcxu_UVCLight.cpp)


target_link_libraries(uvclight
        jpegturbo uacaudio usb100 libuvc android
        ${log-lib})

target_link_libraries(uvc_jni
        jpegturbo uacaudio usb100 libuvc android
        ${log-lib})