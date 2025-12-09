package uz.khoshimjonov.widget;

import javax.swing.*;
import java.awt.*;

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
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        super.paintComponent(g2d);
    }
}
