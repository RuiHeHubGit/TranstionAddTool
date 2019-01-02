/* DO NOT EDIT THIS FILE - it is machine generated */
#include "jni.h"
/* Header for class com_ea_translatetool_util_WindowTool */

#ifndef _Included_com_ea_translatetool_util_WindowTool
#define _Included_com_ea_translatetool_util_WindowTool
#ifdef __cplusplus
extern "C" {
#endif

/*
 * Class:     com_ea_translatetool_util_WindowTool
 * Method:    getMainWindow
 * Signature: (I)I
 */
JNIEXPORT jint JNICALL Java_com_ea_translatetool_util_WindowTool_getMainWindow
  (JNIEnv *, jobject, jint);

/*
 * Class:     com_ea_translatetool_util_WindowTool
 * Method:    findWindow
 * Signature: (Ljava/lang/String;)I
 */
JNIEXPORT jint JNICALL Java_com_ea_translatetool_util_WindowTool_findWindow
  (JNIEnv *, jobject, jstring);

/*
 * Class:     com_ea_translatetool_util_WindowTool
 * Method:    showWindow
 * Signature: (II)Z
 */
JNIEXPORT jboolean JNICALL Java_com_ea_translatetool_util_WindowTool_showWindow
  (JNIEnv *, jobject, jint, jint);

/*
 * Class:     com_ea_translatetool_util_WindowTool
 * Method:    setWindowText
 * Signature: (ILjava/lang/String;)Z
 */
JNIEXPORT jboolean JNICALL Java_com_ea_translatetool_util_WindowTool_setWindowText
  (JNIEnv *, jobject, jint, jstring);

/*
 * Class:     com_ea_translatetool_util_WindowTool
 * Method:    getWindowText
 * Signature: (I)Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_com_ea_translatetool_util_WindowTool_getWindowText
  (JNIEnv *, jobject, jint);

/*
 * Class:     com_ea_translatetool_util_WindowTool
 * Method:    isWindow
 * Signature: (I)Z
 */
JNIEXPORT jboolean JNICALL Java_com_ea_translatetool_util_WindowTool_isWindow
  (JNIEnv *, jobject, jint);

/*
 * Class:     com_ea_translatetool_util_WindowTool
 * Method:    isWindowVisible
 * Signature: (I)Z
 */
JNIEXPORT jboolean JNICALL Java_com_ea_translatetool_util_WindowTool_isWindowVisible
  (JNIEnv *, jobject, jint);

/*
 * Class:     com_ea_translatetool_util_WindowTool
 * Method:    setWindowPos
 * Signature: (IIIIIII)Z
 */
JNIEXPORT jboolean JNICALL Java_com_ea_translatetool_util_WindowTool_setWindowPos
  (JNIEnv *, jobject, jint, jint, jint, jint, jint, jint, jint);

/*
 * Class:     com_ea_translatetool_util_WindowTool
 * Method:    getWindowLong
 * Signature: (II)I
 */
JNIEXPORT jint JNICALL Java_com_ea_translatetool_util_WindowTool_getWindowLong
  (JNIEnv *, jobject, jint, jint);

/*
 * Class:     com_ea_translatetool_util_WindowTool
 * Method:    setWindowLong
 * Signature: (III)I
 */
JNIEXPORT jint JNICALL Java_com_ea_translatetool_util_WindowTool_setWindowLong
  (JNIEnv *, jobject, jint, jint, jint);

/*
 * Class:     com_ea_translatetool_util_WindowTool
 * Method:    getSystemMenu
 * Signature: (IZ)J
 */
JNIEXPORT jlong JNICALL Java_com_ea_translatetool_util_WindowTool_getSystemMenu
  (JNIEnv *, jobject, jint, jboolean);

/*
 * Class:     com_ea_translatetool_util_WindowTool
 * Method:    enableMenuItem
 * Signature: (JII)Z
 */
JNIEXPORT jboolean JNICALL Java_com_ea_translatetool_util_WindowTool_enableMenuItem
  (JNIEnv *, jobject, jlong, jint, jint);

#ifdef __cplusplus
}
#endif
#endif