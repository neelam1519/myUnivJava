package com.example.findany;

public class SearchResult {
    private String beforeText;
    private String ongoingText;
    private String afterText;
    private String roomNo;
    private String lecturerName;

    public SearchResult(String beforeText, String ongoingText, String afterText, String roomNo, String lecturerName) {
        this.beforeText = beforeText;
        this.ongoingText = ongoingText;
        this.afterText = afterText;
        this.roomNo = roomNo;
        this.lecturerName = lecturerName;
    }
    public String getBeforeText() {
        return beforeText;
    }
    public String getOngoingText() {
        return ongoingText;
    }
    public String getAfterText() {
        return afterText;
    }
    public String getRoomNo() {
        return roomNo;
    }
    public String getLecturerName() {
        return lecturerName;
    }
    public void setBeforeText(String beforeText) {
        this.beforeText = beforeText;
    }
    public void setOngoingText(String ongoingText) {
        this.ongoingText = ongoingText;
    }
    public void setAfterText(String afterText) {
        this.afterText = afterText;
    }
    public void setRoomNo(String roomNo) {
        this.roomNo = roomNo;
    }
    public void setLecturerName(String lecturerName) {
        this.lecturerName = lecturerName;
    }
}

