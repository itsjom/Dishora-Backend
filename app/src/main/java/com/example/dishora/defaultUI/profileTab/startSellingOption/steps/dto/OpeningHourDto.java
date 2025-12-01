package com.example.dishora.defaultUI.profileTab.startSellingOption.steps.dto;

public class OpeningHourDto {
    private String dayOfWeek;
    private String opensAt;
    private String closesAt;
    private boolean isClosed;

    public String getDayOfWeek() { return dayOfWeek; }
    public void setDayOfWeek(String shortCode) {
        switch (shortCode.toLowerCase()) {
            case "mon": this.dayOfWeek = "Monday"; break;
            case "tue": this.dayOfWeek = "Tuesday"; break;
            case "wed": this.dayOfWeek = "Wednesday"; break;
            case "thu": this.dayOfWeek = "Thursday"; break;
            case "fri": this.dayOfWeek = "Friday"; break;
            case "sat": this.dayOfWeek = "Saturday"; break;
            case "sun": this.dayOfWeek = "Sunday"; break;
            default: this.dayOfWeek = "Monday"; // fallback
        }
    }

    public String getOpensAt() { return opensAt; }
    public void setOpensAt(String opensAt) { this.opensAt = opensAt; }

    public String getClosesAt() { return closesAt; }
    public void setClosesAt(String closesAt) { this.closesAt = closesAt; }

    public boolean isClosed() { return isClosed; }
    public void setClosed(boolean isClosed) { this.isClosed = isClosed; }
}
