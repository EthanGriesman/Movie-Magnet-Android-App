package TheatersTab;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.myapplication.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import MoviesTab.MoviesActivity;
import MoviesTab.MoviesAdapter;
import MoviesTab.MoviesModel;

import MoviesTab.TheatersAvailable;
import TheatersTab.TheaterAdapter;

public class TheaterSearch extends AppCompatActivity implements MoviesAdapter.OnClickListener {

    // user info
    private String theaterName;
    private String email;

    // holds variables for ui components
    private RecyclerView movieRV;
    private Button backBtn;

    // variables for adapter + movie array list
    private MoviesAdapter adapter;
    private ArrayList<MoviesModel> moviesModelArrayList;

    /**
     * Sets up page for user viewing. This includes button clicking instructions and retrieving saved
     * data (email).
     *
     * Whichever theater the user clicked, the movies that are avaiable to the user will appear on
     * the page, and they will be able to move back to the theaters tab by clicking the "Go back" button.
     * @param savedInstanceState If the activity is being re-initialized after previously
     *                           being shut down then this Bundle contains the data it most
     *                           recently supplied in onSaveInstanceState(Bundle)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_theater_search);

        // variables
        movieRV = findViewById(R.id.RVMovies);
        moviesModelArrayList = new ArrayList<>();
        theaterName = getIntent().getStringExtra("theaterName");
        backBtn = findViewById(R.id.backBtn);

        SharedPreferences sh = getSharedPreferences("UserPreferences", MODE_PRIVATE);
        email = sh.getString("email", "");

        // request the theater's movie show times
        try {
            // create json body object
            JSONObject requestBody = new JSONObject();
            requestBody.put("email", email);
            Log.d("requestBody = ", requestBody.toString());

            getTheaterShowtimes(requestBody);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        // build the recycler view
        buildRecyclerView();

        backBtn.setOnClickListener(new View.OnClickListener(){
            /**
             * Sends the user back to the TheatersActivity class
             * @param view This current View.
             */
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(TheaterSearch.this, TheatersActivity.class);
                startActivity(intent);
            }
        });
    }

    /**
     * Makes a request to the server to retrieve a JSONArray of all theaters in
     * the area.
     * @param requestBody This user's email in a JSON Object
     * @throws JSONException Runtime Exception
     */
    private void getTheaterShowtimes(JSONObject requestBody) throws JSONException {

        String searchUrl = "http://coms-309-040.class.las.iastate.edu:8080/theaters/showtimes/" + theaterName;

        // create a new variable for request queue
        RequestQueue queue = Volley.newRequestQueue(TheaterSearch.this);

        StringRequest stringRequest = new StringRequest(Request.Method.POST, searchUrl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        // recycler view is now visible
                        movieRV.setVisibility(View.VISIBLE);
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
                                //String movieGenre = responseObj.getString("genre");
                                String movieGenre = "Genre currently unavailable";
                                String movieRuntime = responseObj.getString("runtime");
                                String movieTimes = responseObj.getString("times");
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
                        Toast.makeText(TheaterSearch.this, errorMessage, Toast.LENGTH_LONG).show();

                        // Here you could also add a button or a mechanism to retry the request
                    }
                }) {

            /**
             * Gets the body from the JSONObject.
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
     * Populates the Recycler View to show the movie(s) that are avaialable in the
     * searched theater.
     */
    private void buildRecyclerView() {
        adapter = new MoviesAdapter(moviesModelArrayList, TheaterSearch.this, this);

        // add layout manager to recycler view
        LinearLayoutManager manager = new LinearLayoutManager(this);
        movieRV.setHasFixedSize(true);

        // set layout manager to recycler view
        movieRV.setLayoutManager(manager);

        // set adapter to recycler view
        movieRV.setAdapter(adapter);
    }

    @Override
    public void onClick(MoviesModel model) {
        // does nothing when clicked
    }
}