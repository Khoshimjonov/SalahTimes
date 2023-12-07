package uz.khoshimjonov.widget;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class FrameDragListener extends MouseAdapter {

    private final JDialog frame;
    private Point mouseDownCompCoords = null;
    private Point savedPosition = null;

    public FrameDragListener(JDialog frame, Point currentPosition) {
        this.frame = frame;
        this.savedPosition = currentPosition;
    }

    public void mouseReleased(MouseEvent e) {
        Point currCoords = e.getLocationOnScreen();
        savedPosition = new Point(currCoords.x - mouseDownCompCoords.x, currCoords.y - mouseDownCompCoords.y);
        mouseDownCompCoords = null;
    }

    public void mousePressed(MouseEvent e) {
        mouseDownCompCoords = e.getPoint();
        savedPosition = null;
    }

    public void mouseDragged(MouseEvent e) {
        Point currCoords = e.getLocationOnScreen();
        savedPosition = null;
        frame.setLocation(currCoords.x - mouseDownCompCoords.x, currCoords.y - mouseDownCompCoords.y);
    }

    public Point getSavedPosition() {
        return savedPosition;
    }
}
