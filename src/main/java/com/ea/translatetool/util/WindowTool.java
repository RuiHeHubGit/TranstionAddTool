package com.ea.translatetool.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Window manipulation tool
 * WindowTool
 * Only Windows systems are supported
 * Created by HeRui on = 2018;/12/21.
 */
public class WindowTool {
    private static volatile int cmdHwnd;
    public final static int SW_HIDE = 0;
    public final static int SW_NORMAL = 1;
    public final static int SW_SHOWMINIMIZED = 2;
    public final static int SW_MAXIMIZE = 3;
    public final static int SW_SHOWNOACTIVATE = 4;
    public final static int SW_SHOW = 5;
    public final static int SW_MINIMIZE = 6;
    public final static int SW_SHOWMINNOACTIVE = 7;
    public final static int SW_SHOWNA = 8;
    public final static int SW_RESTORE = 9;
    public final static int SW_SHOWDEFAULT = 10;
    public final static int SW_FORCEMINIMIZE = 11;

    // 将窗口置于所有非顶层窗口之上（即在所有顶层窗口之后）,如果窗口己经是非顶层窗口则该标志不起作用
    public final static int HWND_NOTOPMOST = -2;
    // 将窗口置于所有非顶层窗口之上,即使窗口未被激活窗口也将保持顶级位置
    public final static int HWND_TOPMOST = -1;
    // 将窗口置于Z序的顶部
    public final static int HWND_TOP = 0;
    // 将窗口置于Z序的底部。如果参数hWnd标识了一个顶层窗口，则窗口失去顶级位置，并且被置在其他窗口的底部
    public final static int HWND_BOTTOM = 1;

    //在窗口周围画一个边框
    public final static int SWP_DRAWFRAME = 0x0020;
    // 给窗口发送WM_NCCALCSIZE消息，即使窗口尺寸没有改变也会发送该消息。如果未指定这个标志，只有在改变了窗口尺寸时才发送WM_NCCALCSIZE。
    public final static int SWP_FRAMECHANGED = 0x0020;
    // 隐藏窗口
    public final static int SWP_HIDEWINDOW = 0x0080;
    // 不激活窗口。如果未设置标志，则窗口被激活，并被设置到其他最高级窗口或非最高级组的顶部（根据参数hWndlnsertAfter设置）
    public final static int SWP_NOACTIVATE = 0x0010;
    // 维持当前位置
    public final static int SWP_NOMOVE = 0x0002;
    // 维持当前尺寸（忽略cx和cy参数）
    public final static int SWP_NOSIZE = 0x0001;
    // 维持当前Z序（忽略hWndlnsertAfter参数）
    public final static int SWP_NOZORDER = 0x0004;
    // 显示窗口
    public final static int SWP_SHOWWINDOW = 0x0040;
    // 不改变z序中的所有者窗口的位置
    public final static int SWP_NOOWNERZORDER = 0x0200;

    // 设置一个新的扩展窗口风格
    public final static int GWL_EXSTYLE = -20;
    // 设置一个新的窗口风格
    public final static int GWL_STYLE = -16;

    // 表明参数uIDEnableltem给出了菜单项的标识符,如果MF_BYCOMMAND和MF_POSITION都没被指定，则MF_BYCOMMAND为缺省标志
    public final static int MF_BYCOMMAND = 0x00000000;
    // 表明参数uIDEnableltem给出了菜单项的以零为基准的相对位置
    public final static int MF_BYPOSITION = 0x00000400;
    // 表明菜单项无效，但没变灰，因此不能被选择
    public final static int MF_DISABLED = 0x00000002;
    // 表明菜单项有效，并从变灰的状态恢复，因此可被选择
    public final static int MF_ENABLED = 0x00000000;
    //表明菜单项无效并且变灰，因此不能被选择
    public final static int MF_GRAYED = 0x00000001;

    public final static int SC_SIZE = 0xF000;
    public final static int SC_MOVE = 0xF010;
    public final static int SC_MINIMIZE = 0xF020;
    public final static int SC_MAXIMIZE = 0xF030;
    public final static int SC_CLOSE = 0xF060;

    private WindowTool() {
        loadLibrary();
    }

    public static WindowTool getInstance() {
        return WindowsToolInstanceLoader.INSTANCE;
    }

    private static class WindowsToolInstanceLoader{
        private final static WindowTool INSTANCE = new WindowTool();
    }

    /**
     * 加载动态链接库 WindowTool.dl
     */
    public void loadLibrary(){
        try {
            System.loadLibrary("WindowTool");
            getCmdHwnd();
        } catch (Throwable t) {
            File deskDll = new File("WindowTool.dll");
            deskDll.delete();
            InputStream inputStream = null;
            FileOutputStream outputStream = null;
            try {
                inputStream = WindowTool.class.getClassLoader().getResourceAsStream("lib/WindowTool.dll");
                outputStream = new FileOutputStream(deskDll);
                byte[] bytes = new byte[1024];
                int size;
                while ((size = inputStream.read(bytes)) > 0) {
                    outputStream.write(bytes, 0, size);
                }
            } catch (IOException e) {
                LoggerUtil.error(e.getMessage());
            } finally {
                try {
                    if (inputStream != null) {
                        inputStream.close();
                    }
                } catch (IOException e) {
                    LoggerUtil.error(e.getMessage());
                } finally {
                    if (outputStream != null) {
                        try {
                            outputStream.close();
                            System.loadLibrary("WindowTool");
                        } catch (IOException e) {
                            LoggerUtil.error(e.getMessage());
                        }
                    }
                }
            }
        }
    }


    /**
     * 获得调用进程相关联的控制台窗口句柄
     * @param pid
     * @return
     */
    public native int getMainWindow(int pid);

    /**
     * 根据窗口名获得窗口句柄
     * @param windowName
     * @return
     */
    public native int findWindow(String windowName);

    /**
     * 该函数设置指定窗口的显示状态
     * @param hWnd
     * @param nCmdShow
     * @return
     */
    public native boolean showWindow(int hWnd, int nCmdShow);

    /**
     * 该函数改变指定窗口的标题栏的文本内容
     * @param hwnd
     * @param text
     * @return
     */
    public native boolean setWindowText(int hwnd, String text);

    /**
     * 该函数将指定窗口的标题条文本
     * @param hWnd
     * @return
     */
    public native String getWindowText(int hWnd);

    /**
     * 该函数确定给定的窗口句柄是否识别一个已存在的窗口
     * @param hWnd
     * @return
     */
    public native boolean isWindow(int hWnd);

    /**
     * 该函数获得给定窗口的可视状态
     * @param hWnd
     * @return
     */
    public native boolean isWindowVisible(int hWnd);

    /**
     *
     * @param hWnd 窗口句柄
     * @param hWndlnsertAfter 在z序中的位于被置位的窗口前的窗口句柄。该参数必须为一个窗口句柄
     * @param x 以客户坐标指定窗口新位置的左边界
     * @param y 以客户坐标指定窗口新位置的顶边界
     * @param cx 以像素指定窗口的新的宽度
     * @param cy 以像素指定窗口的新的高度
     * @param flags 窗口尺寸和定位的标志
     * @return
     */
    public native boolean setWindowPos(int hWnd, int hWndlnsertAfter, int x, int y,int cx,int cy, int flags);

    /**
     * 获得有关指定窗口的信息
     * @param hWnd
     * @param nlndex
     * @return
     */
    public native int getWindowLong(int hWnd, int nlndex);

    /**
     * 改变指定窗口的属性
     * @param hWnd
     * @param nlndex
     * @param dwNewLong
     * @return
     */
    public native int setWindowLong(int hWnd, int nlndex, int dwNewLong);

    /**
     * 获取菜单
     * @param hWnd
     * @param revert
     * @return
     */
    public native long getSystemMenu(int hWnd, boolean revert);

    /**
     * 使指定的菜单项有效、无效或变灰
     * @param hMenu
     * @param lDEnablttem
     * @param enable
     * @return
     */
    public native boolean enableMenuItem(long hMenu, int lDEnablttem, int enable);

    /**
     * 获取cmd窗口句柄
     * @return
     */
    public int getCmdHwnd() {
        if(cmdHwnd == 0) {
            if(PID.isStartWithWindowExplorer())
                cmdHwnd = getMainWindow(Integer.parseInt(PID.getPID()));
            else {
                cmdHwnd = getMainWindow(
                        Integer.parseInt(
                                PID.getProcessAttr(PID.getPID(), PID.ProcessAttr.PARENT_PROCESS_ID)));
            }
        }
        return cmdHwnd;
    }

    /**
     * 显示或关闭CMD窗口
     * @param show
     */
    public void setCmdShow(boolean show) {
        try {
            getCmdHwnd();
            if(cmdHwnd > 0) {
                showWindow(cmdHwnd,
                        show ? SW_SHOW : SW_HIDE);
            }
        } catch (Throwable t) {
            LoggerUtil.error(t.getMessage());
        }
    }

    /**
     * 使窗口指定系统菜单项有效、无效变灰
     * @param enable
     */
    public void enableSystemMenu(int sc, boolean enable) {
        try {
            getCmdHwnd();
            if(cmdHwnd > 0) {
                if(enable) {
                    enableMenuItem(getSystemMenu(cmdHwnd, false), sc, MF_BYCOMMAND | MF_ENABLED);
                } else {
                    enableMenuItem(getSystemMenu(cmdHwnd, false), sc, MF_BYCOMMAND | MF_GRAYED);
                }
            }
        } catch (Throwable t) {
            LoggerUtil.error(t.getMessage());
        }
    }
}
