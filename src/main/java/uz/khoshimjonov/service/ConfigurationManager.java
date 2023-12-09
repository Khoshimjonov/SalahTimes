package uz.khoshimjonov.service;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public class ConfigurationManager {

    private static final String CONFIG_FILE = "config.properties";

    private static Properties properties;

    private static volatile ConfigurationManager instance;

    public boolean apiSettingsUpdated = true;

    private ConfigurationManager() {
        properties = new Properties();
        loadConfig();
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

    public boolean getLookAndFeelEnabled() {
        return Boolean.parseBoolean(properties.getProperty("lookAndFeelEnabled", "true"));
    }

    public void setLookAndFeelEnabled(boolean lookAndFeelEnabled) {
        properties.setProperty("lookAndFeelEnabled", String.valueOf(lookAndFeelEnabled));
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

    public boolean isNotification() {
        return Boolean.parseBoolean(properties.getProperty("notification", "true"));
    }

    public void setNotification(boolean notification) {
        properties.setProperty("notification", String.valueOf(notification));
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

    private void loadConfig() {
        try {
            Path path = Path.of(CONFIG_FILE);
            if (!Files.exists(path)) {
                Files.createFile(path);
            }
            InputStream input = new FileInputStream(CONFIG_FILE);
            properties.load(input);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void saveConfig() {
        try (OutputStream output = new FileOutputStream(CONFIG_FILE)) {
            properties.store(output, "Configuration File");
            loadConfig();
            apiSettingsUpdated = true;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
