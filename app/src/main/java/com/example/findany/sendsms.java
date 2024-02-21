package com.example.findany;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class sendsms extends AsyncTask<Void, Void, Void> {

    private String apiKey = "Kkt6bmG7ejlnpJAfC6Gut6fxJR3WU2uUneDZjKZSXi7FUAP1VQDdVPZbS230";
    private String senderId = "Neelam";
    private String mobileNumber;
    private String message;

    public sendsms(String mobileNumber, String message) {
        this.mobileNumber = mobileNumber;
        this.message = String.valueOf(message);
    }

    @Override
    protected Void doInBackground(Void... voids) {
        try {
            // Create the URL for the Fast2SMS API
            URL url = new URL("https://www.fast2sms.com/dev/bulkV2?authorization=" + apiKey + "&sender_id=" + senderId + "&message=" + message + "&language=english&route=p&numbers=" + mobileNumber);

            // Open the connection to the URL as HttpsURLConnection
            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();

            // Read the response from the API
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String line;
            StringBuilder response = new StringBuilder();

            while ((line = reader.readLine()) != null) {
                response.append(line);
            }

            reader.close();

            // Check the HTTP response code
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpsURLConnection.HTTP_OK) {
                // Success: Log the response
                Log.d("SendSMSAsyncTask", "SMS sent successfully. Response: " + response.toString());
            } else {
                // Error: Log the error response and code
                Log.e("SendSMSAsyncTask", "Error sending SMS. Response Code: " + responseCode + ", Response: " + response.toString());
            }
        } catch (Exception e) {
            // Exception: Log the exception
            Log.e("SendSMSAsyncTask", "Exception while sending SMS:", e);
        }

        return null;
    }
}

