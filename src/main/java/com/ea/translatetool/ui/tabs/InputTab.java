package com.ea.translatetool.ui.tabs;

import com.ea.translatetool.addit.Addit;
import com.ea.translatetool.addit.AdditAssist;
import com.ea.translatetool.addit.mode.TranslationLocator;
import com.ea.translatetool.config.WorkConfig;
import com.ea.translatetool.constant.GlobalConstant;
import com.ea.translatetool.ui.UI;
import com.ea.translatetool.ui.component.ZebraStripeJTable;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.util.*;
import java.util.List;
import java.util.Timer;
import java.util.concurrent.ConcurrentSkipListSet;

public class InputTab extends JPanel implements ActionListener {
    private UI ui;
    private WorkConfig workConfig;
    private JPanel contentPanel;
    private Integer fileNameMaxLen;
    private SpringLayout springLayout;
    private Spring contentPanelHeight;
    private TreeMap<String, List<File>> excelFiles;
    private HashMap<String, JTable> tableViewMap;
    private ConcurrentSkipListSet<String> updateKeys;

    public InputTab(UI parent) {
        this.ui = parent;
    }

    public void init() {
        this.excelFiles = new TreeMap<>();
        this.updateKeys = new ConcurrentSkipListSet<>();
        this.tableViewMap = new HashMap<>();
        this.workConfig = ui.getWorkConfig();
        setLayout(new BorderLayout());
        initBtnGroup();
        initInputFileListTableView();
    }

    private void initInputFileListTableView() {
        springLayout = new SpringLayout();
        contentPanel = new JPanel(springLayout);
        JScrollPane listScrollPage = new JScrollPane(contentPanel);
        listScrollPage.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        listScrollPage.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        add(listScrollPage, BorderLayout.CENTER);

        new Thread(new Runnable() {
            @Override
            public void run() {
                loadExcelFiles();
            }
        }).start();
    }

    private void loadExcelFiles() {
        final Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                showFileListTable();
                if(!Addit.isRunning()) {
                    updateKeys.clear();
                    timer.cancel();
                }
            }
        }, 200, 500);
        Object result = Addit.doWork(workConfig, Addit.WORK_SCAN_FILE, Addit.WORK_SCAN_FILE, ui);
        if(result == null) {
            return;
        }

        List<File> fileList;
        if(result instanceof List) {
            fileList = (List<File>)result;
        } else {
            return;
        }

        for (Object item : fileList) {
            File f = (File) item;
            String key = f.getParent();
            List<File> oldFileList = excelFiles.get(key);
            if(oldFileList == null) {
                oldFileList = new ArrayList<>();
                oldFileList.add(f);
                excelFiles.put(key, oldFileList);
            } else if(!oldFileList.contains(f)){
                oldFileList.add(f);
            }
            updateKeys.add(key);
        }
        if(!updateKeys.isEmpty()) {
            workConfig.setExcelFiles(fileList);
            Addit.doWork(workConfig, Addit.WORK_CALC_LOCATOR, Addit.WORK_CALC_LOCATOR, ui);
        }
    }

    private void showFileListTable() {

        boolean needAdjWidth = false;
        for (String key : updateKeys) {
            List<File> files = excelFiles.get(key);
            if(files.isEmpty()) {
                continue;
            }

            ZebraStripeJTable jTable = (ZebraStripeJTable) tableViewMap.get(key);
            if(jTable == null) {
                SpringLayout panelLayout = new SpringLayout();
                JPanel panel = new JPanel(panelLayout);
                contentPanel.add(panel);
                panel.add(new JLabel(" " + key));
                jTable = new ZebraStripeJTable(new Object[][]{}, new Object[]{"", "File Name", "Key Locator", "Locale Translation Orientation", "Locale Locator", "Translation Locator"});
                jTable.setCheckBoxWidth(28);
                jTable.setColumnToCheckBox(0, true);
                jTable.setColumnToComboBox(3, new String[]{"horizontal","vertical"});
                JScrollPane jScrollPane = new JScrollPane(jTable);
                panel.add(jScrollPane);
                SpringLayout.Constraints panelCons = springLayout.getConstraints(panel);
                panelLayout.putConstraint(SpringLayout.NORTH, jScrollPane, 18, SpringLayout.NORTH, panel);
                panelLayout.putConstraint(SpringLayout.WEST, jScrollPane, 0, SpringLayout.WEST, panel);
                panelLayout.putConstraint(SpringLayout.EAST, jScrollPane, 0, SpringLayout.EAST, panel);
                panelCons.setHeight(Spring.sum(panelLayout.getConstraints(jTable).getHeight(), Spring.constant(50)));
                tableViewMap.put(key, jTable);

                springLayout.putConstraint(SpringLayout.WEST, panel, -2, SpringLayout.WEST, contentPanel);
                springLayout.putConstraint(SpringLayout.EAST, panel, 2, SpringLayout.EAST, contentPanel);
                if (contentPanelHeight == null) {
                    fileNameMaxLen = 0;
                    contentPanelHeight = Spring.constant(0);
                } else {
                    panelCons.setY(contentPanelHeight);
                }
                contentPanelHeight = Spring.sum(contentPanelHeight, panelCons.getHeight());
                springLayout.getConstraints(contentPanel).setHeight(contentPanelHeight);
            }

            DefaultTableModel dataModel = (DefaultTableModel) jTable.getModel();
            dataModel.setRowCount(files.size());
            dataModel.setColumnCount(6);

            for (int i=0; i<files.size(); ++i) {
                File file = files.get(i);
                String fileName = file.getName();
                //calc second need min-width
                FontMetrics fm = new JLabel().getFontMetrics(getFont());
                Rectangle2D bounds = fm.getStringBounds(fileName, null);
                if(bounds.getWidth() - 10 > fileNameMaxLen) {
                    fileNameMaxLen = (int) bounds.getWidth();
                    needAdjWidth = true;
                }

                dataModel.setValueAt(false, i, 0);
                dataModel.setValueAt(fileName, i, 1);
                TranslationLocator locator = workConfig.getTranslationLocatorMap().get(file.getAbsolutePath());
                if (locator != null) {
                    dataModel.setValueAt(true, i, 0);
                    if (locator.getKeyLocator() != null) {
                        dataModel.setValueAt(locator.getKeyLocator(), i, 2);
                    }
                    if (locator.getOrientation() != null) {
                        dataModel.setValueAt(GlobalConstant.Orientation.values()[locator.getOrientation()].toString().toLowerCase(), i, 3);
                    }
                    if (locator.getLocalLocator() != null) {
                        dataModel.setValueAt(locator.getLocalLocator(), i, 4);
                    }
                    if (locator.getTranslationLocator() != null) {
                        dataModel.setValueAt(locator.getTranslationLocator(), i, 5);
                    }
                    jTable.setRowTextColor(i, Color.BLACK);
                } else {
                    jTable.setRowTextColor(i, Color.RED);
                }
            }
            jTable.setModel(dataModel);

            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    contentPanel.updateUI();
                }
            });

        }

        if(needAdjWidth) {
            for (String key : updateKeys) {
                JTable table = tableViewMap.get(key);
                if (table.getRowCount() > 0) {
                    TableColumn secondColumn = table.getColumnModel().getColumn(1);
                    secondColumn.setPreferredWidth(fileNameMaxLen);
                }
            }
        }
    }

    private void initBtnGroup() {
        JPanel btnGroupPanel = new JPanel();
        add(btnGroupPanel, BorderLayout.NORTH);
        btnGroupPanel.setLayout(new BoxLayout(btnGroupPanel, BoxLayout.LINE_AXIS));
        btnGroupPanel.setPreferredSize(new Dimension(0, 40));
        JButton btnAdd = new JButton("add");
        JButton btnClean = new JButton("clean");
        JButton btnSelect  = new JButton("select all");
        JButton btnInvertSelect = new JButton("cancel select");
        btnAdd.addActionListener(this);
        btnClean.addActionListener(this);
        btnSelect.addActionListener(this);
        btnInvertSelect.addActionListener(this);

        int hMargin = 15;
        btnGroupPanel.add(Box.createHorizontalStrut(hMargin));
        btnGroupPanel.add(btnAdd);
        btnGroupPanel.add(Box.createHorizontalStrut(hMargin));
        btnGroupPanel.add(btnClean);
        btnGroupPanel.add(Box.createHorizontalStrut(hMargin));
        btnGroupPanel.add(btnSelect);
        btnGroupPanel.add(Box.createHorizontalStrut(hMargin));
        btnGroupPanel.add(btnInvertSelect);
        btnGroupPanel.add(Box.createHorizontalStrut(hMargin));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object tag = e.getSource();
        if(tag instanceof JButton) {
            JButton button = (JButton)tag;
            switch (button.getText()) {
                case "add":
                    addInputPath();
                    break;
                case "clean":
                    cleanAllInPath();
                    break;
                case "select all":
                    selectAll(true);
                    break;
                case "cancel select":
                    selectAll(false);
                    break;
            }
        }
    }

    private void selectAll(boolean checked) {
        Set<String> keys = tableViewMap.keySet();
        for (String key : keys) {
            JTable tableView = tableViewMap.get(key);
            AbstractTableModel tableModel = (AbstractTableModel) tableView.getModel();
            for (int i=0; i<tableModel.getRowCount(); ++i) {
                tableModel.setValueAt(checked, i, 0);
            }
            tableView.updateUI();
        }
    }

    private void cleanAllInPath() {
        excelFiles.clear();
        tableViewMap.clear();
        contentPanel.removeAll();
        contentPanel.updateUI();
        workConfig.getTranslationLocatorMap().clear();
        workConfig.getExcelFiles().clear();
        fileNameMaxLen = null;
        contentPanelHeight = null;
    }

    private void addInputPath() {
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

            workConfig.getInput().clear();
            for (File file : files) {
                workConfig.getInput().add(file);
            }
            new Thread(new Runnable() {
                @Override
                public void run() {
                    loadExcelFiles();
                }
            }).start();
        }
    }

    public HashMap<String, JTable> getTableViewMap() {
        return tableViewMap;
    }
}
