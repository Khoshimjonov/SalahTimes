package uz.khoshimjonov.service;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

public class LanguageHelper {
    private static final String BASE_NAME = "messages";
    private static ResourceBundle resourceBundle;

    private static final String[] SUPPORTED_LANGUAGES = {"uz", "en", "ru"};

    private static final ConfigurationManager configurationManager = ConfigurationManager.getInstance();


    public static void setLocale(String languageCode) {
        Locale locale = Locale.of(languageCode);
        resourceBundle = ResourceBundle.getBundle(BASE_NAME, locale);
    }

    public static String getText(String key) {
        return internalGetText(key);
    }

    public static String[] getAvailableLocales() {
        List<String> available = new ArrayList<>();

        for (String lang : SUPPORTED_LANGUAGES) {
            if (propertiesFileExists(lang)) {
                available.add(lang);
            }
        }

        if (available.isEmpty()) {
            available.add("en");
        }

        return available.toArray(new String[0]);
    }

    private static boolean propertiesFileExists(String languageCode) {
        String fileName = "/messages_" + languageCode + ".properties";
        try (InputStream in = LanguageHelper.class.getResourceAsStream(fileName)) {
            return in != null;
        } catch (Exception e) {
            return false;
        }
    }


    private static String internalGetText(String key) {
        if (resourceBundle == null) {
            setLocale(configurationManager.getUserLanguage());
        }

        try {
            return resourceBundle.getString(key);
        } catch (Exception e) {
            return "";
        }
    }
}
