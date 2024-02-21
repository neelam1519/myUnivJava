package com.example.findany;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class URLShortener extends AsyncTask<String, Void, String> {
    private static final String API_KEY = "a63cda50-23b6-11ee-953c-2f4651768d23";
    private static final String API_URL = "https://api.shorten.rest/aliases";
    private static final MediaType JSON_MEDIA_TYPE = MediaType.parse("application/json");
    private static final String TAG = "URLShortener";

    private ShortenUrlListener listener;

    public URLShortener(ShortenUrlListener listener) {
        this.listener = listener;
    }

    @Override
    protected String doInBackground(String... params) {
        String longUrl = params[0];
        String shortenedUrl = null;

        OkHttpClient client = new OkHttpClient();
        JSONObject json = new JSONObject();
        try {
            json.put("destinations", new JSONObject().put("url", longUrl));
        } catch (JSONException e) {
            Log.e(TAG, "Error constructing JSON request", e);
        }
        RequestBody body = RequestBody.create(JSON_MEDIA_TYPE, json.toString());
        Request request = new Request.Builder()
                .url(API_URL)
                .post(body)
                .addHeader("x-api-key", API_KEY)
                .addHeader("Content-Type", "application/json")
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful()) {
                String responseBody = response.body().string();
                shortenedUrl = extractShortenedUrl(responseBody);
            } else {
                Log.e(TAG, "Error occurred while shortening URL. Response code: " + response.code());
            }
        } catch (IOException e) {
            Log.e(TAG, "Error occurred while sending HTTP request", e);
        }

        return shortenedUrl;
    }

    private String extractShortenedUrl(String responseBody) {
        try {
            JSONObject responseJson = new JSONObject(responseBody);
            return responseJson.optString("shortUrl");
        } catch (JSONException e) {
            Log.e(TAG, "Error parsing response JSON", e);
            return null;
        }
    }

    @Override
    protected void onPostExecute(String result) {
        if (listener != null) {
            listener.onShortenUrlResult(result);
        }
    }

    public interface ShortenUrlListener {
        void onShortenUrlResult(String shortenedUrl);
    }
}
