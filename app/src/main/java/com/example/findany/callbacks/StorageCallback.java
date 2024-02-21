package com.example.findany.callbacks;

public interface StorageCallback {

    void onSuccess(String localFilePath);

    void onFailure(Exception exception);
}
