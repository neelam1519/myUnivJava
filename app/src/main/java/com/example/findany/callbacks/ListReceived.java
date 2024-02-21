package com.example.findany.callbacks;

import java.util.List;

public interface ListReceived {
    void onListSuccess(List<String> documentIds);
    void onListFailure(Exception e);
}

