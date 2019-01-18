package com.ea.translatetool.ui.component;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;

public class ZebraStripeJTable extends JTable {
    private JCheckBoxEditor jCheckBoxEditor;
    private JComboBoxEditor jComboBoxEditor;
    private JCheckBoxTableCellRenderer jCheckBoxTableCellRenderer;
    private JComboBoxTableCellRenderer jComboBoxTableCellRenderer;
    private DefaultTableCellRenderer tcr;
    private HashMap<Integer, Object> jBoxMap;
    private HashMap<Integer, Color> rowColorMap;
    private int checkBoxWidth;

    public ZebraStripeJTable(TableModel dataModel) {
        super(dataModel);
        init();
    }

    public ZebraStripeJTable() {
        this(new DefaultTableModel());
    }

    public ZebraStripeJTable(Object[][] data, Object[] columnNames) {
        this(new DefaultTableModel(data, columnNames));
    }

    private void init() {
        checkBoxWidth = 30;
        jBoxMap = new HashMap<>();
        rowColorMap = new HashMap<>();
        setRowHeight(20);
        getTableHeader().setReorderingAllowed(false);
        tcr = new DefaultTableCellRenderer(){
            private static final long serialVersionUID = 1L;
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column){
                if(row%2 == 0)
                    setBackground(Color.WHITE);//设置奇数行底色
                else if(row%2 == 1)
                    setBackground(new Color(220,230,241));//设置偶数行底色
                Color textColor = rowColorMap.get(row);
                if(textColor != null) {
                    setForeground(textColor);
                } else {
                    setForeground(Color.BLACK);
                }
                return super.getTableCellRendererComponent(table, value,isSelected, hasFocus, row, column);
            }
        };
    }

    public void setColumnToCheckBox(int index, boolean selected) {
        if(jCheckBoxTableCellRenderer == null) {
            jCheckBoxTableCellRenderer = new JCheckBoxTableCellRenderer();
        }
        if(jCheckBoxEditor == null) {
            jCheckBoxEditor = new JCheckBoxEditor(selected);
        }
        jBoxMap.put(index, selected);
    }

    public void setColumnToComboBox(int index, String[] items) {
        if(jComboBoxTableCellRenderer == null) {
            jComboBoxTableCellRenderer = new JComboBoxTableCellRenderer();
        }
        if(jComboBoxEditor == null) {
            jComboBoxEditor = new JComboBoxEditor(items);
        }
        jBoxMap.put(index, items);
    }

    public void cancelColumnToBox(int index) {
        if(jBoxMap == null) {
            return;
        }
        jBoxMap.remove(index);
    }

    public void setCheckBoxWidth(int checkBoxWidth) {
        this.checkBoxWidth = checkBoxWidth;
    }

    @Override
    public Class<?> getColumnClass(int column) {
        return super.getColumnClass(column);
    }

    @Override
    public TableCellEditor getCellEditor(int row, int column) {
        Object boxMode = jBoxMap.get(column);
        if(boxMode instanceof Boolean) {
            return jCheckBoxEditor;
        } else if(boxMode instanceof String[]) {
            return jComboBoxEditor;
        }
        return super.getCellEditor(row, column);
    }

    @Override
    public TableCellRenderer getCellRenderer(int row, int column) {
        Object boxMode = jBoxMap.get(column);
        if(boxMode instanceof Boolean) {
            TableColumn tableColumn = getColumnModel().getColumn(column);
            tableColumn.setMaxWidth(checkBoxWidth);
            tableColumn.setPreferredWidth(checkBoxWidth);
            tableColumn.setMinWidth(checkBoxWidth);
            tableColumn.setHeaderRenderer(
                    new TableHeaderCheckBoxCellRenderer(column));
            return jCheckBoxTableCellRenderer;
        }
        return tcr;
    }

    private JCheckBox createJCheckBox(boolean isSelected) {
        JCheckBox jCheckBox = new JCheckBox(null, null, isSelected);
        jCheckBox.setMargin(new Insets(0, 5, 0, 0));
        return jCheckBox;
    }

    public void setRowTextColor(int rowIndex, Color color) {
        rowColorMap.put(rowIndex, color);
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

    class JCheckBoxTableCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            if(value == null) {
                value = jBoxMap.get(column);
            }
            return createJCheckBox((Boolean) value);
        }
    }

    class JComboBoxTableCellRenderer implements TableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            JComboBox jComboBox = new JComboBox<>((String[]) jBoxMap.get(column));
            jComboBox.setSelectedItem(value);
            return jComboBox;
        }
    }

    class TableHeaderCheckBoxCellRenderer implements TableCellRenderer {
        private JCheckBox checkBox;
        public TableHeaderCheckBoxCellRenderer(int index) {
            tableHeader = ZebraStripeJTable.this.getTableHeader();
            checkBox = new JCheckBox();
            checkBox.setSelected((Boolean) jBoxMap.get(index));

            tableHeader.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (e.getClickCount() > 0) {
                        int selectColumn = tableHeader.columnAtPoint(e.getPoint());
                        if (jBoxMap.get(selectColumn) instanceof Boolean) {
                            Boolean selected = (Boolean) jBoxMap.get(selectColumn);
                            jBoxMap.put(selectColumn, !selected);
                            checkBox.setSelected(!selected);
                            DefaultTableModel tableModel = (DefaultTableModel) ZebraStripeJTable.this.getModel();
                            for (int i=0; i<tableModel.getRowCount(); ++i) {
                                ZebraStripeJTable.this.setValueAt(!selected, i, selectColumn);
                            }
                            tableHeader.repaint();
                        }
                    }
                }
            });
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            return checkBox;
        }
    }
}
