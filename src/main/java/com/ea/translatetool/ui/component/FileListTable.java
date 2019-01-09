package com.ea.translatetool.ui.component;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class FileListTable extends JTable {

    private DefaultTableCellRenderer tcr;
    private JCheckBoxEditor jCheckBoxEditor;
    private JCheckBoxTableCellRenderer jCheckBoxTableCellRenderer;

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

        jCheckBoxEditor = new JCheckBoxEditor(true);
        jCheckBoxTableCellRenderer = new JCheckBoxTableCellRenderer();
    }

    @Override
    public TableCellEditor getCellEditor(int row, int column) {
        if(column == 1) {
            return jCheckBoxEditor;
        }
        return super.getCellEditor(row, column);
    }

    @Override
    public TableCellRenderer getCellRenderer(int row, int column) {
        if(column == 1) {
            TableColumn firstColumn = getColumnModel().getColumn(1);
            firstColumn.setPreferredWidth(30);
            firstColumn.setMaxWidth(30);
            firstColumn.setMinWidth(30);
            return jCheckBoxTableCellRenderer;
        }
        return tcr;
    }

    class JCheckBoxEditor extends DefaultCellEditor {
        public JCheckBoxEditor(boolean checked) {
            super(new JCheckBox(null, null, checked));
        }
    }

    class JCheckBoxTableCellRenderer implements TableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            return new JCheckBox(null, null, (Boolean) value);
        }
    }
}
