#include "main.h"
#include "com_ea_translatetool_util_WindowsTool.h"

struct ProcessWindowData
{
	HWND hWnd;
	unsigned long lProcessId;
};

BOOL CALLBACK EnumWindowCallback(HWND hWnd, LPARAM lParam)
{
	ProcessWindowData& wndData = *(ProcessWindowData*)lParam;
	unsigned long lProcessId = 0;
	GetWindowThreadProcessId(hWnd, &lProcessId);
	if ((wndData.lProcessId != lProcessId) || (GetWindow(hWnd, GW_OWNER) != (HWND)0) || !IsWindowVisible(hWnd))
	{
		return 1;
	}
	wndData.hWnd = hWnd;
	return 0;
}

HWND GetMainWindowHwnd(unsigned long lProcessId)
{
    ProcessWindowData wndData;
	wndData.hWnd = 0;
	wndData.lProcessId = lProcessId;
	EnumWindows(EnumWindowCallback, (LPARAM)&wndData);
	return wndData.hWnd;
}

/*
 * Class:     com_ea_translatetool_util_WindowsTool
 * Method:    getMainWindow
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_com_ea_translatetool_util_WindowsTool_getMainWindow
  (JNIEnv * env, jobject obj, jint pid) {
     return (INT64)GetMainWindowHwnd(pid);
}

/*
 * Class:     com_ea_translatetool_util_WindowsTool
 * Method:    findWindow
 * Signature: (Ljava/lang/String;)I
 */
JNIEXPORT jint JNICALL Java_com_ea_translatetool_util_WindowsTool_findWindow
  (JNIEnv *env, jobject obj, jstring windowName) {
      return (INT64)FindWindow(NULL, env->GetStringUTFChars(windowName, 0));
}

/*
 * Class:     com_ea_translatetool_util_WindowsTool
 * Method:    showWindow
 * Signature: (II)Z
 */
JNIEXPORT jboolean JNICALL Java_com_ea_translatetool_util_WindowsTool_showWindow
  (JNIEnv *env, jobject, jint hWnd, jint nCmdShow) {
      return ShowWindow((HWND)hWnd, (int)nCmdShow);
}

/*
 * Class:     com_ea_translatetool_util_WindowsTool
 * Method:    setWindowText
 * Signature: (ILjava/lang/String;)Z
 */
JNIEXPORT jboolean JNICALL Java_com_ea_translatetool_util_WindowsTool_setWindowText
  (JNIEnv *env, jobject obj, jint hWnd, jstring text) {
      return SetWindowText((HWND)hWnd, env->GetStringUTFChars(text, 0));
}

/*
 * Class:     com_ea_translatetool_util_WindowsTool
 * Method:    getWindowText
 * Signature: (I)Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_com_ea_translatetool_util_WindowsTool_getWindowText
  (JNIEnv *env, jobject obj, jint hWnd) {
      TCHAR text[256] = {0};
      GetWindowText((HWND)hWnd, text, 255);
      return env->NewStringUTF(text);
}

/*
 * Class:     com_ea_translatetool_util_WindowsTool
 * Method:    isWindow
 * Signature: (I)Z
 */
JNIEXPORT jboolean JNICALL Java_com_ea_translatetool_util_WindowsTool_isWindow
  (JNIEnv *env, jobject obj, jint hWnd) {
      return IsWindow((HWND)hWnd);
}

/*
 * Class:     com_ea_translatetool_util_WindowsTool
 * Method:    isWindowVisible
 * Signature: (I)Z
 */
JNIEXPORT jboolean JNICALL Java_com_ea_translatetool_util_WindowsTool_isWindowVisible
  (JNIEnv *evn, jobject obj, jint hWnd) {
      return IsWindowVisible((HWND)hWnd);
}

/*
 * Class:     com_ea_translatetool_util_WindowsTool
 * Method:    setWindowPos
 * Signature: (IIIIIII)Z
 */
JNIEXPORT jboolean JNICALL Java_com_ea_translatetool_util_WindowsTool_setWindowPos
  (JNIEnv *env, jobject obj, jint hWnd, jint hWndlnsertAfter, jint x, jint y, jint cx, jint cy, jint flags) {
      return SetWindowPos((HWND)hWnd, (HWND)hWndlnsertAfter, x, y, cx, cy, flags);
}

/*
 * Class:     com_ea_translatetool_util_WindowsTool
 * Method:    getWindowLong
 * Signature: (IIIIIII)Z
 */
JNIEXPORT jint JNICALL Java_com_ea_translatetool_util_WindowsTool_getWindowLong
  (JNIEnv *env, jobject obj, jint hWnd, jint nlndex) {
  return GetWindowLong((HWND)hWnd, nlndex);
}


/*
 * Class:     com_ea_translatetool_util_WindowsTool
 * Method:    setWindowLong
 * Signature: (IIIIIII)Z
 */
JNIEXPORT jint JNICALL Java_com_ea_translatetool_util_WindowsTool_setWindowLong
  (JNIEnv *env, jobject obj, jint hWnd, jint nlndex, jint dwNewLong) {
      return SetWindowLong((HWND)hWnd, nlndex, dwNewLong);
}

/*
 * Class:     com_ea_translatetool_util_WindowsTool
 * Method:    getSystemMenu
 * Signature: (IIIIIII)Z
 */
JNIEXPORT jlong JNICALL Java_com_ea_translatetool_util_WindowsTool_getSystemMenu
  (JNIEnv *env, jobject obj, jint hWnd, jboolean bRevert) {
      return (jlong)GetSystemMenu((HWND)hWnd, bRevert);
}

/*
 * Class:     com_ea_translatetool_util_WindowsTool
 * Method:    enableMenuItem
 * Signature: (IIIIIII)Z
 */
JNIEXPORT jboolean JNICALL Java_com_ea_translatetool_util_WindowsTool_enableMenuItem
  (JNIEnv *env, jobject obj, jint hMenu, jint ulDEnablttem, jint uEnable) {
    return EnableMenuItem((HMENU)hMenu, ulDEnablttem, uEnable);
}


//RemoveMenu(HMENU hMenu,UINT uPosition,UINT uFlags);
extern "C" DLL_EXPORT BOOL APIENTRY DllMain(HINSTANCE hinstDLL, DWORD fdwReason, LPVOID lpvReserved)
{
    switch (fdwReason)
    {
        case DLL_PROCESS_ATTACH:
            // attach to process
            // return FALSE to fail DLL load
            break;

        case DLL_PROCESS_DETACH:
            // detach from process
            break;

        case DLL_THREAD_ATTACH:
            // attach to thread
            break;

        case DLL_THREAD_DETACH:
            // detach from thread
            break;
    }
    return TRUE; // succesful
}
