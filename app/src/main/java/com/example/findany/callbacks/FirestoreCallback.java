package com.example.findany.callbacks;

public interface FirestoreCallback {
    void onSuccess();
    void onFailure(Exception e);
}

