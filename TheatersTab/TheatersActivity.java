package TheatersTab;

import android.content.Intent;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.myapplication.HomeActivity;
import com.example.myapplication.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import android.os.Bundle;

import MoviesTab.MoviesActivity;
import SettingsTab.SettingsActivity;
import SocialTab.SocialActivity;

public class TheatersActivity extends AppCompatActivity implements TheaterAdapter.OnClickListener {

    // variables for ui components
    private RecyclerView theatersRV;
    private FloatingActionButton searchButton;
    private EditText searchText;
    private Button homeBtn, socialBtn, moviesBtn;
    private ImageView settingsBtn;

    // variable for adapter -- class and array list
    private TheaterAdapter adapter;
    private ArrayList<TheatersModal> theatersModalArrayList;
    private String theaterNameObj;

    // server url
    String url = "http://coms-309-040.class.las.iastate.edu:8080/theaters";

    // GestureDetector
    private GestureDetector gestureDetector;

    /**
     * Sets up page for user viewing. This includes button clicking instructions and retrieving saved
     * data (email).
     *
     * The user interacts with the toolbar on top to visit whatever tab they like, search bar
     * to look up a theater and find out what is showing there, swipe either left or right
     * to go to a neighboring tab, and move and click a shortcut button to the social tab.
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_theaters);

        // initialize variables
        theatersRV = findViewById(R.id.RVTheaters);
        searchButton = findViewById(R.id.searchBtn);
        searchText = findViewById(R.id.searchBar);
        homeBtn = findViewById(R.id.homeBtn);
        moviesBtn = findViewById(R.id.movieBtn);
        socialBtn = findViewById(R.id.socialBtn);
        settingsBtn = findViewById(R.id.settingsButton);


        gestureDetector = new GestureDetector(this, new TheatersActivity.SwipeGestureDetector());


        // searchBtn click
        searchButton.setOnClickListener(new View.OnClickListener() {
            /***
             * Gets the user input text, makes a request for their desired theater, and displays it on the screen.
             * @param view This current View
             */
            @Override
            public void onClick(View view) {
                theaterNameObj = searchText.getText().toString();
                // creating a new array list
                theatersModalArrayList = new ArrayList<>();

                // showcases all the theaters
                if (theaterNameObj.isEmpty() || theaterNameObj == null) {
                    getTheaters();
                } else { // looks for a specific theater
                    getTheatersSearch(theaterNameObj);
                }
                //calling method to build recycler view
                buildRecyclerView();
            }
        });

        homeBtn.setOnClickListener(new View.OnClickListener()
        {
            /**
             * Switches the user to the HomeActivity class.
             * @param v The current View
             */
            @Override
            public void onClick(View v)
            {
                Intent intent = new Intent(TheatersActivity.this, HomeActivity.class);
                startActivity(intent);
            }
        });

        moviesBtn.setOnClickListener(new View.OnClickListener()
        {
            /**
             * Switches the user to the HomeActivity class.
             * @param v The current View
             */
            @Override
            public void onClick(View v)
            {
                Intent intent = new Intent(TheatersActivity.this, MoviesActivity.class);
                startActivity(intent);
            }
        });

        socialBtn.setOnClickListener(new View.OnClickListener()
        {
            /**
             * Switches the user to the HomeActivity class.
             * @param v The current View
             */
            @Override
            public void onClick(View v)
            {
                Intent intent = new Intent(TheatersActivity.this, SocialActivity.class);
                startActivity(intent);
            }
        });

        // Set click listener for the settings button
        settingsBtn.setOnClickListener(new View.OnClickListener() {
            /**
             * Switches the user to the SettingsActivity class.
             * @param v This current View
             */
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TheatersActivity.this, SettingsActivity.class);
                startActivity(intent);
            }
        });

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

    /**
     * SwipeGestureDetector is an inner class extending GestureDetector.SimpleOnGestureListener
     * to handle swipe gestures detected by the GestureDetector.
     */
    private class SwipeGestureDetector extends GestureDetector.SimpleOnGestureListener {
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
                float diffX = e2.getX() - e1.getX();
                if (Math.abs(diffX) > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                    if (diffX > 0) {
                        // Swipe right - go to HomeActivity
                        Intent intent = new Intent(TheatersActivity.this, MoviesActivity.class);
                        startActivity(intent);
                    } else {
                        // Swipe left - go to TheatersActivity
                        Intent intent = new Intent(TheatersActivity.this, SocialActivity.class);
                        startActivity(intent);
                    }
                    return true;
                }
            } catch (Exception e) {
                // Handle exception
            }
            return false;
        }
    }


    /**
     * Makes a request to the server to retrieve a JSONArray of all theaters in
     * the area.
     */
    private void getTheaters() {
        // create a new variable for request queue
        RequestQueue queue = Volley.newRequestQueue(TheatersActivity.this);

        // make request for JSON
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, url, null, new Response.Listener<JSONArray>() {
            /***
             * Takes the response by the request and turns it into a card to be
             * displayed on the RecyclerView.
             * @param response A JSONArray returned by the JsonArrayRequest
             */
            @Override
            public void onResponse(JSONArray response) {
                theatersRV.setVisibility(View.VISIBLE);
                for (int i = 0; i < response.length(); i++) {
                    // create a new json object + get each object from the json array
                    try {
                        // get each json object
                        JSONObject responseObj = response.getJSONObject(i);

                        // get the key from the json object
                        String theaterName = responseObj.getString("name");
                        String theaterZip = responseObj.getString("zip");
                        String theaterCity = responseObj.getString("city");
                        String theaterAddress = responseObj.getString("address") + " " + theaterCity + ", " + theaterZip;
                        String theaterPhone = responseObj.getString("phoneNumber");
                        String theaterLink = responseObj.getString("url");
                        theatersModalArrayList.add(new TheatersModal(theaterName, theaterAddress, theaterPhone, theaterLink));
                        buildRecyclerView();

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }, new Response.ErrorListener() {
            /***
             * Shows an error if the JsonArrayRequest was unsuccessful.
             * @param error The error that occurred
             */
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(TheatersActivity.this, "We could not load theaters.", Toast.LENGTH_SHORT).show();
            }
        });

        queue.add(jsonArrayRequest);
    }

    /***
     * Makes a request to the server to retrieve a JSONObject of the searched
     * theater.
     */
    private void getTheatersSearch(String theaterName) {
        // updated url
        String searchUrl = url + "/search/" + theaterName;
        Log.d("searchUrl = ", searchUrl);

        // create a new variable for request queue
        RequestQueue queue = Volley.newRequestQueue(TheatersActivity.this);
        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.GET, searchUrl, null, new Response.Listener<JSONObject>() {
            /***
             * Takes the response by the request and turns it into a theater card to be
             * displayed on the RecyclerView.
             * @param response A JSONObject returned by the JsonObjectRequest
             */
            @Override
            public void onResponse(JSONObject response) {
                theatersRV.setVisibility(View.VISIBLE);
                    try {
                        // get the key from the json object
                        String theaterName = response.getString("name");
                        String theaterZip = response.getString("zip");
                        String theaterCity = response.getString("city");
                        String theaterAddress = response.getString("address") + " " + theaterCity + ", " + theaterZip;
                        String theaterPhone = response.getString("phoneNumber");
                        String theaterLink = response.getString("url");
                        theatersModalArrayList.add(new TheatersModal(theaterName, theaterAddress, theaterPhone, theaterLink));
                        buildRecyclerView();

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
        }, new Response.ErrorListener() {
            /***
             * Shows an error if the JsonObjectRequest was unsuccessful.
             * @param error The error that occurred
             */
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(TheatersActivity.this, "We could not find the theater you were looking for.", Toast.LENGTH_SHORT).show();
            }
        });

        queue.add(jsonObjReq);

    }

    /***
     * Populates the Recycler View to show the theater(s) that are available in the
     * area.
     */
    private void buildRecyclerView() {
        // initialize adapter class
        adapter = new TheaterAdapter(theatersModalArrayList, TheatersActivity.this, this);

        // add layout manager to recycler view
        LinearLayoutManager manager = new LinearLayoutManager(this);
        theatersRV.setHasFixedSize(true);

        // set layout manager to the recycler view
        theatersRV.setLayoutManager(manager);

        // set adapter to the recycler view
        theatersRV.setAdapter(adapter);
    }

    /**
     * Takes the user to the next screen, TheatersAvailable, passing a string of all
     * the theaters the movie  is showing at with it.
     * @param modal This theater card that is being clicked on
     */
    @Override
    public void onClick(TheatersModal modal) {
        Intent intent = new Intent(TheatersActivity.this, TheaterSearch.class);

        // put into next screen
        intent.putExtra("theaterName", theaterNameObj);
        startActivity(intent);
    }
}
