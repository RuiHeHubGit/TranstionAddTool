package com.ea.translatetool.ui.component;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class FileListTable extends JTable {

    private DefaultTableCellRenderer tcr;
    private JButtonTableCellRenderer jButtonTableCellRenderer;
    private ActionListener btnActionListener;

    public FileListTable() {

        setRowHeight(20);

        tcr = new DefaultTableCellRenderer(){
            private static final long serialVersionUID = 1L;
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column){
                if(row%2 == 0)
                    setBackground(Color.WHITE);//设置奇数行底色
                else if(row%2 == 1)
                    setBackground(new Color(220,230,241));//设置偶数行底色
                return super.getTableCellRendererComponent(table, value,isSelected, hasFocus, row, column);
            }
        };

        btnActionListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                DefaultTableModel tableModel = (DefaultTableModel) getModel();
                tableModel.removeRow(getSelectedRow());
                updateUI();
            }
        };

        jButtonTableCellRenderer = new JButtonTableCellRenderer();
    }

    @Override
    public boolean isCellEditable(int row, int column) {
        return false;
    }

    @Override
    public TableCellRenderer getCellRenderer(int row, int column) {
        if(column == 1) {
            TableColumn firstColumn = getColumnModel().getColumn(1);
            firstColumn.setPreferredWidth(70);
            firstColumn.setMaxWidth(70);
            firstColumn.setMinWidth(70);
            return jButtonTableCellRenderer;
        }
        return tcr;
    }

    class JButtonTableCellRenderer implements TableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            JButton btnRemove = new JButton((String) value);
            btnRemove.addActionListener(btnActionListener);
            return btnRemove;
        }
    }
}
