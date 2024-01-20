package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import MoviesTab.MoviesActivity;
import SettingsTab.SettingsActivity;
import SocialTab.SocialActivity;
import TheatersTab.TheatersActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

/**
 * HomeActivity is an activity class that extends AppCompatActivity.
 * It serves as the main screen of the application and handles user interactions
 * for navigating to different tabs such as Theaters, Movies, Social, and Settings.
 * It also includes gesture detection for swiping to navigate to the Movies tab.
 */
public class HomeActivity extends AppCompatActivity {

    // GestureDetector to detect swipe gestures.
    private GestureDetector gestureDetector;

    /**
     * Called when the activity is starting. This is where most initialization should go:
     * calling setContentView(int) to inflate the activity's UI, using findViewById(int)
     * to programmatically interact with widgets in the UI, setting up listeners, and so on.
     *
     * @param savedInstanceState If the activity is being re-initialized after previously
     *                           being shut down then this Bundle contains the data it most
     *                           recently supplied in onSaveInstanceState(Bundle).
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Gesture detection setup.
        gestureDetector = new GestureDetector(this, new SwipeGestureDetector());

        // Button initialization and click listeners setup.
        Button theatersBtn = findViewById(R.id.button8);
        Button moviesBtn = findViewById(R.id.button9);
        Button socialBtn = findViewById(R.id.button4);

        // Listener for theater button to start TheatersActivity.
        theatersBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeActivity.this, TheatersActivity.class);
                startActivity(intent);
            }
        });

        // Listener for movies button to start MoviesActivity.
        moviesBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeActivity.this, MoviesActivity.class);
                startActivity(intent);
            }
        });

        // Listener for social button to start SocialActivity.
        socialBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeActivity.this, SocialActivity.class);
                startActivity(intent);
            }
        });

        // ImageView for settings and its click listener.
        ImageView settingsButton = findViewById(R.id.settingsButton);
        settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeActivity.this, SettingsActivity.class);
                startActivity(intent);
            }
        });
    }

    // store x and y coordinates
    public void saveCoordinatesToSharePreferences(Float finalX, Float finalY) {
        SharedPreferences preferences = getSharedPreferences("UserPreferences", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putFloat("finalX", finalX);
        editor.putFloat("finalY", finalY);
        editor.apply();
    }



    /**
     * This method dispatches touch event to the GestureDetector and
     * is called when a touch screen event was not handled by any of
     * the views under it. This method is used to detect swipe gestures.
     *
     * @param event The MotionEvent object containing full information about the event.
     * @return boolean Return true if this event was consumed or super.onTouchEvent(event) otherwise.
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return gestureDetector.onTouchEvent(event) || super.onTouchEvent(event);
    }

    @Override
    public String toString() {
        return "HomeActivity{" +
                "gestureDetector=" + gestureDetector +
                '}';
    }

    /**
     * SwipeGestureDetector is an inner class extending GestureDetector.SimpleOnGestureListener
     * to handle swipe gestures detected by the GestureDetector.
     */
    private class SwipeGestureDetector extends GestureDetector.SimpleOnGestureListener {
        // Constants to determine the parameters of the swipe.
        private static final int SWIPE_MIN_DISTANCE = 120;
        private static final int SWIPE_MAX_OFF_PATH = 250;
        private static final int SWIPE_THRESHOLD_VELOCITY = 200;

        /**
         * Called when a fling event is detected.
         *
         * @param e1        The first down motion event that started the fling.
         * @param e2        The move motion event that triggered the current onFling.
         * @param velocityX The velocity of this fling measured in pixels per second
         *                  along the x axis.
         * @param velocityY The velocity of this fling measured in pixels per second
         *                  along the y axis.
         * @return boolean Return true if the event is consumed, else false.
         */
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            try {
                // Check for horizontal swipe and that it meets the minimum distance and velocity.
                if (Math.abs(e1.getX() - e2.getX()) > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                    // Detect left swipe and start MoviesActivity.
                    if (e1.getX() > e2.getX()) {
                        Intent intent = new Intent(HomeActivity.this, MoviesActivity.class);
                        startActivity(intent);
                    }
                    // Right swipe would be detected here.
                    return true;
                }
            } catch (Exception e) {
                // Exception handling
            }
            return false;
        }
    }
}

