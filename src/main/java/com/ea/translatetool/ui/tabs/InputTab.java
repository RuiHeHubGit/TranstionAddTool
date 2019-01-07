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
import javax.swing.table.AbstractTableModel;
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
    private TreeSet<String> updateList;
    private HashMap<String, JTable> tableViewMap;

    public InputTab(UI parent) {
        this.excelFiles = new TreeMap<>();
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

        for (String key : updateList) {
            List<File> files = excelFiles.get(key);
            if(files.isEmpty()) {
                continue;
            }

            JTable jTable = tableViewMap.get(key);
            if(jTable == null) {
                JPanel panel = new JPanel(new BorderLayout(2, 2));
                contentPanel.add(panel);
                panel.add(new JLabel(" " + key), BorderLayout.NORTH);
                jTable = new TranslateFileJTable();
                panel.add(jTable, BorderLayout.CENTER);
                tableViewMap.put(key, jTable);
            }
            final Object[][] data = new Object[files.size()][6];
            TableModel dataModel = new AbstractTableModel() {
                String[] columnNames = new String[]{"", "file name", "key", "orientation", "local", "translation"};
                public int getColumnCount() { return columnNames.length; }
                public int getRowCount() { return data.length;}
                public Object getValueAt(int row, int col) {return data[row][col];}
                public String getColumnName(int column) {return columnNames[column];}
                public Class getColumnClass(int c) {return getValueAt(0, c).getClass();}
                public boolean isCellEditable(int row, int col) {return true;}
                public void setValueAt(Object aValue, int row, int column) {
                    data[row][column] = aValue;
                }
            };

            for (int i=0; i<files.size(); ++i) {
                File file = files.get(i);
                dataModel.setValueAt(false, i, 0);
                dataModel.setValueAt(file.getName(), i, 1);
                try {
                    TranslationLocator locator = AdditAssist.calcTranslationLocator(
                            ExcelUtil.getExcelString(ExcelUtil.getWorkbook(file), 0, 0, 0 ), null);
                    if(locator != null) {
                        dataModel.setValueAt(true, i, 0);
                        if(locator.getKeyLocator() != null) {
                            dataModel.setValueAt(locator.getKeyLocator(), i, 2);
                        }
                        if(locator.getOrientation() != null) {
                            dataModel.setValueAt(GlobalConstant.Orientation.values()[locator.getOrientation()].toString().toLowerCase(), i, 3);
                        }
                        if(locator.getLocalLocator() != null) {
                            dataModel.setValueAt(locator.getLocalLocator(), i, 4);
                        }
                        if(locator.getTranslationLocator() != null) {
                            dataModel.setValueAt(locator.getTranslationLocator(), i, 5);
                        }
                    }
                } catch (IOException e) {
                    LoggerUtil.error(e.getMessage());
                }
            }
            jTable.setModel(dataModel);
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
        Set<String> keys = tableViewMap.keySet();
        for (String key : keys) {
            JTable tableView = tableViewMap.get(key);
            DefaultTableModel tableModel = (DefaultTableModel) tableView.getModel();
            for (int i=0; i<tableModel.getRowCount(); ++i) {
                tableModel.setValueAt(checked, i, 0);
            }
        }
    }

    private void cleanSelectPath() {
        excelFiles.clear();
        tableViewMap.clear();
        contentPanel.removeAll();
        contentPanel.updateUI();
    }

    private void addInputPath() {
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
                        if(entry.isFile()  && !entry.getName().startsWith("~$")
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

    public HashMap<String, JTable> getTableViewMap() {
        return tableViewMap;
    }
}
