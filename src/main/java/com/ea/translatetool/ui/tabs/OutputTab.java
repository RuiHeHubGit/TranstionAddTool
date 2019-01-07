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
        setLayout(new BorderLayout(0, 5));
        initTopPanel();
    }

    private void initTopPanel() {
        JPanel topPane = new JPanel(new BorderLayout(5,5));
        add(topPane, BorderLayout.NORTH);
        JComboBox jcbOutType = new JComboBox(GlobalConstant.OutType.values());
        jcbOutType.addItemListener(this);
        topPane.add(jcbOutType, BorderLayout.WEST);

        JPanel centerPanel = new JPanel(new BorderLayout(5, 0));
        centerPanel.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 0));
        centerPanel.add(new JLabel("out path:"), BorderLayout.WEST);
        tfOutPath = new JTextField();
        centerPanel.add(tfOutPath, BorderLayout.CENTER);

        topPane.add(centerPanel, BorderLayout.CENTER);
        JButton btnSelect = new JButton("select");
        btnSelect.addActionListener(this);
        topPane.add(btnSelect, BorderLayout.EAST);

        tfOutPath.setText(workConfig.getOutput().getAbsolutePath());
        showFileListTable();
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
                String suffix = GlobalConstant.OutType.TYPE_JSON.getValue();
                if(workConfig.getOutType() == GlobalConstant.OutType.TYPE_PRO) {
                    suffix = GlobalConstant.OutType.TYPE_PRO.getValue();
                }
                return entry.getName().toLowerCase().endsWith(suffix);
            }
        });

        tableData = new Object[fileList.size()][3];
        for (int i=0; i<fileList.size(); ++i) {
            File file = fileList.get(i);
            tableData[i][0] = file.getName();
            String str = file.getName().substring(0, file.getName().lastIndexOf('.'));
            tableData[i][1] = workConfig.getLocalMap().get(str);
            if(tableData[i][1] == null) {
                tableData[i][1] = str;
            }
            tableData[i][2] = workConfig.getOutType().getValue().substring(1);
        }


        if(fileListTable == null) {
            fileListTable = new JTable();
            add(fileListTable, BorderLayout.CENTER);
        }
        tableMode = new DefaultTableModel(tableData, new String[] {"filename", "local", "type"});
        fileListTable.setModel(tableMode);
        fileListTable.getTableHeader().setVisible(true);
        fileListTable.updateUI();
    }

    @Override
    public void itemStateChanged(ItemEvent e) {
        workConfig.setOutType((GlobalConstant.OutType) e.getItem());
        showFileListTable();
    }
}
