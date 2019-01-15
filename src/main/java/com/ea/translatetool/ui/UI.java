package com.ea.translatetool.ui;

import com.ea.translatetool.App;
import com.ea.translatetool.addit.Addit;
import com.ea.translatetool.addit.AdditAssist;
import com.ea.translatetool.addit.WorkCallback;
import com.ea.translatetool.addit.exception.AlreadyExistKeyException;
import com.ea.translatetool.addit.exception.InvalidExcelContentException;
import com.ea.translatetool.addit.exception.NoFoundLocaleException;
import com.ea.translatetool.addit.mode.TranslationLocator;
import com.ea.translatetool.addit.mode.WorkStage;
import com.ea.translatetool.cmd.CmdMode;
import com.ea.translatetool.config.AppConfig;
import com.ea.translatetool.config.WorkConfig;
import com.ea.translatetool.constant.GlobalConstant;
import com.ea.translatetool.ui.tabs.InputTab;
import com.ea.translatetool.ui.tabs.OutputTab;
import com.ea.translatetool.ui.tabs.SettingTab;
import com.ea.translatetool.ui.tabs.UnableHandleTab;
import com.ea.translatetool.util.LoggerUtil;
import com.ea.translatetool.util.PID;
import com.ea.translatetool.util.ShutdownHandler;
import com.ea.translatetool.util.WindowTool;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.table.TableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

public class UI extends JFrame implements WorkCallback{
    private static UI ui;
    private final App app;
    private WorkConfig workConfig;
    private volatile int exitStatus;
    private Container mainContainer;
    private JTabbedPane tabbedPane;
    private JPanel footPanel;
    private InputTab inputTab;
    private UnableHandleTab unableHandleTab;
    private OutputTab outputTab;
    private SettingTab settingTab;
    private JLabel lbInfo;
    private JLabel lbProgress;
    private JLabel lbStage;
    private JProgressBar progressBar;
    private JButton btnStart;

    private UI(App app) {
        this.app = app;
    }

    public synchronized static void start(App app) {
        if(ui == null) {
            init(app);
        }
        ui.setVisible(true);
    }

    private void doStart() {
        if(Addit.isRunning()) {
            return;
        }

        HashMap<String, JTable> tableHashMap = inputTab.getTableViewMap();
        Set<String> keys = tableHashMap.keySet();
        workConfig.getTranslationLocatorMap().clear();
        ArrayList<File> fileList = new ArrayList<>();
        for (String key : keys) {
            JTable jTable = tableHashMap.get(key);
            TableModel tableModel = jTable.getModel();
            for (int i=0; i<tableModel.getRowCount(); ++i) {
                if((boolean)tableModel.getValueAt(i, 0)) {
                    if(!(Boolean)tableModel.getValueAt(i, 0)) {
                        continue;
                    }
                    File file = new File(key, (String) tableModel.getValueAt(i, 1));
                    fileList.add(file);
                    if(tableModel.getValueAt(i, 0) == null) {
                        continue;
                    }
                    TranslationLocator locator = new TranslationLocator();
                    workConfig.getTranslationLocatorMap().put(file.getAbsolutePath(), locator);
                    locator.setKeyLocator((String) tableModel.getValueAt(i, 2));
                    String ori = (String) tableModel.getValueAt(i, 3);
                    if(ori != null) {
                        locator.setOrientation(GlobalConstant.Orientation.valueOf(ori.toUpperCase()).ordinal());
                    }
                    locator.setLocalLocator((Integer) tableModel.getValueAt(i, 4));
                    locator.setTranslationLocator((Integer) tableModel.getValueAt(i, 5));
                }
            }
        }

        if(fileList.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please add excel file.", "prompt", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        workConfig.setExcelFiles(fileList);
        workConfig.setOutput(new File(outputTab.getOutPath()));

        workConfig.setIgnoreLocaleSet(unableHandleTab.getIgnoreLocaleSet());

        new Thread(new Runnable() {
            @Override
            public void run() {
                Addit.doWork(ui.workConfig, Addit.WORK_LOAD_TRANSLATION, Addit.WORK_TRANSLATION_TO_FILE, ui);
            }
        }).start();
    }

    private void updateProgressBar(long complete, long total) {
        if(total == 0) {
            lbProgress.setText("");
            progressBar.setValue(0);
            return;
        }
        float percent = 100.0f * complete / total;
        lbProgress.setText(String.format(complete+"/"+total+"  %.2f%%", percent));
        progressBar.setValue((int) percent);
        progressBar.setToolTipText(String.format("%.2f%%", percent));
    }

    private static void init(App app) {
        if(PID.isStartWithContain(GlobalConstant.NEED_HIDE_PRO)) {
            WindowTool windowTool = WindowTool.getInstance();
            windowTool.setWindowText(windowTool.getCmdHwnd(), "translate tool cmd");
            windowTool.enableSystemMenu(WindowTool.SC_CLOSE, false);
            windowTool.setCmdShow(false);
        }

        ui = new UI(app);
        ui.workConfig = AdditAssist.createWorkConfig(app.getAppConfig());
        ui.addShutdownHandler();
        ui.initUI();
    }

    private void initUI() {
        mainContainer = this.getContentPane();
        initWindow();
        initFoodPanel();
        initTab();
    }

    private void initFoodPanel() {
        footPanel = new JPanel();
        footPanel.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        footPanel.setLayout(new BoxLayout(footPanel, BoxLayout.LINE_AXIS));
        footPanel.setPreferredSize(new Dimension(footPanel.getSize().width, 35));
        mainContainer.add(footPanel, BorderLayout.SOUTH);

        lbInfo = new JLabel("", JLabel.RIGHT);
        lbStage = new JLabel("", JLabel.RIGHT);
        lbProgress = new JLabel();
        progressBar = new JProgressBar();
        btnStart = new JButton("start");

        progressBar.setMaximumSize(new Dimension(200, (int) btnStart.getPreferredSize().getHeight()));
        progressBar.setMinimumSize(new Dimension(50, (int) btnStart.getPreferredSize().getHeight()));

        JPanel progressPanel = new JPanel();
        progressPanel.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        progressPanel.setLayout(new BoxLayout(progressPanel, BoxLayout.LINE_AXIS));
        progressPanel.setMinimumSize(new Dimension(15, (int) btnStart.getPreferredSize().getHeight()));
        progressPanel.setPreferredSize(new Dimension(750, (int) btnStart.getPreferredSize().getHeight()));
        progressPanel.setMaximumSize(new Dimension(1200, (int) btnStart.getPreferredSize().getHeight()));

        progressPanel.add(Box.createHorizontalStrut(20));
        progressPanel.add(progressBar);
        progressPanel.add(Box.createHorizontalStrut(10));
        progressPanel.add(lbProgress);
        progressPanel.add(Box.createHorizontalStrut(20));
        progressPanel.add(lbStage);
        progressPanel.add(Box.createHorizontalStrut(20));
        progressPanel.add(lbInfo);

        Component glue = Box.createHorizontalGlue();
        glue.setMaximumSize(new Dimension(100, (int) btnStart.getPreferredSize().getHeight()));
        glue.setMinimumSize(new Dimension(20, (int) btnStart.getPreferredSize().getHeight()));


        footPanel.add(glue);
        footPanel.add(btnStart);
        footPanel.add(progressPanel);

        btnStart.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                doStart();
            }
        });
    }

    private void initTab() {

        tabbedPane = new JTabbedPane();
        inputTab = new InputTab(this);
        unableHandleTab = new UnableHandleTab(this);
        outputTab = new OutputTab(this);
        settingTab = new SettingTab(this);

        mainContainer.add(tabbedPane, BorderLayout.CENTER);
        tabbedPane.add("input", inputTab);
        tabbedPane.add("unableHandle", unableHandleTab);
        tabbedPane.add("output", outputTab);
        tabbedPane.add("setting", settingTab);

        inputTab.init();
        unableHandleTab.init();
        outputTab.init();
        settingTab.init();
    }

    private void initWindow() {
        initTheme();
        setTitle("translate tool");
        try {
            BufferedImage image = ImageIO.read(UI.class.getClassLoader().getResourceAsStream(("tools_72px.png")));
            setIconImage(image);
        } catch (IOException e) {
            LoggerUtil.error(e.getMessage());
        }
        setSize(1000, 700);
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
            LoggerUtil.error(e.getMessage());
        } catch (InstantiationException e) {
            LoggerUtil.error(e.getMessage());
        } catch (IllegalAccessException e) {
            LoggerUtil.error(e.getMessage());
        } catch (UnsupportedLookAndFeelException e) {
            LoggerUtil.error(e.getMessage());
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

    public WorkConfig getWorkConfig() {
        return workConfig;
    }

    public AppConfig getAppConfig() {
        return app.getAppConfig();
    }

    @Override
    public void onStart(WorkStage stage) {
        btnStart.setEnabled(false);
        btnStart.updateUI();
        if(stage.getIndex() == 1) {
            System.out.println("start ..");
        }
        String stageInfo = String.format("\n%d/%d %s doWork\n",
                stage.getIndex(),
                stage.getCount(),
                stage.getName());
        System.out.println(stageInfo);
        lbStage.setText(stageInfo);
    }

    @Override
    public void onProgress(long complete, long total, String info) {
        CmdMode.showProgress(complete, total, 50);
        updateProgressBar(complete, total);
        lbInfo.setText(info);
    }

    @Override
    public void onDone(WorkStage stage) {
        String stageInfo = String.format("\n%d/%d %s %s\n",
                stage.getIndex(),
                stage.getCount(),
                stage.getName(),
                stage.isSuccess()?"finished." : "failed.");
        System.out.println(stageInfo);
        lbInfo.setText("");
        lbStage.setText(stageInfo);
        btnStart.setEnabled(true);
        if(stage.getType() == Addit.WORK_TRANSLATION_TO_FILE) {
            outputTab.showFileListTable();
        }
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                tabbedPane.updateUI();
            }
        });
    }

    @Override
    public boolean onError(Throwable t) {
        if(t instanceof AlreadyExistKeyException) {
            if(app.getAppConfig().isCoverKey()) {
                return false;
            }
            try {
                AdditAssist.saveKeyExistTranslation(((AlreadyExistKeyException) t).getExistList(),
                        AdditAssist.createExistKeySaveFile(app.getAppConfig()));
            } catch (IOException e) {
                LoggerUtil.error(e.getMessage());
            }
            return true;
        } else if(t instanceof NoFoundLocaleException) {
            tabbedPane.setSelectedIndex(1);
            unableHandleTab.update();
            JOptionPane.showMessageDialog(ui, t.getMessage(), "error", JOptionPane.ERROR_MESSAGE);
            return true;
        } else {
            LoggerUtil.exceptionLog(t);
            String msg = t.getMessage();
            if (msg == null) msg = "Unknown error.";
            if(t instanceof InvalidExcelContentException) {
                if (JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(ui,
                        msg + "\nDo you want to continue?", "warning", JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE)) {
                    return false;
                }
            } else {
                JOptionPane.showMessageDialog(ui, msg, "error", JOptionPane.ERROR_MESSAGE);
            }
            return true;
        }
    }
}
