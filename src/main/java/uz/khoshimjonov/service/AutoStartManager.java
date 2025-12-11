package uz.khoshimjonov.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;

public class AutoStartManager {

    private static final String REG_PATH = "HKCU\\Software\\Microsoft\\Windows\\CurrentVersion\\Run";
    private static final String APP_NAME = "SalahTimes";
    private static final String EXE_NAME = "SalahTimesWidget.exe"; // Must match jpackage --name

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
        if (command == null) {
            System.err.println("AutoStartManager: Could not determine startup command");
            return false;
        }
        try {
            Process process = new ProcessBuilder("reg", "add", REG_PATH, "/v", APP_NAME, "/t", "REG_SZ", "/d", command, "/f")
                    .redirectErrorStream(true).start();
            int exitCode = process.waitFor();
            if (exitCode == 0) {
                System.out.println("AutoStartManager: Enabled with command: " + command);
                return true;
            } else {
                System.err.println("AutoStartManager: reg add failed with exit code " + exitCode);
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean disable() {
        try {
            Process process = new ProcessBuilder("reg", "delete", REG_PATH, "/v", APP_NAME, "/f")
                    .redirectErrorStream(true).start();
            process.waitFor();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private static String getStartupCommand() {
        // First, check if running as a jpackage app
        String exePath = getPackagedExePath();
        if (exePath != null) {
            return quote(exePath);
        }

        // Fallback: running from JAR or IDE
        return getJarStartupCommand();
    }

    /**
     * Attempts to find the exe path when running as a jpackage application.
     * jpackage sets several system properties we can use.
     */
    private static String getPackagedExePath() {
        // Method 1: Check jpackage.app-path (set by jpackage on some versions)
        String appPath = System.getProperty("jpackage.app-path");
        if (appPath != null && Files.exists(Path.of(appPath))) {
            return appPath;
        }

        // Method 2: Derive from java.home
        // In jpackage app: java.home points to <app-dir>/runtime
        // The exe is at <app-dir>/AppName.exe
        String javaHome = System.getProperty("java.home");
        if (javaHome != null) {
            Path javaHomePath = Path.of(javaHome);

            // Check if this looks like a jpackage runtime
            // jpackage structure: AppName/runtime/... (java.home = AppName/runtime)
            Path appDir = javaHomePath.getParent();
            if (appDir != null) {
                Path exePath = appDir.resolve(EXE_NAME);
                if (Files.exists(exePath)) {
                    return exePath.toAbsolutePath().toString();
                }

                // Also check if runtime is nested deeper
                // Some structures: AppName/app/runtime or similar
                Path parentExe = appDir.getParent();
                if (parentExe != null) {
                    exePath = parentExe.resolve(EXE_NAME);
                    if (Files.exists(exePath)) {
                        return exePath.toAbsolutePath().toString();
                    }
                }
            }
        }

        // Method 3: Check if we're in a directory structure that has the exe
        try {
            Path currentDir = Path.of("").toAbsolutePath();
            Path exeInCurrent = currentDir.resolve(EXE_NAME);
            if (Files.exists(exeInCurrent)) {
                return exeInCurrent.toString();
            }

            // Check parent
            Path exeInParent = currentDir.getParent().resolve(EXE_NAME);
            if (Files.exists(exeInParent)) {
                return exeInParent.toString();
            }
        } catch (Exception ignored) {}

        return null;
    }

    /**
     * Creates a startup command for JAR-based execution (development/non-packaged).
     */
    private static String getJarStartupCommand() {
        try {
            String javaw = System.getProperty("java.home") + File.separator + "bin" + File.separator + "javaw.exe";

            // Check if javaw exists
            if (!Files.exists(Path.of(javaw))) {
                System.err.println("AutoStartManager: javaw.exe not found at " + javaw);
                return null;
            }

            String classPath = System.getProperty("java.class.path");
            if (classPath == null || classPath.isEmpty()) {
                return null;
            }

            File cpFile = new File(classPath);

            // If running from a jar, use -jar
            if (cpFile.isFile() && classPath.toLowerCase().endsWith(".jar")) {
                return quote(javaw) + " -jar " + quote(cpFile.getAbsolutePath());
            }

            // Otherwise use classpath + main class (IDE mode)
            return quote(javaw) + " -cp " + quote(classPath) + " uz.khoshimjonov.Main";

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static String quote(String s) {
        // Escape any existing quotes and wrap in quotes
        return "\"" + s.replace("\"", "\\\"") + "\"";
    }

    /**
     * Debug method to print startup detection info.
     * Call this to troubleshoot autostart issues.
     */
    public static void printDebugInfo() {
        System.out.println("=== AutoStartManager Debug Info ===");
        System.out.println("java.home: " + System.getProperty("java.home"));
        System.out.println("java.class.path: " + System.getProperty("java.class.path"));
        System.out.println("jpackage.app-path: " + System.getProperty("jpackage.app-path"));
        System.out.println("user.dir: " + System.getProperty("user.dir"));
        System.out.println("Packaged exe path: " + getPackagedExePath());
        System.out.println("Startup command: " + getStartupCommand());
        System.out.println("Currently enabled: " + isEnabled());
        System.out.println("===================================");
    }
}