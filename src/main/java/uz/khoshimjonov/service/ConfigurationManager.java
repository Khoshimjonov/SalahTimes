package uz.khoshimjonov.service;

import lombok.Getter;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public class ConfigurationManager {

    private static final String APP_NAME = "SalahTimesWidget";
    private static final String CONFIG_FILE_NAME = "config.properties";

    private final Path configFilePath;
    private static Properties properties;
    private static volatile ConfigurationManager instance;

    public boolean apiSettingsUpdated = true;

    @Getter
    private boolean configurationExists = true;

    private ConfigurationManager() {
        this.configFilePath = getConfigFilePath();
        properties = new Properties();
        loadConfig();
    }

    /**
     * Gets the appropriate config directory based on OS and packaging.
     * For Windows: %APPDATA%/SalahTimesWidget/
     * For packaged apps, this ensures config persists across updates.
     */
    private static Path getConfigFilePath() {
        Path configDir;

        // Check if running from jpackage (app image)
        String appHome = System.getProperty("app.home");

        if (appHome != null) {
            // Running as packaged app - use AppData
            configDir = getAppDataDirectory();
        } else {
            // Running in development - check for portable mode or use AppData
            Path portableConfig = Path.of(CONFIG_FILE_NAME);
            if (Files.exists(portableConfig)) {
                return portableConfig; // Use local config if exists (development)
            }
            configDir = getAppDataDirectory();
        }

        return configDir.resolve(CONFIG_FILE_NAME);
    }

    private static Path getAppDataDirectory() {
        String os = System.getProperty("os.name").toLowerCase();
        Path appDataDir;

        if (os.contains("win")) {
            // Windows: %APPDATA%/SalahTimesWidget
            String appData = System.getenv("APPDATA");
            if (appData != null) {
                appDataDir = Path.of(appData, APP_NAME);
            } else {
                appDataDir = Path.of(System.getProperty("user.home"), "AppData", "Roaming", APP_NAME);
            }
        } else if (os.contains("mac")) {
            // macOS: ~/Library/Application Support/SalahTimesWidget
            appDataDir = Path.of(System.getProperty("user.home"), "Library", "Application Support", APP_NAME);
        } else {
            // Linux: ~/.config/SalahTimesWidget
            String xdgConfig = System.getenv("XDG_CONFIG_HOME");
            if (xdgConfig != null) {
                appDataDir = Path.of(xdgConfig, APP_NAME);
            } else {
                appDataDir = Path.of(System.getProperty("user.home"), ".config", APP_NAME);
            }
        }

        // Create directory if it doesn't exist
        try {
            Files.createDirectories(appDataDir);
        } catch (IOException e) {
            e.printStackTrace();
            // Fallback to current directory
            return Path.of(".");
        }

        return appDataDir;
    }

    public static ConfigurationManager getInstance() {
        if (instance == null) {
            synchronized (ConfigurationManager.class) {
                if (instance == null) {
                    instance = new ConfigurationManager();
                }
            }
        }
        return instance;
    }

    public int getSchool() {
        return Integer.parseInt(properties.getProperty("school", "1"));
    }

    public void setSchool(int school) {
        properties.setProperty("school", String.valueOf(school));
        saveConfig();
    }

    public int getMethod() {
        return Integer.parseInt(properties.getProperty("method", "14"));
    }

    public void setMethod(int method) {
        properties.setProperty("method", String.valueOf(method));
        saveConfig();
    }

    public String getAddress() {
        return properties.getProperty("address", "");
    }

    public void setAddress(String address) {
        properties.setProperty("address", address);
        saveConfig();
    }

    public double getLatitude() {
        return Double.parseDouble(properties.getProperty("latitude", "0.0"));
    }

    public void setLatitude(double latitude) {
        properties.setProperty("latitude", String.valueOf(latitude));
        saveConfig();
    }

    public double getLongitude() {
        return Double.parseDouble(properties.getProperty("longitude", "0.0"));
    }

    public void setLongitude(double longitude) {
        properties.setProperty("longitude", String.valueOf(longitude));
        saveConfig();
    }

    public double getElevation() {
        return Double.parseDouble(properties.getProperty("elevation", "0.0"));
    }

    public void setElevation(double elevation) {
        properties.setProperty("elevation", String.valueOf(elevation));
        saveConfig();
    }

    public boolean getLookAndFeelEnabled() {
        return Boolean.parseBoolean(properties.getProperty("lookAndFeelEnabled", "true"));
    }

    public void setLookAndFeelEnabled(boolean lookAndFeelEnabled) {
        properties.setProperty("lookAndFeelEnabled", String.valueOf(lookAndFeelEnabled));
        saveConfig();
    }

    public boolean getUseApi() {
        return Boolean.parseBoolean(properties.getProperty("useApi", "false"));
    }

    public void setUseApi(boolean useApi) {
        properties.setProperty("useApi", String.valueOf(useApi));
        saveConfig();
    }

    public boolean isDraggable() {
        return Boolean.parseBoolean(properties.getProperty("draggable", "true"));
    }

    public void setDraggable(boolean draggable) {
        properties.setProperty("draggable", String.valueOf(draggable));
        saveConfig();
    }

    public boolean isAlwaysOnTop() {
        return Boolean.parseBoolean(properties.getProperty("alwaysOnTop", "true"));
    }

    public void setAlwaysOnTop(boolean alwaysOnTop) {
        properties.setProperty("alwaysOnTop", String.valueOf(alwaysOnTop));
        saveConfig();
    }

    public int getUpdateDelay() {
        return Integer.parseInt(properties.getProperty("updateDelay", "1"));
    }

    public void setUpdateDelay(int updateDelay) {
        properties.setProperty("updateDelay", String.valueOf(updateDelay));
        saveConfig();
    }

    public int getPointX() {
        return Integer.parseInt(properties.getProperty("pointX", "100"));
    }

    public void setPointX(int pointX) {
        properties.setProperty("pointX", String.valueOf(pointX));
        saveConfig();
    }

    public int getPointY() {
        return Integer.parseInt(properties.getProperty("pointY", "100"));
    }

    public void setPointY(int pointY) {
        properties.setProperty("pointY", String.valueOf(pointY));
        saveConfig();
    }

    public boolean isNotifyBefore() {
        return Boolean.parseBoolean(properties.getProperty("notifyBefore", "true"));
    }

    public void setNotifyBefore(boolean notifyBefore) {
        properties.setProperty("notifyBefore", String.valueOf(notifyBefore));
        saveConfig();
    }

    public boolean isNotifyOnTime() {
        return Boolean.parseBoolean(properties.getProperty("notifyOnTime", "true"));
    }

    public void setNotifyOnTime(boolean notifyOnTime) {
        properties.setProperty("notifyOnTime", String.valueOf(notifyOnTime));
        saveConfig();
    }

    public String getUserLanguage() {
        return properties.getProperty("language", "en");
    }

    public void setUserLanguage(String userLanguage) {
        properties.setProperty("language", userLanguage);
        saveConfig();
    }

    public int getNotificationBeforeMinutes() {
        return Integer.parseInt(properties.getProperty("notificationBeforeMinutes", "30"));
    }

    public void setNotificationBeforeMinutes(int minutes) {
        properties.setProperty("notificationBeforeMinutes", String.valueOf(minutes));
        saveConfig();
    }

    public boolean getAutoStart() {
        return Boolean.parseBoolean(properties.getProperty("autoStart", "false"));
    }

    public void setAutoStart(boolean autoStart) {
        properties.setProperty("autoStart", String.valueOf(autoStart));
        saveConfig();
    }

    private void loadConfig() {
        try {
            Path parentDir = configFilePath.getParent();
            if (parentDir != null && !Files.exists(parentDir)) {
                Files.createDirectories(parentDir);
            }

            if (!Files.exists(configFilePath)) {
                configurationExists = false;
                Files.createFile(configFilePath);
            }

            try (InputStream input = new FileInputStream(configFilePath.toFile())) {
                properties.load(input);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void saveConfig() {
        try {
            Path parentDir = configFilePath.getParent();
            if (parentDir != null && !Files.exists(parentDir)) {
                Files.createDirectories(parentDir);
            }

            try (OutputStream output = new FileOutputStream(configFilePath.toFile())) {
                properties.store(output, "SalahTimesWidget Configuration");
            }
            apiSettingsUpdated = true;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Returns the path where config is stored (useful for debugging/support).
     */
    public Path getConfigPath() {
        return configFilePath;
    }
}