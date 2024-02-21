package com.example.findany.callbacks;

public interface StringCallback {

    void onFieldValueRetrieved(String fieldValue);

    void onFieldNotFound(String error);
}
