package com.example.findany;

import android.content.Context;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class FcmNotificationsSender {

    private String userFcmToken;
    private String title;
    private String body;
    private String selectedBranch;
    private String selectedYear;
    private Context mContext;
    private String fcmServerKey;

    private static final String FCM_URL = "https://fcm.googleapis.com/fcm/send";
    private static final String TAG = "FcmNotificationSender";

    public FcmNotificationsSender(String userFcmToken, String title, String body, String selectedBranch,
                                  String selectedYear, Context mContext,String fcmServerKey) {
        this.userFcmToken = userFcmToken;
        this.title = title;
        this.body = body;
        this.selectedBranch = selectedBranch;
        this.selectedYear = selectedYear;
        this.mContext = mContext;
        this.fcmServerKey=fcmServerKey;
    }

    public void sendNotification() {
        // Logging to determine when and where sendNotification is being called
        Log.d(TAG, "sendNotification: Sending notification with Title: " + title + " and Body: " + body);

        RequestQueue requestQueue = Volley.newRequestQueue(mContext);
        JSONObject mainObj = new JSONObject();
        try {
            mainObj.put("to", userFcmToken);

            Log.d(TAG,"Tokens: "+userFcmToken);

            JSONObject dataObject = new JSONObject();
            dataObject.put("title", title);
            dataObject.put("body", body);
            dataObject.put("branch", selectedBranch);
            dataObject.put("year", selectedYear);
            mainObj.put("data", dataObject);

            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, FCM_URL, mainObj,
                    response -> {
                        // Logging the successful sending of the notification
                        Log.d(TAG, "onResponse: Notification sent successfully with Title: " + title + " and Body: " + body);
                    },
                    error -> {
                        // Logging any error that occurred while sending the notification
                        Log.e(TAG, "sendNotification: Failed to send notification with Title: " + title + " and Body: " + body, error);
                    }) {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> headers = new HashMap<>();
                    headers.put("content-type", "application/json");
                    headers.put("authorization", "key=" + fcmServerKey);
                    return headers;
                }
            };

            requestQueue.add(request);

        } catch (JSONException e) {
            // Logging any JSONException
            Log.e(TAG, "sendNotification: JSON Exception while trying to send notification with Title: " + title + " and Body: " + body, e);
        }
    }

}
