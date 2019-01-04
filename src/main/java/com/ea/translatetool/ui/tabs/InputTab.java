package com.ea.translatetool.ui.tabs;

import javax.swing.*;
import java.awt.*;

public class InputTab extends JPanel {

    public InputTab() {
        setLayout(new BorderLayout());
        initBtnGroup();
        initInputFileListView();
    }

    private void initInputFileListView() {
        JPanel contentPanel = new JPanel(new GridLayout(0, 1, 10, 10));
        contentPanel.setBackground(new Color(255, 200, 50));
        JScrollPane listScrollPage = new JScrollPane(contentPanel);
        listScrollPage.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        listScrollPage.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        add(listScrollPage, BorderLayout.CENTER);


        for (int i=0; i< 100; ++i) {
            contentPanel.add(new JButton("test"+i));
        }
    }

    private void initBtnGroup() {
        JPanel btnGroupPanel = new JPanel();
        add(btnGroupPanel, BorderLayout.NORTH);
        btnGroupPanel.setLayout(new BoxLayout(btnGroupPanel, BoxLayout.LINE_AXIS));
        btnGroupPanel.setPreferredSize(new Dimension(0, 40));
        btnGroupPanel.setBackground(new Color(255,3,3));
        JButton btnAdd = new JButton("add");
        JButton btnClean = new JButton("clean");
        JButton btnSelection  = new JButton("selection");
        JButton btnInvertSelection  = new JButton("InvertSelection");
        int hMargin = 15;
        btnGroupPanel.add(Box.createHorizontalStrut(hMargin));
        btnGroupPanel.add(btnAdd);
        btnGroupPanel.add(Box.createHorizontalStrut(hMargin));
        btnGroupPanel.add(btnClean);
        btnGroupPanel.add(Box.createHorizontalStrut(hMargin));
        btnGroupPanel.add(btnSelection);
        btnGroupPanel.add(Box.createHorizontalStrut(hMargin));
        btnGroupPanel.add(btnInvertSelection);
        btnGroupPanel.add(Box.createHorizontalStrut(hMargin));
    }
}
