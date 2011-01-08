/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.botthoughts;

/**
 *
 * @author Michael Shimniok
 */
public class OperatingSystemValidator {

    public static String getOS() {
        String os = "";
        if (isWindows()) {
            os = "Windows";
        } else if (isMac()) {
            os = "OSX";
        } else if (isLinux()) {
            os = "Linux";
        }

        return os;
    }

    public static boolean isWindows() {

        String os = System.getProperty("os.name").toLowerCase();
        //windows
        return (os.indexOf("win") >= 0);

    }

    public static boolean isMac() {

        String os = System.getProperty("os.name").toLowerCase();
        //Mac
        return (os.indexOf("mac") >= 0);

    }

    public static boolean isLinux() {

        String os = System.getProperty("os.name").toLowerCase();
        //linux or unix
        return (os.indexOf("linux") >= 0);

    }

    public static boolean isUnix() {

        String os = System.getProperty("os.name").toLowerCase();
        //linux or unix
        return (os.indexOf("nix") >= 0);

    }
}