package com.ea.translatetool.ui.tabs;

import com.ea.translatetool.config.WorkConfig;
import com.ea.translatetool.constant.GlobalConstant;
import com.ea.translatetool.ui.UI;
import com.ea.translatetool.util.IOUtil;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;

public class OutputTab extends JPanel implements ActionListener, ItemListener {
    private WorkConfig workConfig;
    private JTextField tfOutPath;
    private JTable fileListTable;
    private TableModel tableMode;
    private Object[][] tableData;

    public OutputTab(UI parent) {
        workConfig = parent.getWorkConfig();
        setLayout(new BorderLayout());
        initTopPanel();
    }

    private void initTopPanel() {
        JPanel topPane = new JPanel(new BorderLayout(5,5));
        add(topPane, BorderLayout.NORTH);
        JPanel westPanel = new JPanel(new GridLayout(1, 2, 15, 5));
        JComboBox jcbOutType = new JComboBox(GlobalConstant.OutType.values());
        jcbOutType.addItemListener(this);
        westPanel.add(jcbOutType);
        westPanel.add(new JLabel("out path:"));
        topPane.add(westPanel, BorderLayout.WEST);
        tfOutPath = new JTextField();
        topPane.add(tfOutPath, BorderLayout.CENTER);
        JButton btnSelect = new JButton("select");
        btnSelect.addActionListener(this);
        topPane.add(btnSelect, BorderLayout.EAST);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object tag = e.getSource();
        if(tag instanceof JButton) {
            JButton button = (JButton) tag;
            if(button.getText().endsWith("select")) {
                selectOutPath();
            }
        }
    }

    private void selectOutPath() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fileChooser.setMultiSelectionEnabled(false);

        if(JFileChooser.APPROVE_OPTION == fileChooser.showDialog(this, "select")) {
            File file = fileChooser.getSelectedFile();
            workConfig.setOutput(file);
            tfOutPath.setText(file.getAbsolutePath());
            showFileListTable();
        }
    }

    private void showFileListTable() {
        java.util.List<File> fileList = IOUtil.fileList(workConfig.getOutput(), false, new  DirectoryStream.Filter<File>() {
            @Override
            public boolean accept(File entry) {
                String suffix = ".json";
                if(workConfig.getOutType() == GlobalConstant.OutType.TYPE_PRO) {
                    suffix = ".properties";
                }
                return entry.getName().toUpperCase().endsWith(suffix);
            }
        });

        tableData = new Object[fileList.size()][3];
        tableMode = new DefaultTableModel(tableData, new String[] {"filename", "local", "type"});
        if(fileListTable != null) {
            remove(fileListTable);
        }
        fileListTable = new JTable(tableMode);
        add(fileListTable, BorderLayout.CENTER);

        for (int i=0; i<fileList.size(); ++i) {
            File file = fileList.get(i);
            tableData[i][0] = file.getName();
            tableData[i][1] = workConfig.getTranslationLocatorMap().get(file.getName());
            tableData[i][2] = workConfig.getOutType();
        }
    }

    @Override
    public void itemStateChanged(ItemEvent e) {
        workConfig.setOutType((GlobalConstant.OutType) e.getItem());
    }
}
