package com.jacker.plugin.music.window;

import com.intellij.ui.Gray;
import com.intellij.ui.JBColor;
import com.jacker.plugin.music.dto.Track;
import icons.MusicIcons;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.util.Objects;

public class MusicTableCellRenderer extends DefaultTableCellRenderer {

    private final Color evenRowColor = new JBColor(Gray._233, Gray._55);//奇数行颜色

    private final Color oddRowColor = new JBColor(Gray._230, new Color(59, 63, 65));//偶数行颜色

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        Component tableCellRendererComponent = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        this.setColor(tableCellRendererComponent, table, row, column);
        return tableCellRendererComponent;
    }

    private void setColor(Component component, JTable table, int row, int column) {
        Track track = (Track) table.getModel().getValueAt(row, 0);
        component.setForeground(new JBColor(Gray._100, Gray._181));
        ((JLabel) component).setIcon(MusicIcons.EMPEROR_NEW_CLOTHES);
        if (Objects.nonNull(track) && (Objects.equals(track.getCanPlay(), false) || Objects.equals(track.getTryPlay(), true))) {
            component.setForeground(new JBColor(Gray._161, Gray._121));
        } else {
            component.setForeground(new JBColor(Gray._100, Gray._181));
        }
        if (Objects.nonNull(track) && Objects.equals(track.getCurrentPlay(), true)) {
            component.setForeground(new JBColor(new Color(192, 59, 50), new Color(192, 59, 50)));
            if (column == 0) {
                ((JLabel) component).setIcon(MusicIcons.CURRENT);
            }
        }

        if (row % 2 == 0) {
            component.setBackground(evenRowColor);
        } else {
            component.setBackground(oddRowColor);
        }
    }
}
