package com.ea.translatetool.ui.tabs;

import com.ea.translatetool.config.WorkConfig;
import com.ea.translatetool.constant.GlobalConstant;
import com.ea.translatetool.ui.UI;
import com.ea.translatetool.util.IOUtil;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.nio.file.DirectoryStream;

public class OutputTab extends JPanel implements ActionListener, ItemListener {
    private WorkConfig workConfig;
    private JTextField tfOutPath;
    private JTable fileListTable;
    private JPanel topPane;
    private TableModel tableMode;
    private Object[][] tableData;

    public OutputTab(UI parent) {
        workConfig = parent.getWorkConfig();
        setLayout(new SpringLayout());
        initTopPanel();
        initFileListTable();
        showFileListTable();
    }

    private void initFileListTable() {
        fileListTable = new JTable() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        fileListTable.setRowHeight(20);
        fileListTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer(){
            private static final long serialVersionUID = 1L;
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,int row, int column){
                if(row%2 == 0)
                    setBackground(Color.WHITE);//设置奇数行底色
                else if(row%2 == 1)
                    setBackground(new Color(220,230,241));//设置偶数行底色

                if(fileListTable.getValueAt(row, 2) == null) {
                    setForeground(Color.RED);
                } else {
                    setForeground(Color.BLACK);
                }
                return super.getTableCellRendererComponent(table, value,isSelected, hasFocus, row, column);
            }
        });

        JScrollPane jScrollPane = new JScrollPane(fileListTable);
        add(jScrollPane);
        SpringLayout springLayout = (SpringLayout) getLayout();
        springLayout.putConstraint(SpringLayout.NORTH,  jScrollPane, 5, SpringLayout.SOUTH, topPane);
        springLayout.putConstraint(SpringLayout.SOUTH,  jScrollPane, 0, SpringLayout.SOUTH, this);
        springLayout.putConstraint(SpringLayout.WEST,  jScrollPane, 0, SpringLayout.WEST, this);
        springLayout.putConstraint(SpringLayout.EAST,  jScrollPane, 0, SpringLayout.EAST, this);

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) {
                showFileListTable();
                super.componentShown(e);
            }
        });
    }

    private void initTopPanel() {
        topPane = new JPanel(new BorderLayout(5,5));
        add(topPane);
        SpringLayout springLayout = (SpringLayout) getLayout();
        springLayout.putConstraint(SpringLayout.NORTH,  topPane, 5, SpringLayout.NORTH, this);
        springLayout.putConstraint(SpringLayout.WEST,  topPane, 0, SpringLayout.WEST, this);
        springLayout.putConstraint(SpringLayout.EAST,  topPane, 0, SpringLayout.EAST, this);

        JComboBox jcbOutType = new JComboBox(GlobalConstant.OutType.values());
        jcbOutType.addItemListener(this);
        topPane.add(jcbOutType, BorderLayout.WEST);

        JPanel centerPanel = new JPanel(new BorderLayout(5, 0));
        centerPanel.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 0));
        centerPanel.add(new JLabel("out path:"), BorderLayout.WEST);
        tfOutPath = new JTextField();
        centerPanel.add(tfOutPath, BorderLayout.CENTER);
        tfOutPath.requestFocus();

        topPane.add(centerPanel, BorderLayout.CENTER);
        JButton btnSelect = new JButton("select");
        btnSelect.addActionListener(this);
        topPane.add(btnSelect, BorderLayout.EAST);

        tfOutPath.setText(workConfig.getOutput().getAbsolutePath());
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
            tfOutPath.setText(file.getAbsolutePath());
            showFileListTable();
        }
    }

    public void showFileListTable() {
        java.util.List<File> fileList = IOUtil.fileList(new File(tfOutPath.getText()), false, new  DirectoryStream.Filter<File>() {
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

        tableMode = new DefaultTableModel(tableData, new String[] {"filename", "local", "type"});
        fileListTable.setModel(tableMode);
        fileListTable.getTableHeader().setVisible(true);
        fileListTable.updateUI();
    }

    public String getOutPath() {
        return tfOutPath.getText();
    }

    @Override
    public void itemStateChanged(ItemEvent e) {
        workConfig.setOutType((GlobalConstant.OutType) e.getItem());
        showFileListTable();
    }
}
