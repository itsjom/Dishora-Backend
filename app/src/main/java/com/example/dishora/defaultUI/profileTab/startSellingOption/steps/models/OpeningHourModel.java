package com.example.dishora.defaultUI.profileTab.startSellingOption.steps.models;

import java.util.Locale;

public class OpeningHourModel {
    private String dayOfWeek;
    private String opensAt; // Should be 24-hr format "HH:mm"
    private String closesAt; // Should be 24-hr format "HH:mm"
    private boolean isClosed;

    public OpeningHourModel(String dayOfWeek, String opensAt, String closesAt, boolean isClosed) {
        this.dayOfWeek = dayOfWeek;
        this.opensAt = opensAt;
        this.closesAt = closesAt;
        this.isClosed = isClosed;
    }

    // --- Standard Getters ---
    public String getDayOfWeek() { return dayOfWeek; }
    public String getOpensAt() { return opensAt; }
    public String getClosesAt() { return closesAt; }
    public boolean isClosed() { return isClosed; }

    // --- Standard Setters (Needed by the Fragment) ---
    public void setOpensAt(String opensAt) { this.opensAt = opensAt; }
    public void setClosesAt(String closesAt) { this.closesAt = closesAt; }
    public void setClosed(boolean closed) { isClosed = closed; }


    // --- Helper Methods (Needed by the Fragment) ---

    /**
     * Helper to get 12-hour format for display (e.g., "9:00 AM - 5:00 PM")
     */
    public String getDisplayTime() {
        try {
            int startHour = getOpensAtHour();
            int startMinute = getOpensAtMinute();
            int endHour = getClosesAtHour();
            int endMinute = getClosesAtMinute();

            String startAmPm = (startHour >= 12) ? "PM" : "AM";
            String endAmPm = (endHour >= 12) ? "PM" : "AM";

            int displayStartHour = (startHour == 0 || startHour == 12) ? 12 : startHour % 12;
            int displayEndHour = (endHour == 0 || endHour == 12) ? 12 : endHour % 12;

            return String.format(Locale.US, "%d:%02d %s - %d:%02d %s",
                    displayStartHour, startMinute, startAmPm,
                    displayEndHour, endMinute, endAmPm);
        } catch (Exception e) {
            return "Set Time"; // Fallback text
        }
    }

    /**
     * Helper to get hour as an int for the time picker
     */
    public int getOpensAtHour() {
        try {
            return Integer.parseInt(opensAt.split(":")[0]);
        } catch (Exception e) {
            return 9; // Default 9 AM
        }
    }

    /**
     * Helper to get minute as an int for the time picker
     */
    public int getOpensAtMinute() {
        try {
            return Integer.parseInt(opensAt.split(":")[1]);
        } catch (Exception e) {
            return 0; // Default :00
        }
    }

    /**
     * Helper to get hour as an int for the time picker
     */
    public int getClosesAtHour() {
        try {
            return Integer.parseInt(closesAt.split(":")[0]);
        } catch (Exception e) {
            return 17; // Default 5 PM
        }
    }

    /**
     * Helper to get minute as an int for the time picker
     */
    public int getClosesAtMinute() {
        try {
            return Integer.parseInt(closesAt.split(":")[1]);
        } catch (Exception e) {
            return 0; // Default :00
        }
    }
}