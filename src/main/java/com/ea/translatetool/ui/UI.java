package com.ea.translatetool.ui;

import com.ea.translatetool.App;
import com.ea.translatetool.addit.Addit;
import com.ea.translatetool.addit.WorkCallback;
import com.ea.translatetool.addit.mode.WorkStage;
import com.ea.translatetool.cmd.CmdMode;
import com.ea.translatetool.config.WorkConfig;
import com.ea.translatetool.util.ShutdownHandler;
import com.ea.translatetool.util.WindowTool;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class UI extends JFrame {
    private static UI ui;
    private final App app;
    private WorkConfig workConfig;
    private volatile int exitStatus;

    private UI(App app) {
        this.app = app;
    }

    public synchronized static void start(App app) {
        if(ui == null) {
            init(app);
        }
        ui.setVisible(true);

        // test
        new Thread(new Runnable() {
            @Override
            public void run() {
                Addit.start(ui.workConfig, 0, new WorkCallback() {

                    @Override
                    public void onStart(WorkStage stage) {

                    }

                    @Override
                    public void onProgress(long complete, long total) {
                        if(!ui.isVisible()) {
                            CmdMode.showProgress(complete, total, 50);
                        }
                    }

                    @Override
                    public void onDone(WorkStage stage) {

                    }

                    @Override
                    public boolean onError(Throwable t) {
                        WindowTool.getInstance().setCmdShow(true);
                        return true;
                    }
                });
            }
        }).start();
    }

    private static void init(App app) {
        WindowTool windowTool = WindowTool.getInstance();
        windowTool.setWindowText(windowTool.getCmdHwnd(), "translate tool cmd");
        windowTool.enableSystemMenu(WindowTool.SC_CLOSE, false);
        windowTool.setCmdShow(false);
        ui = new UI(app);
        ui.initUI();
        ui.addShutdownHandler();
        ui.workConfig = Addit.getDefaultWorkConfig();
        ui.workConfig.setLocalMap(app.loadLocalMap(app.getAppConfig().getLocalMapFilePath()));
        ui.workConfig.setFilePrefix(app.getAppConfig().getFilePrefix());
        ui.workConfig.setFileSuffix(app.getAppConfig().getFileSuffix());
    }

    private void initUI() {
        initTheme();
        setTitle("translate tool");
        setIconImage(this.getToolkit().getImage(getClass().getClassLoader().getResource("tools_72px.ico")));
        setSize(800, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {

                if(JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(UI.this,
                        "Are you sure to close?","prompt",JOptionPane.YES_NO_OPTION)) {
                    if(Addit.isRunning()){
                        JOptionPane.showMessageDialog(null, "Terminating now will cause the task to fail!\n" +
                                "app will wait for the task to complete!", "alert", JOptionPane.WARNING_MESSAGE);

                    }
                    exit(0);
                }
            }
        });
    }

    private void initTheme() {
        try {
            // 当前系统的风格
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            // 可跨平台的风格
            // UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            // Windows风格
            // UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
            // Motif风格
            // UIManager.setLookAndFeel("com.sun.java.swing.plaf.motif.MotifLookAndFeel");
            // Windows Classic风格
            // UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsClassicLookAndFeel");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }
    }

    private void addShutdownHandler() {
        // 异常终止处
        ShutdownHandler.addShutdownHandler(new ShutdownHandler() {
            @Override
            public void run() {
                if(Addit.isRunning()) {
                    WindowTool.getInstance().setCmdShow(true);
                    System.out.println("WARNING:Terminating now will cause the task to fail," +
                            "\napp will wait for the task to complete!");
                }
                while (Addit.isRunning()) {
                    try {
                        sleep(100);
                    } catch (InterruptedException e) {}
                }
                if(exitStatus != 0) {
                    WindowTool.getInstance().setCmdShow(true);
                    System.err.println("abnormal termination.");
                    try {
                        sleep(3000);
                    } catch (InterruptedException e) {}
                }
            }
        });
    }

    void exit(int status) {
        this.exitStatus = status;
        this.dispose();
        System.exit(status);
    }
}
