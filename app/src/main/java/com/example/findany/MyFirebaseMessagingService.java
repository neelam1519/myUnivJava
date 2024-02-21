package com.example.findany;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioAttributes;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.example.findany.Firebase.Firestore;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    String TAG="MyFirebaseMessagingService";
    private static final String CHANNEL_ID = "NeelamChat";
    private static final String CHANNEL_NAME = "GroupChat";
    private static final String CHANNEL_DESC = "Chatting";
    private HashMap<String, List<String>> notificationMessages = new HashMap<>();
    SharedPreferences sharedPreferences;
    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);

        sharedPreferences=getSharedPreferences("UserDetails",MODE_PRIVATE);
        String documentname=sharedPreferences.getString("RegNo","");

        Log.d(TAG,"Document Name: "+documentname+"  New Token: "+token);

        Firestore.updateFieldInAllDocuments("TOKENS",documentname,token);
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        Log.d("onMessageReceived", "From: " + remoteMessage.getFrom());
        Log.d("onMessageReceived", "Data Payload: " + remoteMessage.getData());

        if (remoteMessage.getData() != null && remoteMessage.getData().size() > 0) {
            String title = remoteMessage.getData().get("title");
            String body = remoteMessage.getData().get("body");
            String branch = remoteMessage.getData().get("branch");
            String year = remoteMessage.getData().get("year");
            String tag = remoteMessage.getData().get("tag");

            if(Group_Chat.selectedYear != null && Group_Chat.selectedBranch != null){
                if (branch.equals(Group_Chat.selectedBranch) && year.equals(Group_Chat.selectedYear)) {
                    Log.d(TAG, "User is in the same chat");
                    return;
                }
            }
            showNotification(title, body, branch, year, tag);

            Log.d(TAG,"Messaging: "+body);
        }
    }

    private void showNotification(String title, String body, String branch, String year, String tag) {
        String keyForNotification = branch + "_" + year;
        int notificationId = keyForNotification.hashCode(); // Using hashcode as an identifier for the notification ID

        // Manage list of messages for this branch-year
        if (!notificationMessages.containsKey(keyForNotification)) {
            notificationMessages.put(keyForNotification, new ArrayList<>());
        }

        List<String> messages = notificationMessages.get(keyForNotification);
        if (messages != null) {
            messages.add(0, body);  // Add new message at the beginning
            // If there are more than 5 messages, remove the oldest one
            while (messages.size() > 5) {
                messages.remove(messages.size() - 1);
            }
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.defaultprofile)
                .setContentTitle(title)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setGroup(keyForNotification);  // Group notifications by the unique key

        NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
        // Set the summary text and add all the messages
        inboxStyle.setBigContentTitle(title);
        for (String msg : messages) {
            inboxStyle.addLine(msg);
        }
        builder.setStyle(inboxStyle);

        Intent resultIntent = new Intent(this, Group_Chat.class);
        resultIntent.putExtra("branch", branch);
        resultIntent.putExtra("year", year);
        resultIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        int requestCode = (int) System.currentTimeMillis();

        int pendingIntentFlags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            pendingIntentFlags |= PendingIntent.FLAG_IMMUTABLE;
        }
        PendingIntent pendingIntent = PendingIntent.getActivity(this, requestCode, resultIntent, pendingIntentFlags);
        builder.setContentIntent(pendingIntent);
        builder.setAutoCancel(true);

        Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        builder.setSound(soundUri);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel();
        }

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.notify(notificationId, builder.build());
        }
    }
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
            );

            channel.setDescription(CHANNEL_DESC);

            AudioAttributes audioAttributes = new AudioAttributes.Builder()

                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .build();
            channel.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION), audioAttributes);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }
}