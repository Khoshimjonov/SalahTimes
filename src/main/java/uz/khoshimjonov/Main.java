package uz.khoshimjonov;

import uz.khoshimjonov.service.AutoStartManager;
import uz.khoshimjonov.service.ConfigurationManager;
import uz.khoshimjonov.widget.SalahWidget;

public class Main {
    private static final SalahWidget widget = new SalahWidget();
    public static void main(String[] args) {
        // Apply autostart preference on startup
        try {
            if (ConfigurationManager.getInstance().getAutoStart()) {
                AutoStartManager.enable();
            }
        } catch (Exception ignored) {}
        widget.displayWidget();
    }
}