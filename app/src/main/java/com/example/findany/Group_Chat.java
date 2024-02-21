package com.example.findany;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.OpenableColumns;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.findany.Firebase.Firestore;
import com.example.findany.adapter.GroupChatAdapter;
import com.example.findany.model.ChatMessage;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Group_Chat extends AppCompatActivity{
    private String TAG = "GroupChat";
    private RecyclerView chatRecyclerView;
    private Spinner chatbranchspinner, chatyearspinner;
    private DatabaseReference typingRef;
    private DatabaseReference userPresenceRef;
    private ValueEventListener onlineUsersValueEventListener;
    private Handler typingHandler = new Handler();
    private final long TYPING_DELAY = 1000;
    private DatabaseReference userStatusRef;
    private static final int PICK_FILES_REQUEST_CODE = 1;
    private ChildEventListener messagesChildEventListener;
    private Switch notificationswitch;
    private TextView onlineusers;
    private Toolbar toolbar;
    private TextView toolbarview;
    EditText inputLayout;
    Drawable leftDrawable;
    Drawable rightDrawable;
    private TextView typing;
    private GroupChatAdapter chatAdapter;
    private List<ChatMessage> messageList = new ArrayList<>();
    private DatabaseReference messagesRef;
    private DatabaseReference TokenRef;
    private FirebaseFirestore db;
    private String username, documentname, token, email;
    private List<String> fcmTokens;
    private SharedPreferences sharedPreferences;
    List<String> templist = new ArrayList<>();
    private boolean isLoading = false;
    private boolean hasMessages = false;
    private boolean allMessagesLoaded = false;
    private int totalMessagesCount = 0;
    private int messagesLoadedOnScroll = 0;
    private Boolean recyclerviewscroll = false;
    public static String selectedBranch="ALL";
    public static String selectedYear="ALL";

    private String currentmessageid;
    private Boolean lastmessageboolean = true;
    private String chatGroupName;
    private ChatMessage lastmessage;
    private ProgressBar loadingProgressBar;
    public static Boolean isBackground=true;
    SharedPreferences FirebaseData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat);

        initializeComponents();
        getfcmtoken();
        EmailChanges();
        keyboardDisplay();
        messageEditTextListner();

        chatbranchspinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                disableWhileLoading(false);
                loadingProgressBar.setVisibility(View.VISIBLE);
                Log.d(TAG, "branch spinner previous: " + selectedBranch);
                if (selectedYear != null && selectedBranch != null) {
                    setUserOnlineStatus(false, selectedBranch, selectedYear);
                }

                selectedBranch = chatbranchspinner.getSelectedItem().toString();
                int spinnerPosition = ((ArrayAdapter<String>) chatbranchspinner.getAdapter()).getPosition(selectedBranch);
                chatbranchspinner.setSelection(spinnerPosition);
                Log.d(TAG, "branch spinner: " + selectedBranch);
                loadMessages();
                updateData();
                getUsersFCMTokens(selectedBranch,selectedYear);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        chatyearspinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                disableWhileLoading(false);
                loadingProgressBar.setVisibility(View.VISIBLE);
                Log.d(TAG, "year spinner previous: " + selectedYear);
                if (selectedYear != null && selectedBranch != null) {
                    setUserOnlineStatus(false, selectedBranch, selectedYear);
                }

                selectedYear = chatyearspinner.getSelectedItem().toString();
                int spinnerPosition = ((ArrayAdapter<String>) chatyearspinner.getAdapter()).getPosition(selectedYear);
                chatyearspinner.setSelection(spinnerPosition);

                getUsersFCMTokens(selectedBranch,selectedYear);
                Log.d(TAG, "year spinner: " + selectedYear);
                updateData();
                loadMessages();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        listenForUserTypingStatus();
    }

    private void loadMessages() {

        if (messagesChildEventListener != null && messagesRef != null) {
            messagesRef.removeEventListener(messagesChildEventListener);
        }

        messagesRef = FirebaseDatabase.getInstance().getReference("/group_chat/groupchat_" + selectedBranch + "_" + selectedYear);

        // Clear the existing message list
        messageList.clear();

        // Show the progress bar
        loadingProgressBar.setVisibility(View.VISIBLE);

        // Create a query to fetch all messages
        Query query = messagesRef.orderByKey();

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<ChatMessage> newMessages = new ArrayList<>();

                for (DataSnapshot messageSnapshot : dataSnapshot.getChildren()) {
                    ChatMessage message = messageSnapshot.getValue(ChatMessage.class);
                    if (message != null) {
                        newMessages.add(message);
                        Log.e("GroupChat", "Entered message loop");
                    }
                }

                for (ChatMessage newMessage : newMessages) {
                    if (!containsMessageWithId(messageList, newMessage.getMessageId())) {
                        messageList.add(newMessage);
                    }
                }

                messagesLoadedOnScroll = messageList.size();

                // Log the size and content of the messageList
                for (ChatMessage msg : messageList) {
                    templist.add(msg.getMessageText());
                }
                Log.d("GroupChat", "messageList size: " + templist.size() + "===" + templist);
                templist.clear();

                // Scroll to the bottom or a specific position
                if (!recyclerviewscroll) {
                    scrollToBottom();
                } else {
                    chatRecyclerView.scrollToPosition(14);
                }

                // Notify the adapter of the data change
                chatAdapter.notifyDataSetChanged();

                isLoading = false;

                Log.d("GroupChat", "Total Messages Loaded: " + messagesLoadedOnScroll);
                Log.d("GroupChat", "Total Message Count: " + totalMessagesCount);

                // Add the child event listener
                addMessagesChildEventListener();

                // Hide the progress bar
                disableWhileLoading(true);
                loadingProgressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                isLoading = false;

                // Hide the progress bar in case of an error
                disableWhileLoading(true);
                loadingProgressBar.setVisibility(View.GONE);

                Log.e("GroupChat", "Failed to load messages: " + databaseError.getMessage());
            }
        });
    }

    private boolean containsMessageWithId(List<ChatMessage> list, String messageId) {
        for (ChatMessage message : list) {
            if (message.getMessageId().equals(messageId)) {
                return true;
            }
        }
        return false;
    }

    private void addMessagesChildEventListener() {
        messagesChildEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, String previousChildName) {
                if (dataSnapshot != null) {
                    String messageKey = dataSnapshot.getKey();
                    // Check if the message key already exists in the list
                    if (!messageList.stream().anyMatch(message -> message.getMessageId().equals(messageKey))) {
                        ChatMessage message = dataSnapshot.getValue(ChatMessage.class);
                        if (message != null) {
                            messageList.add(message);
                            chatAdapter.notifyDataSetChanged();
                            scrollToBottom();
                            lastmessage = message;
                        }
                    }
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                // Handle any changes to messages (if required)
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                ChatMessage message = snapshot.getValue(ChatMessage.class);
                if (message != null) {
                    messageList.remove(message);
                    chatAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                // Handle any moved messages (if required)
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle any errors
            }
        };
        messagesRef.addChildEventListener(messagesChildEventListener);
    }

    private  void opengallery(){
        if(isConnectedToInternet()){
            openGallery();
        }else {
            Toast.makeText(getApplicationContext(),"Connect to the internet", Toast.LENGTH_SHORT).show();
        }
    }

    private void notificationgroup() {

        String branch = sharedPreferences.getString("branch", "");
        String year = sharedPreferences.getString("year", "");

        Map<String,String> data=new HashMap<>();

        if(branch !=null && year!=null) {
            data.put(documentname,token);
            Firestore.storeDataInFirestore("TOKENS", "ALL_ALL", data);
            Firestore.storeDataInFirestore("TOKENS", branch + "_YEAR" + year, data);
            Firestore.storeDataInFirestore("TOKENS", "ALL_YEAR" + year, data);
        }else{
            Log.d(TAG,"NotificationGroup null values");
        }
    }

    private void updateData() {
        checkSwitch(selectedBranch,selectedYear);
        inputLayout.setText("");
        typingRef = FirebaseDatabase.getInstance().getReference("typing").child(selectedBranch + "_" + selectedYear).child("isTyping");
        if(selectedBranch!=null && selectedYear!=null) {
            setUserOnlineStatus(true, selectedBranch, selectedYear);
        }
        startTrackingOnlineUsers();
        listenForUserTypingStatus();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG,"onStart started");
        checkSwitch(selectedBranch,selectedYear);
        startTrackingOnlineUsers();
    }

    @Override
    protected void onStop() {
        super.onStop();
        setUserOnlineStatus(false,selectedBranch,selectedYear);
        stopTrackingOnlineUsers();
    }

    private void initializeComponents() {
        chatRecyclerView = findViewById(R.id.chatRecyclerView);
        inputLayout = findViewById(R.id.inputLayout);
        leftDrawable = getResources().getDrawable(R.drawable.icon_attachfile);
        rightDrawable = getResources().getDrawable(R.drawable.send);
        inputLayout.setCompoundDrawablesWithIntrinsicBounds(leftDrawable, null, rightDrawable, null);
        chatbranchspinner = findViewById(R.id.Chat_Branch);
        chatyearspinner = findViewById(R.id.Chat_Year);
        notificationswitch = findViewById(R.id.notificationSwitch);
        onlineusers = findViewById(R.id.onlineusers);
        toolbar = findViewById(R.id.toolbar);
        toolbarview = findViewById(R.id.toolbar_title);
        typing = findViewById(R.id.typing);
        loadingProgressBar = findViewById(R.id.loadingProgressBar);

        sharedPreferences = getSharedPreferences("UserDetails", MODE_PRIVATE);

        documentname = sharedPreferences.getString("RegNo", "");
        email = sharedPreferences.getString("Mail", "");
        username = sharedPreferences.getString("username", documentname);

        chatAdapter = new GroupChatAdapter(this, messageList);
        chatRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        chatRecyclerView.setAdapter(chatAdapter);

        db = FirebaseFirestore.getInstance();

        userPresenceRef = FirebaseDatabase.getInstance().getReference("OnlineUsers")
                .child(documentname);

        setViewListeners();
    }

    void disableWhileLoading(boolean state) {
        inputLayout.setEnabled(state);
        chatbranchspinner.setEnabled(state);
        chatyearspinner.setEnabled(state);
        notificationswitch.setEnabled(state);

        // Update the appearance of UI elements based on the state
        if (state) {
            // If enabled, set the alpha to 1 (full opacity)
            inputLayout.setAlpha(1f);
            chatbranchspinner.setAlpha(1f);
            chatyearspinner.setAlpha(1f);
            notificationswitch.setAlpha(1f);
        } else {
            // If disabled, set the alpha to 0.5 (half opacity)
            inputLayout.setAlpha(0.5f);
            chatbranchspinner.setAlpha(0.5f);
            chatyearspinner.setAlpha(0.5f);
            notificationswitch.setAlpha(0.5f);
        }
    }


    private void setViewListeners() {
        inputLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                final int DRAWABLE_LEFT = 0;
                final int DRAWABLE_RIGHT = 2;

                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (event.getRawX() >= (inputLayout.getRight() - inputLayout.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                        sendMessage();
                        return true;
                    } else if (event.getRawX() <= (inputLayout.getCompoundDrawables()[DRAWABLE_LEFT].getBounds().width())) {
                        // Left drawable clicked
                        // Handle attachment action
                        opengallery();
                        return true;
                    }
                }
                return false;
            }
        });

        notificationswitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            String dynamicPath = selectedBranch + "_" + selectedYear;
            if (isChecked) {
                Map<String,String> data=new HashMap<>();
                data.put(documentname,token);
                Firestore.storeDataInFirestore("TOKENS",dynamicPath,data);
            } else {
                Firestore.removeValueFromFirestore(db,"TOKENS",dynamicPath,documentname);
            }
        });
    }

    private void sendMessage() {
        String messageText = inputLayout.getText().toString().trim();

        if(isConnectedToInternet()) {

            if (!TextUtils.isEmpty(messageText)) {
                String messageId = messagesRef.push().getKey();
                String senderId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                String senderName = username;
                long timestamp = System.currentTimeMillis();

                currentmessageid=messageId;

                ChatMessage message = new ChatMessage(messageId, senderId, senderName, messageText, timestamp, email, "text", null);
                messagesRef.child(messageId).setValue(message);

                // Update the sendNotifications method call
                sendtoNotifications(fcmTokens, messageText);

                inputLayout.setText("");

                setUserTypingStatus(false);
            } else {
                Toast.makeText(this, "Please enter a message", Toast.LENGTH_SHORT).show();
            }
        }else{
            Toast.makeText(getApplicationContext(),"Connect to the internet", Toast.LENGTH_SHORT).show();
        }
    }

    private void scrollToBottom() {
        chatRecyclerView.scrollToPosition(chatAdapter.getItemCount() - 1);
    }
    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        startActivityForResult(Intent.createChooser(intent, "Select Files"), PICK_FILES_REQUEST_CODE);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_FILES_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            if (data.getData() != null) {
                Uri fileUri = data.getData();
                handleSelectedFile(fileUri);
            } else if (data.getClipData() != null) {
                int count = data.getClipData().getItemCount();
                for (int i = 0; i < count; i++) {
                    Uri fileUri = data.getClipData().getItemAt(i).getUri();
                    handleSelectedFile(fileUri);
                }
            }
        }
    }
    private void handleSelectedFile(Uri fileUri) {

        Toast.makeText(Group_Chat.this, "Loading file...", Toast.LENGTH_LONG).show();
        String messageId = messagesRef.push().getKey();
        String senderId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String senderName = username;
        long timestamp = System.currentTimeMillis();

        String filename = getFileNameFromUri(fileUri);
        String fileType = getFileType(filename);  // Added this line to determine file type

        StorageReference storageRef = FirebaseStorage.getInstance().getReference();
        StorageReference fileRef = storageRef.child("group_chat/" + messageId);

        UploadTask uploadTask = fileRef.putFile(fileUri);
        uploadTask.addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                fileRef.getDownloadUrl().addOnCompleteListener(downloadTask -> {
                    if (downloadTask.isSuccessful()) {
                        Uri downloadUri = downloadTask.getResult();
                        if (downloadUri != null) {
                            String downloadUrl = downloadUri.toString();

                            currentmessageid=messageId;

                            sendtoNotifications(fcmTokens,filename+"."+fileType);
                            ChatMessage fileMessage = new ChatMessage(messageId, senderId, senderName, filename, timestamp, email, fileType, downloadUrl);
                            messagesRef.child(messageId).setValue(fileMessage);

                        } else {
                            Toast.makeText(Group_Chat.this, "Failed to get download URL", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(Group_Chat.this, "Failed to get download URL", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                Toast.makeText(Group_Chat.this, "Failed to upload file", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String getFileNameFromUri(Uri uri) {
        String fileName = null;
        String scheme = uri.getScheme();
        if (scheme != null && scheme.equals("content")) {
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                if (nameIndex != -1) {
                    fileName = cursor.getString(nameIndex);
                }
                cursor.close();
            }
        } else if (scheme != null && scheme.equals("file")) {
            fileName = uri.getLastPathSegment();
        }
        return fileName;
    }
    private void getUsersFCMTokens(String selectedBranch,String selectedYear) {
        String path = selectedBranch + "_" + selectedYear;

        Firestore.getAllFcmTokens(db, "TOKENS", path, new OnCompleteListener<List<String>>() {
            @Override
            public void onComplete(@NonNull Task<List<String>> task) {
                if (task.isSuccessful()) {
                    fcmTokens = task.getResult();

                    // Remove the present user's token from the list
                    if (fcmTokens != null && !fcmTokens.isEmpty() && token != null) {
                        fcmTokens.remove(token);
                    }

                    // Do something with the remaining FCM tokens
                    Log.d(TAG, "FCM Tokens (excluding present user): " + fcmTokens);
                } else {
                    // Handle the case where there was an error retrieving FCM tokens
                    Exception exception = task.getException();
                    Log.e(TAG, "Error getting FCM Tokens", exception);
                }
            }
        });
    }
    private void sendtoNotifications(List<String> fcmTokens, String message) {
        if (fcmTokens == null) {
            Log.e("Group_Chat", "FCM Tokens are null");
            return;
        }
        Log.d("groupchat", "entered sendNotifications");

        SharedPreferences sharedPreferences = getSharedPreferences("APIKEYS", Context.MODE_PRIVATE);
        String fcmServerKey = sharedPreferences.getString("FCMKEY", "");
        Log.d(TAG,"FCM SERVER KEY: "+fcmServerKey);

        for (String token : fcmTokens) {
            FcmNotificationsSender notificationsSender = new FcmNotificationsSender(token, "BRANCH="+selectedBranch+", YEAR="+selectedYear, message, selectedBranch, selectedYear, getApplicationContext(),fcmServerKey);
            notificationsSender.sendNotification();
        }
    }
    private void checkSwitch(String selectedBranch,String selectedYear) {

        if(selectedBranch!=null && selectedYear!=null) {
            String path = selectedBranch + "_" + selectedYear;
            Log.d(TAG, "checkswitch: " + path);
            DocumentReference documentRef = db.collection("TOKENS")
                    .document(path);

            documentRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document != null && document.exists() && document.contains(documentname)) {
                            notificationswitch.setChecked(true);
                        } else {
                            notificationswitch.setChecked(false);
                        }
                    } else {
                        Exception exception = task.getException();
                        Log.e(TAG, "Error checking switch state", exception);
                    }
                }
            });
        }
    }



    private void setUserOnlineStatus(boolean isOnline, String branch, String year) {
        Log.d(TAG,"setUserOnlineStatus: "+branch+"_"+year+" is online: "+isOnline);
        DatabaseReference branchYearRef = FirebaseDatabase.getInstance().getReference("OnlineUsers")
                .child(branch + "_" + year);

        userStatusRef = branchYearRef.child(documentname);

        if (isOnline) {
            // Use onDisconnect() to automatically update user presence when the app is closed or disconnected
            userStatusRef.onDisconnect().setValue(false);
            // Set the current user's online status
            userStatusRef.setValue(true);
        } else {
            // Set the current user's online status to false immediately when going offline
            userStatusRef.removeValue(); // Use removeValue() to delete the specific key-value
        }
    }


    private void startTrackingOnlineUsers() {
        DatabaseReference onlineUsersRef = FirebaseDatabase.getInstance().getReference("OnlineUsers")
                .child(selectedBranch + "_" + selectedYear);

        onlineUsersValueEventListener = onlineUsersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                long childCount = 0;
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    boolean isOnline = snapshot.getValue(Boolean.class);
                    if (isOnline) {
                        childCount++;
                    }
                }
                onlineusers.setText(String.valueOf(childCount));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle the error event here
            }
        });
    }

    private void stopTrackingOnlineUsers() {
        if (onlineUsersValueEventListener != null) {
            userStatusRef.getParent().removeEventListener(onlineUsersValueEventListener);
        }
    }
    private void setUserTypingStatus(boolean isTyping) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null && selectedBranch != null && selectedYear != null) {
            DatabaseReference userTypingRef = FirebaseDatabase.getInstance().getReference("typing")
                    .child(selectedBranch + "_" + selectedYear)
                    .child(documentname); // Store the user typing status under their unique user ID
            userTypingRef.setValue(isTyping);
        }
    }

    private void listenForUserTypingStatus() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null && selectedBranch != null && selectedYear != null) {
            DatabaseReference typingRef = FirebaseDatabase.getInstance().getReference("typing");
            typingRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.hasChild(selectedBranch + "_" + selectedYear)) {
                        long typingcount = 0;
                        for (DataSnapshot snapshot : dataSnapshot.child(selectedBranch + "_" + selectedYear).getChildren()) {
                            // Skip the child with the name "document name"
                            if (!snapshot.getKey().equals(documentname)) {
                                boolean istyping = snapshot.getValue(Boolean.class);
                                if (istyping) {
                                    typingcount++;
                                }
                            }
                        }
                        if (typingcount == 0) {
                            typing.setVisibility(View.GONE);
                        } else {
                            typing.setVisibility(View.VISIBLE);
                            typing.setText(String.valueOf(typingcount) + " typing");
                        }
                    } else {
                        typing.setVisibility(View.GONE);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    // Handle database read error if necessary
                }
            });
        }
    }
    private String getFileType(String filename) {
        String extension = filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
        String mimeType;

        switch (extension) {
            case "jpg":
            case "jpeg":
            case "png":
            case "gif":
            case "bmp":
            case "webp":
                mimeType = "image/*";
                break;
            case "mp4":
            case "avi":
            case "mkv":
            case "flv":
                mimeType = "video/*";
                break;
            case "mp3":
            case "wav":
            case "flac":
            case "m4a":
                mimeType = "audio/*";
                break;
            case "pdf":
                mimeType = "application/pdf";
                break;
            case "doc":
            case "docx":
                mimeType = "application/msword";
                break;
            case "ppt":
            case "pptx":
                mimeType = "application/vnd.ms-powerpoint";
                break;
            case "xls":
            case "xlsx":
                mimeType = "application/vnd.ms-excel";
                break;
            case "zip":
                mimeType = "application/zip";
                break;
            case "rar":
                mimeType = "application/x-rar-compressed";
                break;
            default:
                mimeType = "application/octet-stream";
                break;
        }
        return mimeType;
    }

    private Runnable typingTimeoutRunnable = new Runnable() {
        @Override
        public void run() {
            setUserTypingStatus(false);
        }
    };

    private void getfcmtoken() {
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        token = task.getResult();
                        Log.d(TAG, "FCM Token: " + token);
                        notificationgroup();
                    } else {
                        Log.e(TAG, "Failed to get FCM token");
                    }
                });
    }

    void keyboardDisplay(){

        View rootView = findViewById(android.R.id.content);
        rootView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                int rootViewHeight = rootView.getHeight();
                int visibleHeight = rootView.getRootView().getHeight() - rootView.getHeight();

                if (visibleHeight > rootViewHeight / 3) {
                    // Keyboard is visible, scroll the RecyclerView to the last item
                    int lastItemIndex = chatRecyclerView.getAdapter().getItemCount() - 1;
                    if (lastItemIndex >= 0) {
                        chatRecyclerView.smoothScrollToPosition(lastItemIndex);
                    }
                } else {
                    // Keyboard is hidden
                }
            }
        });
    }

    void messageEditTextListner(){
        inputLayout.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.length() > 0) {
                    // User has started typing
                    setUserTypingStatus(true);
                    // Remove any existing callbacks (reset the delay) and start a new one
                    typingHandler.removeCallbacks(typingTimeoutRunnable);
                    typingHandler.postDelayed(typingTimeoutRunnable, TYPING_DELAY);
                } else {
                    // User has stopped typing
                    typingHandler.removeCallbacks(typingTimeoutRunnable);
                    typingHandler.postDelayed(typingTimeoutRunnable, TYPING_DELAY);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
    }

    void EmailChanges(){
        if (email != null && email.endsWith("@klu.ac.in")) {
            toolbarview.setVisibility(View.GONE);
            Log.d(TAG,"Email ends with klu");
        } else {
            Log.d(TAG,"Email not ends with klu");
            notificationswitch.setVisibility(View.GONE);
            chatyearspinner.setVisibility(View.GONE);
            chatbranchspinner.setVisibility(View.GONE);
            TextView textbranch = findViewById(R.id.textBranch);
            textbranch.setVisibility(View.GONE);
            TextView textyear = findViewById(R.id.textYear);
            textyear.setVisibility(View.GONE);
            TextView textnotification = findViewById(R.id.textNotification);
            textnotification.setVisibility(View.GONE);
            setSupportActionBar(toolbar);
            getSupportActionBar().setTitle("Your Toolbar Text");
            toolbarview.setText("Group Chat");
        }
    }
    private boolean isConnectedToInternet() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
            return activeNetwork != null && activeNetwork.isConnected();
        }
        return false;
    }

}