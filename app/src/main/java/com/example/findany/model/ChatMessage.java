package com.example.findany.model;

public class ChatMessage {
    private String messageId;
    private String senderId;
    private String senderName;
    private String messageText;
    private long timestamp;
    private  String documentname;
    private String messagetype;
    private String url;
    public String getMessagetype() {
        return messagetype;
    }

    public void setMessagetype(String messagetype) {
        this.messagetype = messagetype;
    }


    public String getRegno() {
        return documentname;
    }

    public void setRegno(String regno) {
        this.documentname = regno;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
    public ChatMessage() {
    }

    public ChatMessage(String messageId, String senderId, String senderName, String messageText, long timestamp, String documentname, String messagetype, String url) {
        this.messageId = messageId;
        this.senderId = senderId;
        this.senderName = senderName;
        this.messageText = messageText;
        this.timestamp = timestamp;
        this.documentname=documentname;
        this.messagetype=messagetype;
        this.url=url;

    }


    // Getters and setters

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getMessageText() {
        return messageText;
    }

    public void setMessageText(String messageText) {
        this.messageText = messageText;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }


}
