package com.ea.translatetool.ui.tabs;

import com.ea.translatetool.addit.AdditAssist;
import com.ea.translatetool.addit.mode.TranslationLocator;
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
    private UI parent;
    private JPanel contentPanel;
    private HashMap<String, java.util.List<File>> excelFiles;

    public InputTab(UI parent) {
        this.parent = parent;
        this.excelFiles = new HashMap<>();
        setLayout(new BorderLayout());
        initBtnGroup();
        initInputFileListView();
    }

    private void initInputFileListView() {
        contentPanel = new JPanel(new GridLayout(0, 1, 10, 10));
        JScrollPane listScrollPage = new JScrollPane(contentPanel);
        listScrollPage.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        listScrollPage.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        add(listScrollPage, BorderLayout.CENTER);

        showFileList();
    }

    private void showFileList() {
        contentPanel.removeAll();

        Set<String> keys = excelFiles.keySet();
        for (String key : keys) {
            List<File> files = excelFiles.get(key);
            if(!files.isEmpty()) {
                JPanel panel = new JPanel(new BorderLayout(2, 2));
                contentPanel.add(panel);
                panel.add(new JLabel(" "+key), BorderLayout.NORTH);
                Object[][] data = new Object[files.size()][6];
                for (int i=0; i<files.size(); ++i) {
                    data[i][0] = true;
                    File file = files.get(i);
                    data[i][1] = file.getName();
                    try {
                        TranslationLocator locator = AdditAssist.calcTranslationLocator(
                                ExcelUtil.getExcelString(ExcelUtil.getWorkbook(file), 0, 0, 0 ), null);
                        if(locator != null) {
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
                TableModel tableModel = new DefaultTableModel(data,
                        new Object[]{new JCheckBox(), "file name", "key", "orientation", "local", "translation"});
                JTable jTable = new TranslateFileJTable(tableModel);
                panel.add(jTable, BorderLayout.CENTER);
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
        JButton btnInvertSelect = new JButton("invert select");
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
                    selectAllPath();
                    break;
                case "invert select":
                    InvertSelect();
                    break;
            }
        }
    }

    private void InvertSelect() {

    }

    private void selectAllPath() {

    }

    private void cleanSelectPath() {

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

            boolean addNew = false;
            for (File file : files) {
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
            }

            if(addNew) {
                showFileList();
            }
        }

    }
}
