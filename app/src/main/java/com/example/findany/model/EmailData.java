package com.example.findany.model;

import java.io.File;

public class EmailData {
    private File[] files;
    private String recipientEmail;
    private String subject;
    private String message;

    public EmailData(){

    }
    public EmailData(File[] files, String recipientEmail, String subject, String message) {
        this.files = files;
        this.recipientEmail = recipientEmail;
        this.subject = subject;
        this.message = message;
    }

    public void setFiles(File[] files) {
        this.files = files;
    }

    public void setRecipientEmail(String recipientEmail) {
        this.recipientEmail = recipientEmail;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public File[] getFiles() {
        return files;
    }

    public String getRecipientEmail() {
        return recipientEmail;
    }

    public String getSubject() {
        return subject;
    }

    public String getMessage() {
        return message;
    }
}

