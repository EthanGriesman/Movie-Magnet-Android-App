package MoviesTab;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.myapplication.HomeActivity;
import com.example.myapplication.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import SettingsTab.SettingsActivity;
import SocialTab.SocialActivity;
import TheatersTab.TheatersActivity;

/**
 * A tab the user can use to search for a movie and find which theaters in the Ames
 * area show that movie.
 */
public class MoviesActivity extends AppCompatActivity implements MoviesAdapter.OnClickListener {

    // variables for ui components
    private FloatingActionButton searchBtn;
    private EditText searchText;
    private RecyclerView moviesRV;

    // variable for adapter -- class and array list
    private MoviesAdapter adapter;
    private ArrayList<MoviesModel> moviesModelArrayList;
    private String movieTitleObj;
    private String email;

    // for next screen AvailableTheaters
    private String availableTheaters;

    // server url
    private String url;

    // variables for ui components
    private Button socialBtn, theatersBtn, homeBtn, reviewBtn;
    private ImageView settingsBtn;

    // GestureDetector
    private GestureDetector gestureDetector;

    /**
     * Sets up page for user viewing. This includes button clicking instructions, retrieving saved
     * data (email).
     *
     * The user interacts with the toolbar on top to visit whatever tab they like, search bar
     * to look up a movie and where they are showing in theaters, swipe either left or right
     * to go to a neighboring tab, and move and click a shortcut button to the social tab.
     *
     * @param savedInstanceState If the activity is being re-initialized after previously
     *                           being shut down then this Bundle contains the data it most
     *                           recently supplied in onSaveInstanceState(Bundle)
     */
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movies);

        SharedPreferences sh = getSharedPreferences("UserPreferences", MODE_PRIVATE);
        email = sh.getString("email", "");
        url = "http://coms-309-040.class.las.iastate.edu:8080/movies/";

        // initialize variables
        moviesRV = findViewById(R.id.RVMovies);
        searchBtn= findViewById(R.id.searchBtn);
        searchText = findViewById(R.id.searchBar);

        // searchBtn click
        searchBtn.setOnClickListener(new View.OnClickListener() {
            /**
             * Gets the user input text, makes a request for their desired movie, and displays it on the screen.
             * @param view This current View
             */
            @Override
            public void onClick(View view) {
                movieTitleObj = searchText.getText().toString();
                // creating a new array list
                moviesModelArrayList = new ArrayList<>();

                // request the theater's movie show times
                try {
                    // create json body object
                    JSONObject requestBody = new JSONObject();
                    requestBody.put("email", email);
                    Log.d("requestBody = ", requestBody.toString());
                    // showcases all the theaters
                    if (movieTitleObj.isEmpty() || movieTitleObj == null) {
                        getMovies(requestBody);
                    } else { // looks for a specific theater
                        getMovieSearch(requestBody);
                    }
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }

                //calling method to build recycler view
                buildRecyclerView();
            }

    });

        // Initialize buttons and image view
        settingsBtn = findViewById(R.id.settingsBtn);
        socialBtn = findViewById(R.id.socialBtn);
        theatersBtn = findViewById(R.id.theatersBtn);
        reviewBtn = findViewById(R.id.reviewBtn);
        homeBtn = findViewById(R.id.homeBtn);

        // Initialize GestureDetector
        gestureDetector = new GestureDetector(this, new SwipeGestureDetector());

        // Set click listener for the settings button
        settingsBtn.setOnClickListener(new View.OnClickListener() {
            /**
             * Switches the user to the SettingsActivity class.
             * @param v This current View
             */
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MoviesActivity.this, SettingsActivity.class));
            }
        });

        // Set click listeners for the other buttons
        socialBtn.setOnClickListener(new View.OnClickListener() {
            /**
             * Switches the user to the SocialActivity class.
             * @param v This current view
             */
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MoviesActivity.this, SocialActivity.class);
                startActivity(intent);
            }
        });

        // Set click listeners for the other buttons
        reviewBtn.setOnClickListener(new View.OnClickListener() {
            /**
             * Switches the user to the ReviewActivity class.
             * @param v This current view
             */
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MoviesActivity.this, ReviewActivity.class);
                startActivity(intent);
            }
        });

        theatersBtn.setOnClickListener(new View.OnClickListener() {
            /**
             * Switches the user to the TheatersActivity class.
             * @param v This current view
             */
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MoviesActivity.this, TheatersActivity.class);
                startActivity(intent);
            }
        });

        homeBtn.setOnClickListener(new View.OnClickListener() {
            /**
             * Switches the user to the HomeActivity class.
             * @param v This current view
             */
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MoviesActivity.this, HomeActivity.class);
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
                        Intent intent = new Intent(MoviesActivity.this, HomeActivity.class);
                        startActivity(intent);
                    } else {
                        // Swipe left - go to TheatersActivity
                        Intent intent = new Intent(MoviesActivity.this, TheatersActivity.class);
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
     * Makes a request to the server to retrieve a string of all the movies currently
     * showing in theaters.
     * @param requestBody a JSONObject that holds the user's email.
     */
    private void getMovies(JSONObject requestBody) {
        String searchUrl = url + "all";

        // create a new variable for request queue
        RequestQueue queue = Volley.newRequestQueue(MoviesActivity.this);

        StringRequest stringRequest = new StringRequest(Request.Method.POST, searchUrl,
                new Response.Listener<String>() {
                    /**
                     * Takes the response by the request and turns it into a card to be
                     * displayed on the RecyclerView.
                     * @param response A String returned by the StringRequest
                     */
                    @Override
                    public void onResponse(String response) {

                        // recycler view is now visible
                        moviesRV.setVisibility(View.VISIBLE);
                        try {
                            // turn response into a JSONArray
                            //Log.d("theaterName:", theaterName);
                            Log.d("Response:", response);
                            JSONArray jsonArrayResponse = new JSONArray(response);
                            for (int i = 0; i < jsonArrayResponse.length(); i++) {
                                // create a new json object + get each object from the json array
                                // get each json object
                                JSONObject responseObj = jsonArrayResponse.getJSONObject(i);

                                // get the key from the json object
                                String movieTitle = responseObj.getString("title");
                                String movieRating = responseObj.getString("rating");
                                String movieGenre = responseObj.getString("genre");
                                String movieRuntime = responseObj.getString("runtime");
                                String movieTimes = "Theater times not available";

                                // put the JSONArray into the next screen
                                availableTheaters = responseObj.getString("theaterList");
                                moviesModelArrayList.add(new MoviesModel(movieTitle, movieRating, movieRuntime, movieGenre, movieTimes));
                                buildRecyclerView();
                            }

                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }
                    }
                },
                new Response.ErrorListener() {
                    /**
                     * Shows an error if the StringRequest was unsuccessful.
                     * @param error The error that occurred
                     */
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // Log the error for debugging
                        Log.e("Volley", "Error response", error);

                        // Extract more detailed information if possible from the error object
                        NetworkResponse networkResponse = error.networkResponse;
                        if (networkResponse != null && networkResponse.data != null) {
                            String jsonError = new String(networkResponse.data);
                            Log.e("Volley", "Server error: " + jsonError);
                        }

                        // Inform the user with a more detailed message
                        String errorMessage = "The server is currently experiencing issues. Please try again later.";
                        if (networkResponse != null && networkResponse.statusCode == 500) {
                            errorMessage = "Internal Server Error. Please try again later.";
                        }
                        Toast.makeText(MoviesActivity.this, errorMessage, Toast.LENGTH_LONG).show();

                        // Here you could also add a button or a mechanism to retry the request
                    }
                }) {

            /**
             * Gets the body from the JSONObject
             * @return The String holding the user's email if successful
             */
            @Override
            public byte[] getBody() {
                try {
                    return requestBody.getString("email").getBytes("utf-8");
                } catch (UnsupportedEncodingException | JSONException e) {
                    VolleyLog.wtf("Unsupported Encoding while trying to get the bytes of %s using %s", requestBody, "utf-8");
                    return null;
                }
            }
            @Override
            public String getBodyContentType() {
                return "application/json";
            }
        };

        queue.add(stringRequest);
    }

    /**
     * Makes a String Request to the server to retrieve a string of the searched movie object.
     * @param requestBody holds the user's email.
     */
    private void getMovieSearch(JSONObject requestBody) {
        // updated url
        String searchUrl = url + "/search/" + movieTitleObj;
        Log.d("searchUrl = ", searchUrl);

        RequestQueue queue = Volley.newRequestQueue(MoviesActivity.this);

        StringRequest stringRequest = new StringRequest(Request.Method.POST, searchUrl,
                new Response.Listener<String>() {
                    /**
                     * Takes the response by the request and turns it into a movie card to be
                     * displayed on the RecyclerView.
                     * @param response A String returned by the stringRequest
                     */
                    @Override
                    public void onResponse(String response) {

                        // recycler view is now visible
                        moviesRV.setVisibility(View.VISIBLE);
                        try {
                            // turn response into a JSONArray
                            //Log.d("theaterName:", theaterName);
                            Log.d("Response:", response);
                            JSONObject jsonObjectResponse = new JSONObject(response);

                                // get the key from the json object
                                String movieTitle = jsonObjectResponse.getString("title");
                                String movieRating = jsonObjectResponse.getString("rating");
                                String movieGenre = jsonObjectResponse.getString("genre");
                                String movieRuntime = jsonObjectResponse.getString("runtime");
                                String movieTimes = "Theater times not available";
                                // put the JSONArray into the next screen
                                availableTheaters = jsonObjectResponse.getString("theaterList");
                                moviesModelArrayList.add(new MoviesModel(movieTitle, movieRating, movieRuntime, movieGenre, movieTimes));
                            buildRecyclerView();
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }
                    }
                },
                new Response.ErrorListener() {
                    /**
                     * Shows an error if the stringRequest was unsuccessful.
                     * @param error The error that occurred
                     */
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // Log the error for debugging
                        Log.e("Volley", "Error response", error);

                        // Extract more detailed information if possible from the error object
                        NetworkResponse networkResponse = error.networkResponse;
                        if (networkResponse != null && networkResponse.data != null) {
                            String jsonError = new String(networkResponse.data);
                            Log.e("Volley", "Server error: " + jsonError);
                        }

                        // Inform the user with a more detailed message
                        String errorMessage = "The server is currently experiencing issues. Please try again later.";
                        if (networkResponse != null && networkResponse.statusCode == 500) {
                            errorMessage = "Internal Server Error. Please try again later.";
                        }
                        Toast.makeText(MoviesActivity.this, errorMessage, Toast.LENGTH_LONG).show();

                        // Here you could also add a button or a mechanism to retry the request
                    }
                }) {
            /**
             * Get's the body from the JSONObject
             * @return The String holding the user's email if successful
             */
            @Override
            public byte[] getBody() {
                try {
                    return requestBody.getString("email").getBytes("utf-8");
                } catch (UnsupportedEncodingException | JSONException e) {
                    VolleyLog.wtf("Unsupported Encoding while trying to get the bytes of %s using %s", requestBody, "utf-8");
                    return null;
                }
            }

            @Override
            public String getBodyContentType() {
                return "application/json";
            }
        };

        queue.add(stringRequest);
    }


    /**
     * Populates the Recycler View to show the movie(s) that are avaialable in theaters
     * or were searched by the user.
     */
    private void buildRecyclerView() {
        // initialize adapter class
        adapter = new MoviesAdapter(moviesModelArrayList, MoviesActivity.this, this);

        // add layout manager to recycler view
        LinearLayoutManager manager = new LinearLayoutManager(this);
        moviesRV.setHasFixedSize(true);

        // set layout manager to the recycler view
        moviesRV.setLayoutManager(manager);

        // set adapter to the recycler view
        moviesRV.setAdapter(adapter);
    }

    /**
     * Takes the user to the next screen, TheatersAvailable, passing a string of all
     * the theaters the movie is showing at with it.
     * @param model This movie card that is being clicked on
     */
    @Override
    public void onClick(MoviesModel model) {
        Intent intent = new Intent(getApplicationContext(), TheatersAvailable.class);

        // put into next screen
        intent.putExtra("availableTheaters", availableTheaters);
        startActivity(intent);
    }


    /**
     * Helper method to show a toast message.
     * @param message
     */
    // Helper method to show a toast message
    private void showMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
