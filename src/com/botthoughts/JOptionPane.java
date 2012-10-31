/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.botthoughts;

import java.awt.Component;
import javax.swing.Icon;
import javax.swing.ImageIcon;

/**
 *
 * @author mes
 */
public class JOptionPane extends javax.swing.JOptionPane {
    private static ImageIcon appIcon;
    private static int result;

    public static void showMessageDialog(Component parentComponent, Object message, String title, int messageType) {
        if (PlatformUtilities.isMac()) {
            javax.swing.JOptionPane.showMessageDialog(parentComponent, message, title, messageType, appIcon);
        } else {
            javax.swing.JOptionPane.showMessageDialog(parentComponent, message, title, messageType);
        }
    }

    public static int showConfirmDialog(Component parentComponent, Object message, String title, int optionType, int messageType) {
        if (PlatformUtilities.isMac()) {
            result = javax.swing.JOptionPane.showConfirmDialog(parentComponent, message, title, optionType, messageType, appIcon);
        } else {
            result = javax.swing.JOptionPane.showConfirmDialog(parentComponent, message, title, optionType, messageType);
        }
        return result;
    }

    public static int showOptionDialog(Component parentComponent, Object message,
                                   String title, int optionType, int messageType,
                                   Icon icon, Object[] options, Object initialValue) {
        if (PlatformUtilities.isMac()) {
            result = javax.swing.JOptionPane.showOptionDialog(parentComponent, message, title, optionType, messageType, appIcon, options, initialValue);
        } else {
            result = javax.swing.JOptionPane.showOptionDialog(parentComponent, message, title, optionType, messageType, icon, options, initialValue);
        }
        return result;
    }

    /**
     * @param appIcon the appIcon to set
     */
    public static void setAppIcon(ImageIcon appIcon) {
        JOptionPane.appIcon = appIcon;
    }

}
