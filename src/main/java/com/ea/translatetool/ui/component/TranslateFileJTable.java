package com.ea.translatetool.ui.component;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.table.*;
import java.awt.*;
import java.awt.geom.Rectangle2D;

/**
 * Created by HeRui on 2019/1/6.
 */
public class TranslateFileJTable extends JTable{
    private TableModel tableModel;
    private JCheckBoxEditor jCheckBoxEditor;
    private JComboBoxEditor jComboBoxEditor;
    private JCheckBoxTableCellRenderer jCheckBoxTableCellRenderer;
    private DefaultTableCellRenderer tcr;

    public TranslateFileJTable() {
        this(null);
    }

    public TranslateFileJTable(TableModel tableModel) {
        super(tableModel);
        this.tableModel = tableModel;
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
    public void setModel(TableModel dataModel) {
        super.setModel(dataModel);

        if(dataModel == null) {
            return;
        }
        if(getColumnModel().getColumnCount() > 0) {
            TableColumn firstColumn = getColumnModel().getColumn(0);
            firstColumn.setPreferredWidth(28);
            firstColumn.setMaxWidth(28);
            firstColumn.setMinWidth(28);
        }
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

    private JCheckBox createJCheckBox(boolean isSelected) {
        JCheckBox jCheckBox = new JCheckBox(null, null, isSelected);
        jCheckBox.setMargin(new Insets(0, 5, 0, 0));
        return jCheckBox;
    }

    class JCheckBoxEditor extends DefaultCellEditor {

        public JCheckBoxEditor(boolean checked) {
            super(createJCheckBox(checked));
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
            return createJCheckBox((Boolean.valueOf(value.toString())) );
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
