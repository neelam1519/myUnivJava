package com.example.findany;

import static android.content.Context.MODE_PRIVATE;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class NotificationReceiver extends BroadcastReceiver {
    private SqliteHelper dbHelper;
    String TAG = "NotificationReceiver";
    String value;
    String dayOfWeek;
    int hour;
    int minute;
    String subject;
    Calendar calendar = Calendar.getInstance();
    int currentSubjectIndex;
    @Override
    public void onReceive(final Context context, Intent intent) {

        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            Intent intents = new Intent("com.neelam.findany.NOTIFY");
            intents.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.sendBroadcast(intents);
        }

        dbHelper = new SqliteHelper(context);

        LocalTime currentTime = LocalTime.now();
        hour = currentTime.getHour();
        minute = currentTime.getMinute();

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        Date currentDate = new Date();
        String Date = dateFormat.format(currentDate);
        SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE");
        dayOfWeek = dayFormat.format(currentDate).toUpperCase();

        Log.e("Time:  ",hour+":"+minute);

        if (dayOfWeek.equals("SUNDAY")) {
            sendNotification(context,"FindAny","TODAY IS SUNDAY");
            setCalendar(context,intent,1,8,45);
            return;
        } else if (dayOfWeek.equals("SATURDAY")) {
            value = getValueFromSharedPreferences(context, "HOLIDAYS", Date);
            if (value != null) {
                if (value.equals("HOLIDAY")) {
                    sendNotification(context,"FindAny","TODAY IS HOLIDAY");
                    setCalendar(context,intent,1,8,45);
                    return;
                } else {
                    dayOfWeek = value;
                    Log.e(TAG, "Today is SATURDAY and the timetable is " + dayOfWeek);
                }
            } else {
                Log.e(TAG, "Value is null");
            }
        }

        SharedPreferences ACEDEMICDETAILS = context.getSharedPreferences("UserDetails", MODE_PRIVATE);

        String year = ACEDEMICDETAILS.getString("year", "");
        String branch = ACEDEMICDETAILS.getString("branch", "");
        String slot = ACEDEMICDETAILS.getString("slot", "");

        if (year == null || branch == null || slot == null) {
            Log.d(TAG, "The Required Details are null");
            setCalendar(context,intent,0,40,0);
            return;
        }

        String Timetablename = "YEAR" + year + "_" + branch + "_SLOT" + slot;
        String Lecturestable = "YEAR" + year + "_" + branch + "_SLOT" + slot + "_LECTURERS";
        List<String> Subjects = dbHelper.getValuesFromSpecificColumn(Timetablename, dayOfWeek);

        if (Subjects.isEmpty()) {
            setCalendar(context,intent,0,40,0);
            Log.e(TAG, "Subjects list is empty");
            return;
        }

        if (hour == 8 && minute >= 45) {
            currentSubjectIndex = 0;
        } else if (hour > 8) {
            if (minute < 45) {
                currentSubjectIndex = hour - 9;
            } else {
                currentSubjectIndex = hour - 8;
            }
        }

        Log.d("currentSubjectIndex:  ", String.valueOf(+currentSubjectIndex));
        if(currentSubjectIndex < Subjects.size()) {
            subject = Subjects.get(currentSubjectIndex);

            if(subject==null){
                Log.e(TAG,"currentSubjectIndex not found");
                setCalendar(context,intent,0,40,0);
                return;
            }

            String displaysubject = subject;

            Log.d(TAG, "Current subject: " + subject);

            if (subject.contains("LAB")) {
                subject = subject.replace("LAB", "").trim();
            }

            String lecturerName = dbHelper.getLecturerName(context, subject);

            if (lecturerName == null || lecturerName.isEmpty()) {
                Log.d(TAG, "Lecturer doesn't present");
                sendNotification(context,displaysubject," ");
                setCalendar(context,intent,0,40,0);
                return;
            }

            String roomno = dbHelper.getRoomNoForLecturerAndSubject(Lecturestable, lecturerName, subject);

            if (roomno == null || roomno.isEmpty()) {
                Log.d(TAG, "roomNo doesn't present");
                sendNotification(context,displaysubject," ");
                setCalendar(context,intent,0,40,0);
                return;
            }

            Log.d(TAG, "roomNo: " + roomno);

            sendNotification(context, displaysubject, roomno);
        }else{
            Log.d(TAG,"currentsubjectIndex is out of index");
            setCalendar(context,intent,0,40,0);
            return;
        }

        alaramtimings(context, intent);
    }

    private void alaramtimings(Context context, Intent intent) {

        if (hour <= 8 && minute < 45) {
            setCalendar(context,intent,0,8,45);
            Log.d(TAG, "1");
        } else if (hour >= 16 && minute > 46) {
            setCalendar(context,intent,1,8,45);
            Log.d(TAG, "2");
        } else {
            if (minute < 45) {
                setCalendar(context,intent,0,0,45);
                Log.d(TAG, "3");
            } else {
                setCalendar(context,intent,0,1,45);
                Log.d(TAG, "4");
            }
        }

    }

    private void setCalendar(Context context,Intent intent,int day,int hour,int minute) {
        calendar.add(Calendar.DAY_OF_MONTH, day);
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);

        convertTimeToMillis(calendar,context,intent);
    }


    private void convertTimeToMillis(Calendar calendar,Context context,Intent intent){
        long triggerTimeInMillis = calendar.getTimeInMillis();
        setAlarm(context, intent, triggerTimeInMillis);
        Log.d(TAG, "Adjusted Time: " + calendar.getTime());
        Log.d(TAG, String.valueOf(triggerTimeInMillis));
    }

    public void sendNotification(Context context,String title, String content) {
        // Log for debugging
        Log.d(TAG, "Sending Notification - Title: " + title + ", Content: " + content);

        // Create a NotificationChannel for Android 8.0 and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("TimeTableNotification", "NotificationReceiver", NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(channel);
        }
        // Create an explicit intent for launching the app when the notification is clicked
        Intent resultIntent = new Intent(context, MainActivity.class);
        resultIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        // Create a PendingIntent for the notification
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                0,
                resultIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Create a NotificationCompat.Builder
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "TimeTableNotification")
                .setSmallIcon(R.drawable.defaultprofile)
                .setContentTitle(title)
                .setContentText(content)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);  // Removes the notification when the user taps it

        // Build the notification
        Notification notification = builder.build();

        // Get the NotificationManager service
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // Log for debugging
        Log.d(TAG, "NotificationManager obtained");

        // Send the notification
        if (notificationManager != null) {
            int notificationId = 1; // You can use a unique ID for each notification
            notificationManager.notify(notificationId, notification);

            // Log for debugging
            Log.d(TAG, "Notification sent with ID: " + notificationId);
        } else {
            // Log for debugging
            Log.e(TAG, "NotificationManager is null. Notification not sent.");
        }
    }

    public void setAlarm(Context context,Intent intent, long triggerTimeInMillis) {
        // Get the AlarmManager service
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        // Create a PendingIntent for the alarm
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 143, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        // Set the alarm
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) { // Android 12 or higher
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTimeInMillis, pendingIntent);
            } else {
                // You can notify the user or take other actions if you cannot schedule exact alarms.
                Log.e(TAG, "Cannot schedule exact alarms.");
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) { // Android 11
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTimeInMillis, pendingIntent);
        } else { // Android 6.0 Marshmallow or higher
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTimeInMillis, pendingIntent);
        }
    }
    public String getValueFromSharedPreferences(Context context, String sharedPreferencesName, String key) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(sharedPreferencesName, MODE_PRIVATE);

        if (sharedPreferences.contains(key)) {
            return sharedPreferences.getString(key, null);
        } else {
            return null;
        }
    }
}
