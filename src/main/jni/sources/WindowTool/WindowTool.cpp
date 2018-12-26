// WindowTool.cpp : 定义 DLL 应用程序的导出函数。
//

#include "stdafx.h"


WNDPROC OldProc;

void charTowchar(const char *chr, wchar_t *wchar, int size)
{
	MultiByteToWideChar(CP_ACP, 0, chr,
		strlen(chr) + 1, wchar, size / sizeof(wchar[0]));
}


void wcharTochar(const wchar_t *wchar, char *chr, int length)
{
	WideCharToMultiByte(CP_ACP, 0, wchar, -1,
		chr, length, NULL, NULL);
}


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

LRESULT CALLBACK FilterCloseProc(HWND hWnd, UINT message, WPARAM wParam, LPARAM lParam)
{
	switch (message)
	{
	case WM_CLOSE:
		break;
	default:
		return CallWindowProc(OldProc, hWnd, message, wParam, lParam);
	}
}

/*
 * Class:     com_ea_translatetool_util_WindowTool
 * Method:    getMainWindow
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_com_ea_translatetool_util_WindowTool_getMainWindow
(JNIEnv * env, jobject obj, jint pid)
{
	return (INT64)GetMainWindowHwnd(pid);
}

/*
 * Class:     com_ea_translatetool_util_WindowTool
 * Method:    findWindow
 * Signature: (Ljava/lang/String;)I
 */
JNIEXPORT jint JNICALL Java_com_ea_translatetool_util_WindowTool_findWindow
(JNIEnv *env, jobject obj, jstring windowName)
{
	wchar_t wText[256];
	charTowchar(env->GetStringUTFChars(windowName, 0), wText, 256);
	return (INT64)FindWindow(NULL, wText);
}

/*
 * Class:     com_ea_translatetool_util_WindowTool
 * Method:    showWindow
 * Signature: (II)Z
 */
JNIEXPORT jboolean JNICALL Java_com_ea_translatetool_util_WindowTool_showWindow
(JNIEnv *env, jobject, jint hWnd, jint nCmdShow)
{
	return ShowWindow((HWND)hWnd, (int)nCmdShow);
}

/*
 * Class:     com_ea_translatetool_util_WindowTool
 * Method:    setWindowText
 * Signature: (ILjava/lang/String;)Z
 */
JNIEXPORT jboolean JNICALL Java_com_ea_translatetool_util_WindowTool_setWindowText
(JNIEnv *env, jobject obj, jint hWnd, jstring text)
{
	wchar_t wText[256];
	charTowchar(env->GetStringUTFChars(text, 0), wText, 256);
	return SetWindowText((HWND)hWnd, wText);
}

/*
 * Class:     com_ea_translatetool_util_WindowTool
 * Method:    getWindowText
 * Signature: (I)Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_com_ea_translatetool_util_WindowTool_getWindowText
(JNIEnv *env, jobject obj, jint hWnd)
{
	TCHAR text[256] = { 0 };
	GetWindowText((HWND)hWnd, text, 255);
	char mChar[256] = { 0 };
	wcharTochar(text, mChar, 256);
	return env->NewStringUTF(mChar);
}

/*
 * Class:     com_ea_translatetool_util_WindowTool
 * Method:    isWindow
 * Signature: (I)Z
 */
JNIEXPORT jboolean JNICALL Java_com_ea_translatetool_util_WindowTool_isWindow
(JNIEnv *env, jobject obj, jint hWnd)
{
	return IsWindow((HWND)hWnd);
}

/*
 * Class:     com_ea_translatetool_util_WindowTool
 * Method:    isWindowVisible
 * Signature: (I)Z
 */
JNIEXPORT jboolean JNICALL Java_com_ea_translatetool_util_WindowTool_isWindowVisible
(JNIEnv *evn, jobject obj, jint hWnd)
{
	return IsWindowVisible((HWND)hWnd);
}

/*
 * Class:     com_ea_translatetool_util_WindowTool
 * Method:    setWindowPos
 * Signature: (IIIIIII)Z
 */
JNIEXPORT jboolean JNICALL Java_com_ea_translatetool_util_WindowTool_setWindowPos
(JNIEnv *env, jobject obj, jint hWnd, jint hWndlnsertAfter, jint x, jint y, jint cx, jint cy, jint flags)
{
	return SetWindowPos((HWND)hWnd, (HWND)hWndlnsertAfter, x, y, cx, cy, flags);
}

/*
 * Class:     com_ea_translatetool_util_WindowTool
 * Method:    getWindowLong
 * Signature: (IIIIIII)Z
 */
JNIEXPORT jint JNICALL Java_com_ea_translatetool_util_WindowTool_getWindowLong
(JNIEnv *env, jobject obj, jint hWnd, jint nlndex)
{
	return GetWindowLong((HWND)hWnd, nlndex);
}


/*
 * Class:     com_ea_translatetool_util_WindowTool
 * Method:    setWindowLong
 * Signature: (IIIIIII)Z
 */
JNIEXPORT jint JNICALL Java_com_ea_translatetool_util_WindowTool_setWindowLong
(JNIEnv *env, jobject obj, jint hWnd, jint nlndex, jint dwNewLong)
{
	return SetWindowLong((HWND)hWnd, nlndex, dwNewLong);
}

/*
 * Class:     com_ea_translatetool_util_WindowTool
 * Method:    getSystemMenu
 * Signature: (IIIIIII)Z
 */
JNIEXPORT jlong JNICALL Java_com_ea_translatetool_util_WindowTool_getSystemMenu
(JNIEnv *env, jobject obj, jint hWnd, jboolean bRevert)
{
	return (jlong)GetSystemMenu((HWND)hWnd, bRevert);
}

/*
 * Class:     com_ea_translatetool_util_WindowTool
 * Method:    enableMenuItem
 * Signature: (IIIIIII)Z
 */
JNIEXPORT jboolean JNICALL Java_com_ea_translatetool_util_WindowTool_enableMenuItem
(JNIEnv *env, jobject obj, jlong hMenu, jint ulDEnablttem, jint uEnable)
{
	return EnableMenuItem((HMENU)hMenu, ulDEnablttem, uEnable);
}