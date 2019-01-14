package com.ea.translatetool.ui.tabs;

import com.ea.translatetool.App;
import com.ea.translatetool.addit.AdditAssist;
import com.ea.translatetool.config.AppConfig;
import com.ea.translatetool.config.ConfigRepository;
import com.ea.translatetool.config.FileConfigRepositoryImpl;
import com.ea.translatetool.constant.GlobalConstant;
import com.ea.translatetool.ui.UI;
import com.ea.translatetool.ui.component.ZebraStripeJTable;
import com.ea.translatetool.util.LoggerUtil;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;

import static com.ea.translatetool.util.LoggerUtil.*;

public class SettingTab extends JPanel implements ActionListener,ItemListener{
    private UI ui;
    private AppConfig config;
    private JTextField tfLocalMapPath;
    private JTextField tfExistKeySaveDir;
    private JTextField tfLogSaveDir;
    private JComboBox<String> jcobLogLevel;
    private ZebraStripeJTable jtInPath;
    private JTextField jfOutPath;
    private JTextField jfFilePrefix;
    private JTextField jfFileSuffix;
    private JCheckBox jcbIsCoverKey;
    private List<String> inPaths;
    private JButton btnSave;
    private JButton btnDefault;
    private JButton btnCancel;
    private JDialog localeMapDlg;
    private ZebraStripeJTable jtLocaleMap;

    public SettingTab(UI ui) {
        this.ui = ui;
    }

    public void init() {
        config = ui.getAppConfig();
        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout());
        JPanel confirmPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 10));
        btnSave = new JButton("save");
        btnSave.addActionListener(this);
        btnDefault = new JButton("default");
        btnDefault.addActionListener(this);
        btnCancel = new JButton("cancel");
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
        jcobLogLevel.setSelectedItem(config.getLogLevel());
        jtInPath = new ZebraStripeJTable();
        jtInPath.setColumnToCheckBox(1, true);
        jtInPath.setCheckBoxWidth(30);
        jfOutPath = new JTextField(config.getOutPath());
        jfFilePrefix = new JTextField(config.getFilePrefix());
        jfFileSuffix = new JTextField(config.getFileSuffix());
        jcbIsCoverKey = new JCheckBox("Cover Exist Key ", null, config.isCoverKey());

        jcbIsCoverKey.addItemListener(this);
        jcobLogLevel.addItemListener(this);

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

        settingPanel.add(Box.createVerticalStrut(15));
        JPanel boxPanel = new JPanel();
        boxPanel.setLayout(new BoxLayout(boxPanel, BoxLayout.LINE_AXIS));
        boxPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        boxPanel.add(Box.createHorizontalStrut(5));
        boxPanel.add(new JLabel("Log Leave  "));
        jcobLogLevel.setMaximumSize(new Dimension(80, 20));
        boxPanel.add(jcobLogLevel);
        boxPanel.add(Box.createHorizontalStrut(30));
        boxPanel.add(jcbIsCoverKey);
        boxPanel.add(Box.createHorizontalStrut(30));
        JButton btnLocaleMap = new JButton("locale map");
        boxPanel.add(btnLocaleMap);
        settingPanel.add(boxPanel);
        btnLocaleMap.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showLocaleMapDialog();
            }
        });

        settingPanel.add(Box.createVerticalStrut(20));
        settingPanel.add(createInPathPanel());

        inPaths = new ArrayList<>();
        for (String path : config.getInPath()) {
            if(!inPaths.contains(path)) {
                inPaths.add(path);
            }
        }
        updateInPathListTable();
        enableConfirmButtons(false, true);
    }

    private Component createInPathPanel() {
        JButton btnRemove = new JButton("remove");
        JButton btnClear = new JButton("clear");
        JButton btnAddInPath = new JButton("add");
        final JCheckBox jcbAll = new JCheckBox("all", true);
        btnRemove.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                boolean isChange = false;
                DefaultTableModel tableModel = (DefaultTableModel) jtInPath.getModel();
                for (int i=0; i<tableModel.getRowCount(); ++i) {
                    if((Boolean) tableModel.getValueAt(i, 1)) {
                        tableModel.removeRow(i);
                        --i;
                        isChange = true;
                    }
                }
                if(isChange) {
                    jtInPath.updateUI();
                    enableConfirmButtons(true, true);
                }
            }
        });
        btnClear.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                boolean isChange = jtInPath.getRowCount() > 0;
                ((DefaultTableModel)jtInPath.getModel()).setRowCount(0);
                if(isChange) {
                    jtInPath.updateUI();
                    enableConfirmButtons(true, true);
                }
            }
        });
        jcbAll.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                boolean checked = ((JCheckBox)e.getSource()).isSelected();
                for (int i=0; i<jtInPath.getRowCount(); ++i) {
                    jtInPath.setValueAt(checked, i, 1);
                }
                jtInPath.updateUI();
            }
        });
        jtInPath.getModel().addTableModelListener(new TableModelListener() {
            @Override
            public void tableChanged(TableModelEvent e) {
                DefaultTableModel tableModel = (DefaultTableModel) e.getSource();
                if(e.getColumn() == 1) {
                    if(!((Boolean) tableModel.getValueAt(e.getFirstRow(), e.getColumn()))) {
                        jcbAll.setSelected(false);
                    }
                }
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

    private void enableConfirmButtons(boolean show, boolean showDefBtn) {
        btnSave.setEnabled(show);
        btnDefault.setEnabled(showDefBtn);
        btnCancel.setEnabled(show);
    }

    private void addInPath() {
        FileFilter fileFilter = AdditAssist.createExcelFileFilter();
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
                enableConfirmButtons(true, true);
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
                    enableConfirmButtons(true, true);
                }
            }
        });

        panel.add(jLabel, BorderLayout.NORTH);
        panel.add(jf, BorderLayout.CENTER);
        panel.add(jButton, BorderLayout.EAST);

        return panel;
    }

    private void saveSetting() {
        String[] inPath = new String[jtInPath.getRowCount()];
        for (int i=0; i<jtInPath.getRowCount(); ++i) {
            inPath[i] = (String) jtInPath.getValueAt(i, 0);
        }
        setAppConfig(config,
                tfLocalMapPath.getText(),
                tfExistKeySaveDir.getText(),
                tfLogSaveDir.getText(),
                (String) jcobLogLevel.getSelectedItem(),
                inPath,
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

        enableConfirmButtons(false, false);
        AppConfig  newConfig = App.createDefAppConfig();
        newConfig.setLogLevel(LoggerUtil.setLogLevel(newConfig.getLogLevel()));
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
        inPaths.clear();
        for (String path : newConfig.getInPath()) {
            if(!inPaths.contains(path)) {
                inPaths.add(path);
            }
        }
        resetUIValue();
        saveSetting();
    }

    private void cancelChange() {
        resetUIValue();
        enableConfirmButtons(false, true);
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
        updateInPathListTable();
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

    private void showLocaleMapDialog() {
        if(localeMapDlg == null) {
            localeMapDlg = new JDialog(ui, false);
            initLocaleMapDlg();
        }
        showLocaleMap();
        localeMapDlg.setVisible(true);
    }

    private void showLocaleMap() {
        DefaultTableModel tableModel = (DefaultTableModel) jtLocaleMap.getModel();
        HashMap<String, String> localMap = ui.getWorkConfig().getLocalMap();
        Set<String> keys = localMap.keySet();
        tableModel.setRowCount(keys.size());
        tableModel.setColumnCount(2);
        int index = 0;
        for (String key : keys) {
            tableModel.setValueAt(key, index, 0);
            tableModel.setValueAt(localMap.get(key), index, 1);
            ++index;
        }
    }

    private void initLocaleMapDlg() {
        localeMapDlg.setSize(500, 600);
        localeMapDlg.setLocationRelativeTo(null);
        localeMapDlg.setLayout(new BorderLayout(5, 5));
        jtLocaleMap = new ZebraStripeJTable(new Object[][]{}, new Object[]{"Locale Name", "Locale Value"});

        localeMapDlg.add(new JScrollPane(jtLocaleMap), BorderLayout.CENTER);

        final JButton btnDef = new JButton("Default");
        final JButton btnSave = new JButton("Save");
        btnSave.setEnabled(false);
        final JButton btnCancel = new JButton("Cancel");
        btnCancel.setEnabled(false);
        final JButton btnRemove = new JButton("Remove");
        btnRemove.setEnabled(false);
        final JButton btnAdd = new JButton("Add New");
        JPanel opTablePanel = new JPanel();
        opTablePanel.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 10));
        opTablePanel.setLayout(new BoxLayout(opTablePanel, BoxLayout.LINE_AXIS));
        opTablePanel.add(Box.createHorizontalGlue());
        opTablePanel.add(btnSave);
        opTablePanel.add(Box.createHorizontalStrut(15));
        opTablePanel.add(btnCancel);
        opTablePanel.add(Box.createHorizontalStrut(15));
        opTablePanel.add(btnRemove);
        opTablePanel.add(Box.createHorizontalStrut(15));
        opTablePanel.add(btnAdd);
        opTablePanel.add(Box.createHorizontalStrut(15));
        opTablePanel.add(btnDef);
        opTablePanel.add(Box.createHorizontalStrut(15));

        btnDef.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int opt = JOptionPane.showConfirmDialog(localeMapDlg, "Are you sure you want to restore the default locale map?", "prompt", JOptionPane.YES_NO_OPTION);
                if(JOptionPane.YES_OPTION != opt) {
                    return;
                }
                btnDef.setEnabled(false);
                try {
                    HashMap<String, String> newLocaleMap = AdditAssist.loadLocalMap(config.getLocalMapFilePath(), true);
                    HashMap<String, String> localMap = ui.getWorkConfig().getLocalMap();
                    localMap.clear();
                    localMap.putAll(newLocaleMap);
                    showLocaleMap();
                } catch (IOException e1) {
                    btnDef.setEnabled(true);
                    LoggerUtil.error(e1.getMessage());
                    JOptionPane.showMessageDialog(localeMapDlg, "set to default failed.", "error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        btnSave.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                btnSave.setEnabled(false);
                btnCancel.setEnabled(false);
                DefaultTableModel tableModel = (DefaultTableModel) jtLocaleMap.getModel();
                HashMap<String, String> localMap = ui.getWorkConfig().getLocalMap();
                localMap.clear();
                for (int i=0; i<tableModel.getRowCount(); ++i) {
                    String keyName = tableModel.getValueAt(i, 0).toString();
                    String keyValue = tableModel.getValueAt(i, 1).toString();
                    if(keyName.trim().isEmpty() || keyValue.trim().isEmpty()) {
                        tableModel.removeRow(i);
                        --i;
                        continue;
                    }
                    localMap.put(keyName, keyValue);
                }
                saveLocaleMap(localMap);
            }
        });
        btnCancel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                btnSave.setEnabled(false);
                btnCancel.setEnabled(false);
                showLocaleMap();
            }
        });
        btnRemove.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                btnRemove.setEnabled(false);
                int[] rows = jtLocaleMap.getSelectedRows();
                DefaultTableModel tableModel = (DefaultTableModel) jtLocaleMap.getModel();
                int rCount = 0;
                for (int rowIndex : rows) {
                    tableModel.removeRow(rowIndex-rCount);
                    ++rCount;
                }
                jtLocaleMap.updateUI();
                if(rCount > 0) {
                    btnSave.setEnabled(true);
                    btnCancel.setEnabled(true);
                    btnDef.setEnabled(true);
                }
            }
        });

        btnAdd.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                DefaultTableModel tableModel = (DefaultTableModel) jtLocaleMap.getModel();
                int rowCount = jtLocaleMap.getRowCount();
                tableModel.insertRow(rowCount, new String[]{"", ""});
                jtLocaleMap.getSelectionModel().setSelectionInterval(rowCount , rowCount);
                Rectangle rect = jtLocaleMap.getCellRect(rowCount ,  0 ,  true );
                jtLocaleMap.scrollRectToVisible(rect);
                jtLocaleMap.editCellAt(rowCount, 0);
                jtLocaleMap.getEditorComponent().requestFocus();
                btnSave.setEnabled(true);
                btnCancel.setEnabled(true);
                btnRemove.setEnabled(true);
                btnDef.setEnabled(true);
            }
        });
        localeMapDlg.add(opTablePanel, BorderLayout.NORTH);

        jtLocaleMap.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                super.mouseReleased(e);
                btnRemove.setEnabled(jtLocaleMap.getSelectedRows().length > 0);
            }
        });
    }

    private void saveLocaleMap(HashMap<String, String> localMap) {
        ConfigRepository configRepository = FileConfigRepositoryImpl.getInstance();
        Properties properties = new Properties();
        properties.put(FileConfigRepositoryImpl.CONFIG_FILE_PATH_KEY, config.getLocalMapFilePath());
        configRepository.storage(localMap, properties);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object tag = e.getSource();
        if(tag instanceof JButton) {
            JButton btn = (JButton) tag;
            switch (btn.getText()) {
                case "save":
                    enableConfirmButtons(false, true);
                    saveSetting();
                    break;
                case "default":
                    defaultSetting();
                    break;
                case "cancel":
                    enableConfirmButtons(false, true);
                    cancelChange();
                    break;
            }
        }
    }

    @Override
    public void itemStateChanged(ItemEvent e) {
        enableConfirmButtons(true, true);
    }
}
