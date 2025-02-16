#include <jni.h>
#include <string>
#include <android/bitmap.h>
#include "stackblur.h"


extern "C"
JNIEXPORT jobject JNICALL

Java_com_lts360_compose_ui_chat_ChatUtilNativeBaseActivity_createThumbnail(JNIEnv *env, jclass obj, jobject bitmapIn) {
    AndroidBitmapInfo infoIn;
    void *pixels;

    // Get image info
    if (AndroidBitmap_getInfo(env, bitmapIn, &infoIn) != ANDROID_BITMAP_RESULT_SUCCESS) {
        return env->NewStringUTF("failed");
    }

    // Check image
    if (infoIn.format != ANDROID_BITMAP_FORMAT_RGBA_8888 &&
        infoIn.format != ANDROID_BITMAP_FORMAT_RGB_565) {
        return env->NewStringUTF("Only support ANDROID_BITMAP_FORMAT_RGBA_8888 and ANDROID_BITMAP_FORMAT_RGB_565");
    }

    // Lock all images
    if (AndroidBitmap_lockPixels(env, bitmapIn, &pixels) != ANDROID_BITMAP_RESULT_SUCCESS) {
        return env->NewStringUTF("AndroidBitmap_lockPixels failed!");
    }
    // height width
    int h = infoIn.height;
    int w = infoIn.width;
    int r = 50;

    // Start
    if (infoIn.format == ANDROID_BITMAP_FORMAT_RGBA_8888) {
        pixels = blur_ARGB_8888((int *) pixels, w, h, r);
    } else if (infoIn.format == ANDROID_BITMAP_FORMAT_RGB_565) {
        pixels = blur_RGB_565((short *) pixels, w, h, r);
    }

    // End

    // Unlocks everything
    AndroidBitmap_unlockPixels(env, bitmapIn);

    return bitmapIn;
}