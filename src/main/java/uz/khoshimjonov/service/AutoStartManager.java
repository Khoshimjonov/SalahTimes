package uz.khoshimjonov.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

public class AutoStartManager {

    private static final String REG_PATH = "HKCU\\Software\\Microsoft\\Windows\\CurrentVersion\\Run";
    private static final String APP_NAME = "SalahTimes";

    public static boolean isEnabled() {
        try {
            Process process = new ProcessBuilder("reg", "query", REG_PATH, "/v", APP_NAME)
                    .redirectErrorStream(true).start();
            try (BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = br.readLine()) != null) {
                    if (line.contains(APP_NAME)) {
                        return true;
                    }
                }
            }
        } catch (Exception ignored) {}
        return false;
    }

    public static boolean enable() {
        String command = getStartupCommand();
        try {
            new ProcessBuilder("reg", "add", REG_PATH, "/v", APP_NAME, "/t", "REG_SZ", "/d", command, "/f").start().waitFor();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean disable() {
        try {
            new ProcessBuilder("reg", "delete", REG_PATH, "/v", APP_NAME, "/f").start().waitFor();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private static String getStartupCommand() {
        String javaw = System.getProperty("java.home") + File.separator + "bin" + File.separator + "javaw.exe";
        String classPath = System.getProperty("java.class.path");
        File cpFile = new File(classPath);
        // If running from a jar, use -jar
        if (cpFile.isFile() && classPath.toLowerCase().endsWith(".jar")) {
            return quote(javaw) + " -jar " + quote(cpFile.getAbsolutePath());
        }
        // Otherwise use classpath + main class
        return quote(javaw) + " -cp " + quote(classPath) + " uz.khoshimjonov.Main";
    }

    private static String quote(String s) {
        return '"' + s + '"';
    }
}
