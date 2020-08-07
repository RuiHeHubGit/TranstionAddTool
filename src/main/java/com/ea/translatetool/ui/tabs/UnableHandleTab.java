package com.ea.translatetool.ui.tabs;

import com.ea.translatetool.addit.Addit;
import com.ea.translatetool.addit.mode.Translation;
import com.ea.translatetool.ui.UI;
import com.ea.translatetool.ui.component.ZebraStripeJTable;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

public class UnableHandleTab extends JPanel {
    private UI ui;
    private SpringLayout springLayout;
    private ZebraStripeJTable jtTranslation;

    public UnableHandleTab(UI ui) {
        this.ui = ui;
    }

    public void init() {
        initTableView();
        showTable();
    }

    public void update() {
        showTable();
    }

    private void showTable() {
        ConcurrentLinkedQueue<Translation> noFoundLocaleList = Addit.getNoFoundLocaleList();
        if(noFoundLocaleList == null || noFoundLocaleList.isEmpty()) {
            return;
        }

        DefaultTableModel tableModel = (DefaultTableModel) jtTranslation.getModel();
        tableModel.setRowCount(noFoundLocaleList.size());
        int row = 0;
        for (Translation translation : noFoundLocaleList) {
            tableModel.setValueAt(true, row, 0);
            tableModel.setValueAt(translation.getLocal(), row, 1);
            tableModel.setValueAt(translation.getKey(), row, 2);
            tableModel.setValueAt(translation.getTranslation(), row, 3);
            tableModel.setValueAt(translation.getFile().getAbsolutePath(), row, 4);
            ++row;
        }
    }

    private void initTableView() {
        setLayout(new BorderLayout(5, 5));
        springLayout = new SpringLayout();
        jtTranslation = new ZebraStripeJTable(new Object[][]{}, new Object[]{"", "locale", "key", "translation", "path"});
        jtTranslation.setColumnToCheckBox(0, true);
        jtTranslation.getColumnModel().getColumn(0).setMaxWidth(28);
        JScrollPane listScrollPage = new JScrollPane(jtTranslation);
        listScrollPage.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        listScrollPage.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        add(listScrollPage, BorderLayout.CENTER);
    }

    public Set<String> getIgnoreLocaleSet() {
        Set<String> ignoreLocaleSet = new TreeSet<>();
        if(jtTranslation == null) {
            return ignoreLocaleSet;
        }
        int rowCount = jtTranslation.getRowCount();
        for (int i=0; i<rowCount; ++i) {
            if(!(boolean) jtTranslation.getValueAt(i, 0)) {
                ignoreLocaleSet.add(jtTranslation.getValueAt(i, 1).toString());
            }
        }
        return ignoreLocaleSet;
    }
}
