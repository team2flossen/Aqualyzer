package com.aqualyzer.ui;

import com.aqualyzer.core.enums.QualityRating;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.util.EnumMap;

public class QualityRatingCellRenderer extends DefaultTableCellRenderer {

    private final EnumMap<QualityRating, Icon> icons;
    private final EnumMap<QualityRating, String> names;


    public QualityRatingCellRenderer() {
        icons = new EnumMap<>(QualityRating.class);
        var acceptIconUrl = getClass().getClassLoader().getResource("icons/accept.png");
        var unknownIconUrl = getClass().getClassLoader().getResource("icons/help.png");
        var errorIconUrl = getClass().getClassLoader().getResource("icons/error.png");
        var exclamationIconUrl = getClass().getClassLoader().getResource("icons/exclamation.png");
        icons.put(QualityRating.Good,       acceptIconUrl != null ? new ImageIcon(acceptIconUrl) : null);
        icons.put(QualityRating.OK,         acceptIconUrl != null ? new ImageIcon(acceptIconUrl) : null);
        icons.put(QualityRating.Unknown,    unknownIconUrl != null ? new ImageIcon(unknownIconUrl) : null);
        icons.put(QualityRating.Risk,       errorIconUrl != null ? new ImageIcon(errorIconUrl) : null);
        icons.put(QualityRating.Poor,       exclamationIconUrl != null ? new ImageIcon(exclamationIconUrl) : null);
        icons.put(QualityRating.Critical,   exclamationIconUrl != null ? new ImageIcon(exclamationIconUrl) : null);

        names = new EnumMap<>(QualityRating.class);
        names.put(QualityRating.Good, "Gut");
        names.put(QualityRating.OK, "OK");
        names.put(QualityRating.Unknown, "Nicht ausgewertet");
        names.put(QualityRating.Risk, "Risiko");
        names.put(QualityRating.Poor, "Schlecht");
        names.put(QualityRating.Critical, "Kritisch");
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
                                                   boolean isSelected, boolean hasFocus,
                                                   int row, int column) {
        var c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        if (value instanceof QualityRating q) {
            setIcon(icons.getOrDefault(q, null));
            setText(names.getOrDefault(q, "Unbekannt"));
            if (value == QualityRating.Critical){
                c.setFont(c.getFont().deriveFont(Font.BOLD));
            }
        } else {
            setIcon(null);
        }
        return c;
    }

}
