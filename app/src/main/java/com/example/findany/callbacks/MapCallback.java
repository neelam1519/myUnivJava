package com.example.findany.callbacks;

import java.util.Map;

public interface MapCallback {
    void onCallback(Map<String, Object> data);
    void onFailure(Exception e);
}
