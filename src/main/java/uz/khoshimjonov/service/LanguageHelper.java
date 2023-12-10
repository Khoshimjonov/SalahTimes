package uz.khoshimjonov.service;

import java.io.InputStream;
import java.util.HashSet;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Set;

public class LanguageHelper {
    private static final String BASE_NAME = "messages";
    private static ResourceBundle resourceBundle;

    private static final ConfigurationManager configurationManager = ConfigurationManager.getInstance();


    public static void setLocale(String languageCode) {
        Locale locale = Locale.of(languageCode);
        resourceBundle = ResourceBundle.getBundle(BASE_NAME, locale);
    }

    public static String getText(String key) {
        return internalGetText(key);
    }

    public static String[] getAvailableLocales() {
        Locale[] availableLocales = Locale.getAvailableLocales();
        Set<String> locales = new HashSet<>();
        for (Locale availableLocale : availableLocales) {
            String language = availableLocale.getLanguage();
            if (!language.trim().isEmpty() && propertiesFileExists(language)) {
                locales.add(language);
            }
        }
        return locales.toArray(new String[0]);
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
