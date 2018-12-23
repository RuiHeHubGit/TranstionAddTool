/* DO NOT EDIT THIS FILE - it is machine generated */
#include "jni.h"
/* Header for class com_ea_translatetool_util_WindowsTool */

#ifndef _Included_com_ea_translatetool_util_WindowsTool
#define _Included_com_ea_translatetool_util_WindowsTool
#ifdef __cplusplus
extern "C" {
#endif

/*
 * Class:     com_ea_translatetool_util_WindowsTool
 * Method:    getMainWindow
 * Signature: (I)I
 */
JNIEXPORT jint JNICALL Java_com_ea_translatetool_util_WindowsTool_getMainWindow
  (JNIEnv *, jobject, jint);

/*
 * Class:     com_ea_translatetool_util_WindowsTool
 * Method:    findWindow
 * Signature: (Ljava/lang/String;)I
 */
JNIEXPORT jint JNICALL Java_com_ea_translatetool_util_WindowsTool_findWindow
  (JNIEnv *, jobject, jstring);

/*
 * Class:     com_ea_translatetool_util_WindowsTool
 * Method:    showWindow
 * Signature: (II)Z
 */
JNIEXPORT jboolean JNICALL Java_com_ea_translatetool_util_WindowsTool_showWindow
  (JNIEnv *, jobject, jint, jint);

/*
 * Class:     com_ea_translatetool_util_WindowsTool
 * Method:    setWindowText
 * Signature: (ILjava/lang/String;)Z
 */
JNIEXPORT jboolean JNICALL Java_com_ea_translatetool_util_WindowsTool_setWindowText
  (JNIEnv *, jobject, jint, jstring);

/*
 * Class:     com_ea_translatetool_util_WindowsTool
 * Method:    getWindowText
 * Signature: (I)Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_com_ea_translatetool_util_WindowsTool_getWindowText
  (JNIEnv *, jobject, jint);

/*
 * Class:     com_ea_translatetool_util_WindowsTool
 * Method:    isWindow
 * Signature: (I)Z
 */
JNIEXPORT jboolean JNICALL Java_com_ea_translatetool_util_WindowsTool_isWindow
  (JNIEnv *, jobject, jint);

/*
 * Class:     com_ea_translatetool_util_WindowsTool
 * Method:    isWindowVisible
 * Signature: (I)Z
 */
JNIEXPORT jboolean JNICALL Java_com_ea_translatetool_util_WindowsTool_isWindowVisible
  (JNIEnv *, jobject, jint);

/*
 * Class:     com_ea_translatetool_util_WindowsTool
 * Method:    setWindowPos
 * Signature: (IIIIIII)Z
 */
JNIEXPORT jboolean JNICALL Java_com_ea_translatetool_util_WindowsTool_setWindowPos
  (JNIEnv *, jobject, jint, jint, jint, jint, jint, jint, jint);

/*
 * Class:     com_ea_translatetool_util_WindowsTool
 * Method:    getWindowLong
 * Signature: (II)I
 */
JNIEXPORT jint JNICALL Java_com_ea_translatetool_util_WindowsTool_getWindowLong
  (JNIEnv *, jobject, jint, jint);

/*
 * Class:     com_ea_translatetool_util_WindowsTool
 * Method:    setWindowLong
 * Signature: (III)I
 */
JNIEXPORT jint JNICALL Java_com_ea_translatetool_util_WindowsTool_setWindowLong
  (JNIEnv *, jobject, jint, jint, jint);

/*
 * Class:     com_ea_translatetool_util_WindowsTool
 * Method:    getSystemMenu
 * Signature: (IZ)I
 */
JNIEXPORT jlong JNICALL Java_com_ea_translatetool_util_WindowsTool_getSystemMenu
  (JNIEnv *, jobject, jint, jboolean);

/*
 * Class:     com_ea_translatetool_util_WindowsTool
 * Method:    enableMenuItem
 * Signature: (III)I
 */
JNIEXPORT jboolean JNICALL Java_com_ea_translatetool_util_WindowsTool_enableMenuItem
  (JNIEnv *, jobject, jint, jint, jint);

#ifdef __cplusplus
}
#endif
#endif