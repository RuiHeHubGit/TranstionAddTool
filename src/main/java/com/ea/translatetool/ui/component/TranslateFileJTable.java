package com.ea.translatetool.ui.component;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;

/**
 * Created by HeRui on 2019/1/6.
 */
public class TranslateFileJTable extends JTable{
    private JCheckBoxEditor jCheckBoxEditor;
    private JComboBoxEditor jComboBoxEditor;
    private JCheckBoxTableCellRenderer jCheckBoxTableCellRenderer;
    private DefaultTableCellRenderer tcr;

    public TranslateFileJTable(TableModel tableModel) {
        super(tableModel);
        getTableHeader().setReorderingAllowed(false);
        TableColumn firstColumn = getColumnModel().getColumn(0);
        firstColumn.setPreferredWidth(25);
        firstColumn.setMaxWidth(25);
        firstColumn.setMinWidth(25);

        setRowHeight(20);

        jCheckBoxEditor = new JCheckBoxEditor(true);
        jComboBoxEditor = new JComboBoxEditor(new String[]{"vertical", "horizontal"});

        jCheckBoxTableCellRenderer = new JCheckBoxTableCellRenderer();

        tcr = new DefaultTableCellRenderer(){
            private static final long serialVersionUID = 1L;
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,int row, int column){
                if(row%2 == 0)
                    setBackground(Color.WHITE);//设置奇数行底色
                else if(row%2 == 1)
                    setBackground(new Color(220,230,241));//设置偶数行底色

                if(getValueAt(row, 2) == null) {
                    setForeground(Color.RED);
                } else {
                    setForeground(Color.BLACK);
                }
                return super.getTableCellRendererComponent(table, value,isSelected, hasFocus, row, column);
            }
        };
    }

    @Override
    public TableCellEditor getCellEditor(int row, int column) {
        if(column == 0) {
            return jCheckBoxEditor;
        } else if(column == 3) {
            return jComboBoxEditor;
        }
        return super.getCellEditor(row, column);
    }

    @Override
    public TableCellRenderer getCellRenderer(int row, int column) {
        if(column == 0) {
            return jCheckBoxTableCellRenderer;
        }
        return tcr;
    }

    class JCheckBoxEditor extends DefaultCellEditor {

        public JCheckBoxEditor(boolean checked) {
            super(new JCheckBox(null, null, checked));
        }
    }

    class JComboBoxEditor extends DefaultCellEditor {

        public JComboBoxEditor(String[] items) {
            super(new JComboBox<>(items));
        }
    }

    class JCheckBoxTableCellRenderer implements TableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            return new JCheckBox(null, null, (Boolean) value);
        }
    }

    class JComboBoxTableCellRenderer implements TableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            JComboBox jComboBox = new JComboBox<>(new String[]{"vertical", "horizontal"});
            jComboBox.setSelectedItem(value);
            return jComboBox;
        }
    }
}
