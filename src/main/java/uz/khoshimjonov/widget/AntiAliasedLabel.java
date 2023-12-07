package uz.khoshimjonov.widget;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class AntiAliasedLabel extends JLabel {

    public AntiAliasedLabel(String text) {
        super(text);
    }

    public AntiAliasedLabel() {
        super();
        Font font = new Font("Roboto", Font.BOLD, 14);
        setFont(font);
        setForeground(Color.WHITE);
        setHorizontalAlignment(SwingConstants.LEFT);
        setVerticalAlignment(SwingConstants.CENTER);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        Map<RenderingHints.Key, Object> renderingHints = new HashMap<>();
        renderingHints.put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        renderingHints.put(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2d.setRenderingHints(renderingHints);

        super.paintComponent(g2d);
    }
}
