package com.ea.translatetool.ui.tabs;

import com.ea.translatetool.addit.Addit;
import com.ea.translatetool.config.AppConfig;
import com.ea.translatetool.config.ConfigRepository;
import com.ea.translatetool.config.FileConfigRepositoryImpl;
import com.ea.translatetool.constant.GlobalConstant;
import com.ea.translatetool.ui.UI;
import com.ea.translatetool.ui.component.FileListTable;
import com.ea.translatetool.util.ExcelUtil;
import com.ea.translatetool.util.LoggerUtil;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.DefaultTableModel;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static com.ea.translatetool.util.LoggerUtil.*;

public class SettingTab extends JPanel implements ActionListener{
    private UI ui;
    private AppConfig config;
    private JPanel confirmPanel;
    private JTextField tfLocalMapPath;
    private JTextField tfExistKeySaveDir;
    private JTextField tfLogSaveDir;
    private JComboBox<String> jcobLogLevel;
    private JTable jtInPath;
    private JTextField jfOutPath;
    private JTextField jfFilePrefix;
    private JTextField jfFileSuffix;
    private JCheckBox jcbIsCoverKey;
    private List<File> inPaths;


    public SettingTab(UI ui) {
        this.ui = ui;
        config = ui.getAppConfig();
        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout());
        confirmPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 10));
        JButton btnSave = new JButton("save");
        btnSave.addActionListener(this);
        JButton btnDefault = new JButton("default");
        btnDefault.addActionListener(this);
        JButton btnCancel = new JButton("cancel");
        btnCancel.addActionListener(this);
        confirmPanel.add(btnSave);
        confirmPanel.add(btnDefault);
        confirmPanel.add(btnCancel);
        add(confirmPanel, BorderLayout.NORTH);

        JPanel settingPanel = new JPanel(new FlowLayout());
        settingPanel.setLayout(new BoxLayout(settingPanel, BoxLayout.PAGE_AXIS));
        add(new JScrollPane(settingPanel), BorderLayout.CENTER);

        tfLocalMapPath = new JTextField(config.getLocalMapFilePath());
        tfExistKeySaveDir = new JTextField(config.getExistKeySaveDir());
        tfLogSaveDir = new JTextField(config.getLogSaveDir());
        jcobLogLevel = new JComboBox<>(new String[]{LEVEL_FINE, LEVEL_INFO, LEVEL_WARNING, LEVEL_ERROR});
        jtInPath = new FileListTable();
        jfOutPath = new JTextField(config.getOutPath());
        jfFilePrefix = new JTextField(config.getFilePrefix());
        jfFileSuffix = new JTextField(config.getFileSuffix());
        jcbIsCoverKey = new JCheckBox("Cover Exist Key ", null, config.isCoverKey());

        settingPanel.add(createSingleJFileChooserPanel("Local Map Path", tfLocalMapPath, "select", JFileChooser.FILES_ONLY));
        settingPanel.add(createSingleJFileChooserPanel("Exist Key Save Dir", tfExistKeySaveDir, "select", JFileChooser.DIRECTORIES_ONLY));
        settingPanel.add(createSingleJFileChooserPanel("Log Save Dir", tfLogSaveDir, "select", JFileChooser.DIRECTORIES_ONLY));
        settingPanel.add(createSingleJFileChooserPanel("Out Path", jfOutPath, "select", JFileChooser.DIRECTORIES_ONLY));

        settingPanel.add(Box.createVerticalStrut(10));
        JPanel fixPanel = new JPanel(new GridLayout(1, 2));
        fixPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 55));
        fixPanel.add(createEditPanel("File Prefix:", jfFilePrefix));
        fixPanel.add(createEditPanel("File Suffix:", jfFileSuffix));
        settingPanel.add(fixPanel);

        settingPanel.add(Box.createVerticalStrut(10));
        JPanel boxPanel = new JPanel();
        boxPanel.setLayout(new BoxLayout(boxPanel, BoxLayout.LINE_AXIS));
        boxPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        boxPanel.add(Box.createHorizontalStrut(5));
        boxPanel.add(new JLabel("Log Leave  "));
        jcobLogLevel.setMaximumSize(new Dimension(80, 20));
        boxPanel.add(jcobLogLevel);
        boxPanel.add(Box.createHorizontalStrut(30));
        boxPanel.add(jcbIsCoverKey);
        settingPanel.add(boxPanel);

        settingPanel.add(Box.createVerticalStrut(10));
        JButton btnAddInPath = new JButton("add");
        btnAddInPath.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addInPath();
            }
        });
        JPanel inPathListTop = new JPanel(new BorderLayout());
        inPathListTop.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        inPathListTop.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        inPathListTop.add(new JLabel("int path"), BorderLayout.WEST);
        inPathListTop.add(btnAddInPath, BorderLayout.EAST);
        settingPanel.add(inPathListTop);
        settingPanel.add(jtInPath);
        inPaths = new ArrayList<>();
        updateInPathListTable();
    }

    private void addInPath() {
        FileFilter fileFilter = new FileFilter() {
            @Override
            public boolean accept(File f) {
                if(f.isDirectory() || (f.isFile()
                        && !f.getName().startsWith("~$")
                        && (f.getAbsolutePath().endsWith(ExcelUtil.SUFFIX_XLS)
                        || f.getAbsolutePath().endsWith(ExcelUtil.SUFFIX_XLSX)))) {
                    return true;
                }
                return false;
            }

            @Override
            public String getDescription() {
                return "directory;*"+ExcelUtil.SUFFIX_XLS+";*"+ExcelUtil.SUFFIX_XLSX;
            }
        };

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        fileChooser.setMultiSelectionEnabled(true);
        fileChooser.setFileFilter(fileFilter);

        if(JFileChooser.APPROVE_OPTION == fileChooser.showDialog(this, "select")) {
            File[] files = fileChooser.getSelectedFiles();
            for (File file : files) {
                if(!fileFilter.accept(file)) {
                    JOptionPane.showMessageDialog(this, "Please only select a folder or excel file", "error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }

            boolean needUpdate = false;
            for (File file : files) {
                if(!inPaths.contains(file)) {
                    inPaths.add(file);
                    needUpdate = true;
                }
            }

            if(needUpdate) {
                updateInPathListTable();
            }
        }
    }

    private void updateInPathListTable() {
        DefaultTableModel tableModel = (DefaultTableModel) jtInPath.getModel();
        tableModel.setColumnCount(2);
        tableModel.setRowCount(config.getInPath().length);
        int index = 0;
        for (String path : config.getInPath()) {
            tableModel.setValueAt(path, index, 0);
            tableModel.setValueAt("remove", index++, 1);
        }
        jtInPath.updateUI();
    }

    private Component createEditPanel(String lbText, JTextField jf) {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        panel.add(new JLabel(lbText), BorderLayout.NORTH);
        panel.add(jf, BorderLayout.CENTER);
        return panel;
    }

    private  JPanel createSingleJFileChooserPanel(String lbText, final JTextField jf, final String btnName, final int selectionMode) {
        final JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 55));
        JLabel jLabel = new JLabel(lbText);
        jLabel.setMinimumSize(new Dimension(100, 20));
        JButton jButton = new JButton(btnName);
        jButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setFileSelectionMode(selectionMode);
                fileChooser.setMultiSelectionEnabled(false);
                if(JFileChooser.APPROVE_OPTION == fileChooser.showDialog(panel, btnName)) {
                    File file = fileChooser.getSelectedFile();
                    jf.setText(file.getAbsolutePath());
                }
            }
        });

        panel.add(jLabel, BorderLayout.NORTH);
        panel.add(jf, BorderLayout.CENTER);
        panel.add(jButton, BorderLayout.EAST);

        return panel;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object tag = e.getSource();
        if(tag instanceof JButton) {
            JButton btn = (JButton) tag;
            switch (btn.getText()) {
                case "save":
                    saveSetting();
                    break;
                case "default":
                    defaultSetting();
                    break;
                case "cancel":
                    cancelChange();
                    break;
            }
        }
    }

    private void saveSetting() {
        String[] InPath = new String[]{};
        setAppConfig(config,
                tfLocalMapPath.getText(),
                tfExistKeySaveDir.getText(),
                tfLogSaveDir.getText(),
                (String) jcobLogLevel.getSelectedItem(),
                InPath,
                jfOutPath.getText(),
                jfFilePrefix.getText(),
                jfFileSuffix.getText(),
                jcbIsCoverKey.isSelected());
        ConfigRepository configRepository = FileConfigRepositoryImpl.getInstance();
        Properties properties = new Properties();
        properties.put(FileConfigRepositoryImpl.CONFIG_FILE_PATH_KEY, GlobalConstant.CONFIG_FILE_PATH);
        configRepository.storage(config, properties);
    }

    private void defaultSetting() {
        int opt = JOptionPane.showConfirmDialog(this, "Are you sure you want to restore the default settings?", "prompt", JOptionPane.YES_NO_OPTION);
        if(JOptionPane.YES_OPTION != opt) {
            return;
        }

        File configFile = new File(GlobalConstant.CONFIG_FILE_PATH);
        configFile.delete();
        ConfigRepository configRepository = FileConfigRepositoryImpl.getInstance();
        Properties properties = new Properties();
        properties.put(FileConfigRepositoryImpl.CONFIG_FILE_PATH_KEY, GlobalConstant.CONFIG_FILE_PATH);
        AppConfig  newConfig = configRepository.load(AppConfig.class, properties);
        System.getProperty(LoggerUtil.LOG_LEVEL, newConfig.getLogLevel());
        setAppConfig(config,
                newConfig.getLocalMapFilePath(),
                newConfig.getExistKeySaveDir(),
                newConfig.getLogSaveDir(),
                newConfig.getLogLevel(),
                newConfig.getInPath(),
                newConfig.getOutPath(),
                newConfig.getFilePrefix(),
                newConfig.getFileSuffix(),
                newConfig.isCoverKey());

        resetUIValue();
    }

    private void cancelChange() {
        resetUIValue();
        confirmPanel.setVisible(false);
    }

    private void resetUIValue() {
        tfLocalMapPath.setText(config.getLocalMapFilePath());
        tfExistKeySaveDir.setText(config.getExistKeySaveDir());
        tfLogSaveDir.setText(config.getLogSaveDir());
        jcobLogLevel.setSelectedItem(config.getLogLevel());
        jfOutPath.setText(config.getOutPath());
        jfFilePrefix.setText(config.getFilePrefix());
        jfFileSuffix.setText(config.getFileSuffix());
        jcbIsCoverKey.setSelected(config.isCoverKey());
    }

    void setAppConfig(AppConfig config, String localMapFilePath, String existKeySaveDir, String logSaveDir, String logLevel,
                      String[] inPath, String outPath, String filePrefix, String fileSuffix, boolean isCoverKey) {
        config.setLocalMapFilePath(localMapFilePath);
        config.setExistKeySaveDir(existKeySaveDir);
        config.setLogSaveDir(logSaveDir);
        config.setLogLevel (logLevel);
        config.setInPath (inPath);
        config.setOutPath (outPath);
        config.setFilePrefix (filePrefix);
        config.setFileSuffix (fileSuffix);
        config.setCoverKey (isCoverKey);
    }
}
