package com.ea.translatetool.ui.tabs;

import com.ea.translatetool.config.AppConfig;
import com.ea.translatetool.ui.UI;
import com.ea.translatetool.ui.component.FileListTable;
import com.ea.translatetool.util.ExcelUtil;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.DefaultTableModel;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static com.ea.translatetool.util.LoggerUtil.*;

public class SettingTab extends JPanel{
    private UI ui;
    private AppConfig config;
    private JTextField tfLocalMapPath;
    private JTextField tfExistKeySaveDir;
    private JTextField tfLogSaveDir;
    private JComboBox<String> jcobLogLevel;
    private JTable jtInPath;
    private JTextField jfOutPath;
    private JTextField jfFilePrefix;
    private JTextField jfFileSuffix;
    private JCheckBox jcbIsCoverKey;
    private List<String> inPaths;


    public SettingTab(UI ui) {
        this.ui = ui;
        config = ui.getAppConfig();
        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout());
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

        settingPanel.add(Box.createVerticalStrut(20));
        settingPanel.add(createInPathPanel());

        inPaths = new ArrayList<>();
        for (String path : config.getInPath()) {
            if(!inPaths.contains(path)) {
                inPaths.add(path);
            }
        }
        updateInPathListTable();
    }

    private Component createInPathPanel() {
        final JButton btnRemove = new JButton("remove");
        JButton btnClear = new JButton("clear");
        JButton btnAddInPath = new JButton("add");
        final JCheckBox jcbAll = new JCheckBox("all", true);
        btnRemove.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                DefaultTableModel tableModel = (DefaultTableModel) jtInPath.getModel();
                for (int i=0; i<tableModel.getRowCount(); ++i) {
                    if((Boolean) tableModel.getValueAt(i, 1)) {
                        tableModel.removeRow(i);
                        --i;
                    }
                }
                jtInPath.updateUI();
            }
        });
        btnClear.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ((DefaultTableModel)jtInPath.getModel()).setRowCount(0);
                jtInPath.updateUI();
            }
        });
        jcbAll.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                boolean checked = null != jcbAll.getSelectedObjects();
                for (int i=0; i<jtInPath.getRowCount(); ++i) {
                    jtInPath.setValueAt(checked, i, 1);
                }
                jtInPath.updateUI();
            }
        });
        btnAddInPath.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addInPath();
            }
        });
        JPanel inPathListTop = new JPanel();
        inPathListTop.setLayout(new BoxLayout(inPathListTop, BoxLayout.LINE_AXIS));
        inPathListTop.add(Box.createHorizontalGlue());
        inPathListTop.add(btnRemove);
        inPathListTop.add(Box.createHorizontalStrut(15));
        inPathListTop.add(btnClear);
        inPathListTop.add(Box.createHorizontalStrut(15));
        inPathListTop.add(btnAddInPath);
        inPathListTop.add(Box.createHorizontalStrut(15));
        inPathListTop.add(jcbAll);

        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
        tablePanel.add(inPathListTop, BorderLayout.NORTH);
        jtInPath.setMinimumSize(new Dimension(Integer.MAX_VALUE, 20));
        tablePanel.add(jtInPath, BorderLayout.CENTER);

        JPanel InPathPanel = new JPanel(new BorderLayout(5, 5));
        InPathPanel.add(new JLabel("In Path"), BorderLayout.NORTH);
        InPathPanel.setBorder(BorderFactory.createEmptyBorder(0, 5, 5, 5));
        InPathPanel.add(tablePanel);
        return InPathPanel;
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
                    inPaths.add(file.getAbsolutePath());
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
        tableModel.setRowCount(inPaths.size());
        int index = 0;
        for (String path : inPaths) {
            tableModel.setValueAt(path, index, 0);
            tableModel.setValueAt(true, index++, 1);
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
}
