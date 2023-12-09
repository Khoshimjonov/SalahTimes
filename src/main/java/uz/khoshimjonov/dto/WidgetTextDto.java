package uz.khoshimjonov.dto;

import java.awt.*;

public class WidgetTextDto {

    private String nextSalah;
    private String remainingTime;
    private Color textColor;

    public WidgetTextDto(String nextSalah, String remainingTime, Color textColor) {
        this.nextSalah = nextSalah;
        this.remainingTime = remainingTime;
        this.textColor = textColor;
    }

    public String getNextSalah() {
        return nextSalah;
    }

    public void setNextSalah(String nextSalah) {
        this.nextSalah = nextSalah;
    }

    public String getRemainingTime() {
        return remainingTime;
    }

    public void setRemainingTime(String remainingTime) {
        this.remainingTime = remainingTime;
    }

    public Color getTextColor() {
        return textColor;
    }

    public void setTextColor(Color textColor) {
        this.textColor = textColor;
    }
}
