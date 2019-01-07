package com.ea.translatetool.ui.tabs;

import com.ea.translatetool.addit.AdditAssist;
import com.ea.translatetool.addit.mode.TranslationLocator;
import com.ea.translatetool.config.WorkConfig;
import com.ea.translatetool.constant.GlobalConstant;
import com.ea.translatetool.ui.UI;
import com.ea.translatetool.ui.component.TranslateFileJTable;
import com.ea.translatetool.util.ExcelUtil;
import com.ea.translatetool.util.IOUtil;
import com.ea.translatetool.util.LoggerUtil;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.util.*;
import java.util.List;

public class InputTab extends JPanel implements ActionListener{
    private WorkConfig workConfig;
    private JPanel contentPanel;
    private TreeMap<String, List<File>> excelFiles;
    private TreeMap<String, Object[][]> tableData;
    private TreeSet<String> updateList;
    private HashMap<String, JTable> tableViewMap;

    public InputTab(UI parent) {
        this.excelFiles = new TreeMap<>();
        this.tableData = new TreeMap<>();
        this.updateList = new TreeSet<>();
        this.tableViewMap = new HashMap<>();
        this.workConfig = parent.getWorkConfig();
        setLayout(new BorderLayout());
        initBtnGroup();
        initInputFileListTableView();
    }

    private void initInputFileListTableView() {
        contentPanel = new JPanel(new GridLayout(0, 1, 10, 10));
        JScrollPane listScrollPage = new JScrollPane(contentPanel);
        listScrollPage.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        listScrollPage.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        add(listScrollPage, BorderLayout.CENTER);
        File[] files = new File[workConfig.getInput().size()];
        for (int i=0; i<workConfig.getInput().size(); ++i) {
            files[i] = workConfig.getInput().get(i);
        }
        scanExcelFiles(files);
    }

    private void showFileListTable() {

        Object[] columnNames = new Object[]{new JCheckBox(), "file name", "key", "orientation", "local", "translation"};
        for (String key : updateList) {
            List<File> files = excelFiles.get(key);
            if(files.isEmpty()) {
                continue;
            }

            Object[][] data = tableData.get(key);
            if(data == null) {
                data = new Object[files.size()][6];
                tableData.put(key, data);
            }
            for (int i=0; i<files.size(); ++i) {
                data[i][0] = false;
                File file = files.get(i);
                data[i][1] = file.getName();
                try {
                    TranslationLocator locator = AdditAssist.calcTranslationLocator(
                            ExcelUtil.getExcelString(ExcelUtil.getWorkbook(file), 0, 0, 0 ), null);
                    if(locator != null) {
                        workConfig.getTranslationLocatorMap().put(key, locator);
                        data[i][0] = true;
                        if(locator.getKeyLocator() != null) {
                            data[i][2] = locator.getKeyLocator();
                        }
                        if(locator.getOrientation() != null) {
                            data[i][3] = GlobalConstant.Orientation.values()[locator.getOrientation()].toString().toLowerCase();
                        }
                        if(locator.getLocalLocator() != null) {
                            data[i][4] = locator.getLocalLocator();
                        }
                        if(locator.getTranslationLocator() != null) {
                            data[i][5] = locator.getTranslationLocator();
                        }
                    }
                } catch (IOException e) {
                    LoggerUtil.error(e.getMessage());
                }
            }

            JTable jTable = tableViewMap.get(key);
            if(jTable == null) {
                JPanel panel = new JPanel(new BorderLayout(2, 2));
                contentPanel.add(panel);
                panel.add(new JLabel(" " + key), BorderLayout.NORTH);
                TableModel tableModel = new DefaultTableModel(data, columnNames);
                jTable = new TranslateFileJTable(tableModel);
                panel.add(jTable, BorderLayout.CENTER);
                tableViewMap.put(key, jTable);
            }
        }
        contentPanel.updateUI();
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
                    cleanSelectPath();
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
        Set<String> keys = tableData.keySet();
        for (String key : keys) {
            for (Object[] row : tableData.get(key)) {
                row[0] = checked;
            }
            JTable tableView = tableViewMap.get(key);
            tableView.updateUI();
        }
    }

    private void cleanSelectPath() {
        excelFiles.clear();
        tableData.clear();
        tableViewMap.clear();
        workConfig.getTranslationLocatorMap().clear();
        contentPanel.removeAll();
        contentPanel.updateUI();
    }

    private void addInputPath() {
        FileFilter fileFilter = new FileFilter() {
            @Override
            public boolean accept(File f) {
                if(f.isDirectory() || (f.isFile()
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
            scanExcelFiles(files);
        }

    }

    private void scanExcelFiles(File[] files) {
        updateList.clear();
        for (File file : files) {
            boolean addNew = false;
            List<File> newFileList = null;
            String key;
            if(file.isDirectory()) {
                key = file.getAbsolutePath();
                newFileList = IOUtil.fileList(file, true, new DirectoryStream.Filter<File>() {
                    @Override
                    public boolean accept(File entry) throws IOException {
                        if(entry.isFile()
                                && (entry.getAbsolutePath().endsWith(ExcelUtil.SUFFIX_XLS)
                                || entry.getAbsolutePath().endsWith(ExcelUtil.SUFFIX_XLSX))) {
                            return true;
                        }
                        return false;
                    }
                });
            } else {
                key = file.getParent();
            }
            List<File> oldFileList = excelFiles.get(key);
            if(oldFileList == null) {
                oldFileList = new ArrayList<>();
                excelFiles.put(key, oldFileList);
            }
            if(file.isDirectory()) {
                if (!newFileList.isEmpty()) {
                    for (File f : newFileList) {
                        if (!oldFileList.contains(f)) {
                            oldFileList.add(f);
                            addNew = true;
                        }
                    }
                }
            } else {
                if (!oldFileList.contains(file)) {
                    oldFileList.add(file);
                    addNew = true;
                }
            }
            if(addNew) {
                updateList.add(key);
            }
        }

        if(!updateList.isEmpty()) {
            showFileListTable();
        }
    }

    public TreeMap<String, Object[][]> getTableData() {
        return tableData;
    }
}
