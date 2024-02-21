package com.example.findany;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.FullscreenListener;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.YouTubePlayerListener;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.options.IFramePlayerOptions;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kotlin.Unit;
import kotlin.jvm.functions.Function0;

public class youtubevideos extends AppCompatActivity {
    private static final String TAG = "Youtube_Videos";
    private YouTubePlayer youTubePlayer;
    private FrameLayout fullscreenViewContainer;
    private Boolean isFullScreen = false;
    private IFramePlayerOptions iFramePlayerOptions;
    private List<String> subdocumentIdsList = new ArrayList<>();
    private LinearLayout youtubePlayerContainer;
    private YouTubePlayerListener youTubePlayerListener;
    private Map<String, String> videoslist = new HashMap<>();
    Map<String, String> filteredVideos = new HashMap<>();
    SearchView searchView;
    private static final String API_KEY = "AIzaSyBvc4sC6cqelhdcxe6vvZuwk8bu8cvCMvg"; // Replace with your API key
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_youtubevideos);
        Log.d(TAG, "Oncreate started");

        videoslist.clear();
        filteredVideos.clear();

        Intent intent = getIntent();
        String branch = intent.getStringExtra("branch");
        String year = intent.getStringExtra("year");
        String subject = intent.getStringExtra("subject");
        String unit = intent.getStringExtra("unit");

        new GetVideosListTask().execute(branch, year, subject, unit);


        //getVideosList("ACADEMICDETAILS", "LectureVideos", branch, year, subject, unit);
        initializeViews();
        searchviewlistner();

    }
    private class GetVideosListTask extends AsyncTask<String, Void, Map<String, Object>> {

        @Override
        protected Map<String, Object> doInBackground(String... params) {
            String branch = params[0];
            String year = params[1];
            String subject = params[2];
            String unit = params[3];

            FirebaseFirestore db = FirebaseFirestore.getInstance();

            // Reference to the specified document in the sub-collection
            DocumentReference subdocumentRef = db.collection("ACADEMICDETAILS")
                    .document("LectureVideos")
                    .collection(branch)
                    .document(year)
                    .collection(subject)
                    .document(unit);

            try {
                Task<DocumentSnapshot> task = subdocumentRef.get();
                DocumentSnapshot document = Tasks.await(task);

                if (document.exists()) {
                    // DocumentSnapshot exists, get data
                    return document.getData();
                }
            } catch (Exception e) {
                Log.e(TAG, "Error fetching document: " + e.getMessage());
            }

            return null;
        }

        @Override
        protected void onPostExecute(Map<String, Object> data) {
            if (data != null) {
                handleVideosList(data);
            } else {
                // Handle the case when the document does not exist or an error occurred
                showToast("No Videos Found");
            }
        }
        private void showToast(String message) {
            runOnUiThread(() -> Toast.makeText(youtubevideos.this, message, Toast.LENGTH_SHORT).show());
        }
    }

    private void handleVideosList(Map<String, Object> data) {
        if (!data.isEmpty()) {
            for (Map.Entry<String, Object> entry : data.entrySet()) {
                String field = entry.getKey();
                Object value = entry.getValue();

                videoslist.put(field, value.toString());
                showVideo(value.toString(), field);

                Log.d(TAG, "VideosMapList: " + videoslist);
            }
        } else {
            // Handle the case when the document exists but has no videos
            showToast("No Videos Found");
        }
    }
    private void showToast(String message) {
        runOnUiThread(() -> Toast.makeText(youtubevideos.this, message, Toast.LENGTH_SHORT).show());
    }
    private void searchviewlistner() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterVideos(newText);
                return true;
            }
        });
    }

    private void filterVideos(String input) {
        filteredVideos.clear();

        for (Map.Entry<String, String> entry : videoslist.entrySet()) {
            String videoTitle = entry.getKey().toLowerCase();
            String videoId = entry.getValue();

            if (videoTitle.contains(input.toLowerCase())) {
                filteredVideos.put(videoTitle, videoId);
            }
        }

        updateUIWithFilteredVideos();
    }

    private void updateUIWithFilteredVideos() {
        youtubePlayerContainer.removeAllViews();

        for (Map.Entry<String, String> entry : filteredVideos.entrySet()) {
            String videoTitle = entry.getKey();
            String videoId = entry.getValue();

            showVideo(videoId, videoTitle);
        }
    }

    private void initializeViews() {
        Log.d(TAG, "Initializing views started");
        youtubePlayerContainer = findViewById(R.id.youtube_player_container);
        fullscreenViewContainer = findViewById(R.id.full_screen_view_container);
        searchView = findViewById(R.id.searchView);
        searchView.clearFocus();
        youtubePlayerContainer.removeAllViews();

        iFramePlayerOptions = new IFramePlayerOptions.Builder()
                .controls(1)
                .fullscreen(1)
                .autoplay(0)
                .build();

        Log.d(TAG, String.valueOf(subdocumentIdsList));
    }

    private void showVideo(String videoId, String videoTitle) {
        Log.d(TAG, "showVideo started: " + videoId);

        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(LinearLayout.VERTICAL);

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        layoutParams.setMargins(16, 16, 16, 16);
        linearLayout.setLayoutParams(layoutParams);

        YouTubePlayerView youTubePlayerView = new YouTubePlayerView(this);
        youTubePlayerView.setEnableAutomaticInitialization(false);
        youTubePlayerView.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));

        getLifecycle().addObserver(youTubePlayerView);

        youTubePlayerListener = new AbstractYouTubePlayerListener() {
            @Override
            public void onReady(@NonNull YouTubePlayer player) {
                youtubevideos.this.youTubePlayer = player;
                youtubevideos.this.youTubePlayer.cueVideo(videoId, 0);
            }
        };

        youTubePlayerView.initialize(youTubePlayerListener, iFramePlayerOptions);

        TextView textView = new TextView(this);
        textView.setText(videoTitle);
        textView.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));

        linearLayout.addView(youTubePlayerView);
        linearLayout.addView(textView);

        youtubePlayerContainer.addView(linearLayout);

        youTubePlayer(youTubePlayerView);
    }

    private void youTubePlayer(YouTubePlayerView youtubePlayerView) {
        Log.d(TAG, "youTubePlayer started");
        if (youtubePlayerView != null) {
            youtubePlayerView.addFullscreenListener(new FullscreenListener() {
                @Override
                public void onEnterFullscreen(View fullscreenView, Function0<Unit> exitFullscreenCallback) {
                    isFullScreen = true;
                    Log.d(TAG, "youtubePlayerView: " + youtubePlayerView);
                    fullscreenViewContainer.setVisibility(View.VISIBLE);
                    fullscreenViewContainer.addView(fullscreenView);
                    searchView.setVisibility(View.GONE);
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                    Log.d(TAG, "Entered fullscreen mode.");
                }
                @Override
                public void onExitFullscreen() {
                    isFullScreen = false;

                    Log.d(TAG, "youTubePlayerView is not null");

                    youtubePlayerView.setVisibility(View.VISIBLE);
                    fullscreenViewContainer.setVisibility(View.GONE);
                    searchView.setVisibility(View.VISIBLE);
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

                    Log.d(TAG, "Exited fullscreen mode.");
                }
            });
        } else {
            Log.d(TAG, "YouTubePlayerView is null");
        }
    }
    @Override
    public void onBackPressed() {
        if (isFullScreen) {

        } else {
            super.onBackPressed();
        }
    }


}
