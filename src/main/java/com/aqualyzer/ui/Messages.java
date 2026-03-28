package com.aqualyzer.ui;

import javax.swing.*;
import java.awt.*;
import java.text.MessageFormat;
import java.util.ResourceBundle;

public final class Messages {

    private static ResourceBundle bundle;

    private Messages() {}

    private static ResourceBundle getBundle() {
        if (bundle == null) {
            bundle = ResourceBundle.getBundle("messages");
        }
        return bundle;
    }

    public static String get(String key) {
        return getBundle().getString(key);
    }

    public static String get(String key, Object... args) {
        return MessageFormat.format(getBundle().getString(key), args);
    }

    /**
     * Findet ein JLabel mit dem gegebenen Text im Container-Baum und setzt den neuen Text.
     * Nützlich für Labels, die als lokale Variablen im generierten $$$setupUI$$$()-Code existieren.
     */
    public static void relabel(Container container, String currentText, String newText) {
        for (var comp : container.getComponents()) {
            if (comp instanceof JLabel label && currentText.equals(label.getText())) {
                label.setText(newText);
            }
            if (comp instanceof Container c) {
                relabel(c, currentText, newText);
            }
        }
    }
}
