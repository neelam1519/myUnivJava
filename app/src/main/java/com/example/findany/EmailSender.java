package com.example.findany;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Base64;
import android.util.Log;
import android.webkit.MimeTypeMap;

import com.example.findany.callbacks.EmailSendingCallback;
import com.example.findany.model.EmailData;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okio.BufferedSource;
import okio.Okio;

public class EmailSender extends AsyncTask<EmailData, Void, Boolean> {

    private static final String TAG = "EmailSender";
    private static final String SENDER_EMAIL = "99210041602@klu.ac.in";
    private Context context;
    private EmailSendingCallback callback;
    private static OkHttpClient client = new OkHttpClient();
    private String apiKey;

    public EmailSender(Context context, EmailSendingCallback callback) {
        this.context = context;
        this.callback = callback;
    }

    @Override
    protected Boolean doInBackground(EmailData... emailData) {
        apiKey = getAPIKeyFirestore();

        if (apiKey == null || apiKey.isEmpty()) {
            Log.e(TAG, "API key is not available from Firestore.");
            return false;
        }

        try {
            JSONObject requestBodyJson = createRequestBody(emailData[0]);

            RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"), requestBodyJson.toString());
            Request request = createRequest(apiKey, requestBody);

            try (Response response = client.newCall(request).execute()) {
                if (response.isSuccessful()) {
                    Log.d(TAG, "Email sent successfully");
                    return true;
                } else {
                    // Log the details of the failed response
                    Log.e(TAG, "Email sending failed: " + response.code() + " - " + response.message() + " - " + response.body().string());
                    return false;
                }
            }
        } catch (IOException | JSONException e) {
            Log.e(TAG, "Exception: " + e.getMessage());
            return false;
        }
    }

    @Override
    protected void onPostExecute(Boolean isSuccessful) {
        super.onPostExecute(isSuccessful);
        if (callback != null) {
            callback.onEmailSendingComplete(isSuccessful);
        }
    }

    private JSONObject createRequestBody(EmailData data) throws JSONException {
        File[] files = data.getFiles();
        JSONArray attachmentsArray = createAttachmentsArray(files);

        JSONObject requestBodyJson = new JSONObject();
        requestBodyJson.put("personalizations", createPersonalizationsArray(data.getRecipientEmail()));
        requestBodyJson.put("from", new JSONObject().put("email", SENDER_EMAIL));
        requestBodyJson.put("subject", data.getSubject());

        // Always include the "content" field with an array containing the message
        requestBodyJson.put("content", new JSONArray().put(new JSONObject().put("type", "text/plain").put("value", data.getMessage())));

        // Always include the "attachments" field, even if it's an empty array
        requestBodyJson.put("attachments", new JSONArray());

        if (files != null && files.length > 0) {
            requestBodyJson.put("attachments", attachmentsArray);
        }

        return requestBodyJson;
    }


    private JSONArray createAttachmentsArray(File[] files) throws JSONException {
        JSONArray attachmentsArray = new JSONArray();

        if (files != null && files.length > 0) {
            for (File attachmentFile : files) {
                JSONObject attachmentObject = new JSONObject();
                attachmentObject.put("content", encodeFileToBase64(attachmentFile));
                attachmentObject.put("type", getMimeType(attachmentFile));
                attachmentObject.put("filename", attachmentFile.getName());

                attachmentsArray.put(attachmentObject);
            }
        }

        return attachmentsArray;
    }


    private JSONArray createPersonalizationsArray(String recipientEmail) throws JSONException {
        JSONArray personalizationsArray = new JSONArray();
        JSONObject personalizationObject = new JSONObject();
        JSONArray toEmailsArray = new JSONArray();

        JSONObject toEmailObject = new JSONObject();
        toEmailObject.put("email", recipientEmail);
        toEmailsArray.put(toEmailObject);
        personalizationObject.put("to", toEmailsArray);

        personalizationsArray.put(personalizationObject);
        return personalizationsArray;
    }

    private Request createRequest(String apiKey, RequestBody requestBody) {
        return new Request.Builder()
                .url("https://api.sendgrid.com/v3/mail/send")
                .addHeader("Authorization", "Bearer " + apiKey)
                .post(requestBody)
                .build();
    }

    private String encodeFileToBase64(File file) {
        try (BufferedSource source = Okio.buffer(Okio.source(file))) {
            byte[] fileBytes = source.readByteArray();
            return Base64.encodeToString(fileBytes, Base64.NO_WRAP);
        } catch (IOException e) {
            Log.e(TAG, "Exception: " + e.getMessage());
            return null;
        }
    }

    private String getMimeType(File file) {
        String fileExtension = getFileExtension(file.getName());
        return getMimeTypeFromExtension(fileExtension);
    }

    private String getFileExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf(".");
        return (dotIndex != -1 && dotIndex < fileName.length() - 1) ? fileName.substring(dotIndex + 1) : "";
    }

    private String getMimeTypeFromExtension(String extension) {
        String type = null;
        if (extension != null) {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension.toLowerCase());
        }
        return type != null ? type : "application/octet-stream";
    }

    private String getAPIKeyFirestore() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference docRef = db.collection("APIKEYS").document("APIKEYS");

        Task<DocumentSnapshot> task = docRef.get();
        try {
            Tasks.await(task); // Blocking call to wait for the result
        } catch (Exception e) {
            Log.e(TAG, "Exception while waiting for Firestore task: " + e.getMessage());
        }

        if (task.isSuccessful()) {
            DocumentSnapshot document = task.getResult();
            if (document.exists()) {
                return document.getString("EMAIL_APIKEY");
            }
        }

        Log.e(TAG, "Failed to get API key from Firestore.");
        return null;
    }
}