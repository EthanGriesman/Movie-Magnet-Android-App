package SocialTab;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;

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

import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;

import android.content.Intent;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

import MoviesTab.MoviesActivity;
import MoviesTab.MoviesModel;
import SettingsTab.SettingsActivity;
import TheatersTab.TheatersActivity;


/**
 * Users of the same age group are able to chat with each other here via DMing or
 * broadcasting their message to others.
 */
public class SocialActivity extends AppCompatActivity {

    // variables for ui components
    private ListView listView;
    private Button addBtn, homeBtn, moviesBtn, theatersBtn, chatRoomBtn;
    private ImageView settingsBtn;
    private EditText addTV;
    private ArrayAdapter<String> arr;

    // user info
    private String email;
    private String[] friendsList; // usernames

    private String[] friendsEmails;
    private JSONArray allUsers;

    private static final String TAG = "SocialActivity";


    // url
    private String url;

    // GestureDetector
    private GestureDetector gestureDetector;

    /**
     * Sets up the pages elements including  as well as retrieving saved user data (username and ageGroup).
     * @param savedInstanceState If the activity is being re-initialized after previously
     *                           being shut down then this Bundle contains the data it most
     *                           recently supplied in onSaveInstanceState(Bundle)
     */
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_social2);

        gestureDetector = new GestureDetector(this, new SocialActivity.SwipeGestureDetector());

        /* initialize UI elements */
        chatRoomBtn = findViewById(R.id.chatRoomBtn);
        listView = findViewById(R.id.listView);
        addBtn = findViewById(R.id.addBtn);
        homeBtn = findViewById(R.id.homeBtn);
        moviesBtn = findViewById(R.id.moviesBtn);
        theatersBtn = findViewById(R.id.theatersBtn);
        settingsBtn = findViewById(R.id.settingsBtn);

        if (email == null || email.isEmpty()) {
            Log.e(TAG, "Email is null or empty");
            // Handle the case where email is null or empty
            return; // Exit the method or show an error message
        }

        SharedPreferences sh = getSharedPreferences("UserPreferences", MODE_PRIVATE);
        email = sh.getString("email", "");

        url = "http://coms-309-040.class.las.iastate.edu:8080/users/friends/" + email;
        friendsList = new String[]{};
        friendsEmails = new String[]{};

        friendsListUpdate();

        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addTV = findViewById(R.id.friendUsernameTextView);
                String addUsername = addTV.getText().toString(); // username to add to friend's list
                boolean foundUser = false;
                // check if the user exists
                for (int i = 0; i < allUsers.length(); i++) {
                    try {
                        JSONObject user = allUsers.getJSONObject(i);
                        String userUsername = user.getString("name");

                        // finds username
                        if (userUsername.equals(addUsername)) {
                            String friendEmail = user.getString("email");

                            // check if this user is already in friends list
                            for (int j = 0; j < friendsEmails.length; j++) {
                                if ( friendEmail.equals(friendsEmails[j]) ) {
                                    Toast.makeText(SocialActivity.this, "You're already friends with them.", Toast.LENGTH_LONG).show();
                                    foundUser = true;
                                    break;
                                }
                            }
                            if (foundUser) {
                                break;
                            }

                            // create json body object
                            JSONObject requestBody = new JSONObject();
                            requestBody.put("email", friendEmail);

                            addFriend(requestBody);
                            foundUser = true;
                        }

                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                }
                if (!foundUser) {
                    Toast.makeText(SocialActivity.this, "We could not find them.", Toast.LENGTH_LONG).show();
                }
                friendsListUpdate();
            }
        });

        AdapterView.OnItemClickListener messageClickedHandler = new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView parent, View v, int position, long id) {
                Log.d("Clicked item position: ", String.valueOf(position));

                AlertDialog.Builder builder = new AlertDialog.Builder(parent.getContext());
                builder.setTitle("Confirm deletion")
                        .setMessage("Remove this friend?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String removeFriendEmail = friendsEmails[position];
                                Log.d("Removing friend...", removeFriendEmail);

                                // update url
                                String removeFriendUrl = url + "/" + removeFriendEmail;

                                try {
                                    // create json body object
                                    JSONObject requestBody = new JSONObject();
                                    requestBody.put("name", removeFriendEmail);
                                    removeFriend(requestBody, removeFriendUrl);
                                } catch (JSONException e) {
                                    throw new RuntimeException(e);
                                }
                                friendsListUpdate();
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        };

        listView.setOnItemClickListener(messageClickedHandler);

        // go to Chat Room
        chatRoomBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SocialActivity.this, GroupChat.class);
                startActivity(intent);
            }
        });

        // Listener for theater button to start TheatersActivity.
        theatersBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SocialActivity.this, TheatersActivity.class);
                startActivity(intent);
            }
        });

        // Listener for movies button to start MoviesActivity.
        moviesBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SocialActivity.this, MoviesActivity.class);
                startActivity(intent);
            }
        });

        // Listener for social button to start SocialActivity.
        homeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SocialActivity.this, HomeActivity.class);
                startActivity(intent);
            }
        });

        // ImageView for settings and its click listener.
        settingsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SocialActivity.this, SettingsActivity.class);
                startActivity(intent);
            }
        });
    }

    public void friendsListUpdate() {
        getFriendList(); // emails
        findUsernamesOfFriends(); // emails -> usernames
    }

    public void getFriendList() {

        // create a new variable for request queue
        RequestQueue queue = Volley.newRequestQueue(SocialActivity.this);

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    /**
                     * Takes the response by the request and turns it into a card to be
                     * displayed on the RecyclerView.
                     * @param response A String returned by the StringRequest
                     */
                    @Override
                    public void onResponse(String response) {

                        // recycler view is now visible
                        //moviesRV.setVisibility(View.VISIBLE);

                        // turn response into a JSONArray
                        //Log.d("theaterName:", theaterName);
                        Log.d("Response:", response);

                        // split the response
                        response = response.replace("[", "");
                        response = response.replace("]", "");
                        response = response.replace("\"", "");

                        String[] friendsListSplit = response.split(",");
                        friendsEmails = new String[friendsListSplit.length];

                        // put response into friends list and keep the emails
                        for (int i = 0; i < friendsListSplit.length; i++) {
                            friendsEmails[i] = friendsListSplit[i];
                            Log.d("friendsEmails: ", friendsEmails[i]);
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
                        Toast.makeText(SocialActivity.this, errorMessage, Toast.LENGTH_LONG).show();

                        // Here you could also add a button or a mechanism to retry the request
                    }
                }) {
        };

        queue.add(stringRequest);
    }

    // send a get request of all users
    public void findUsernamesOfFriends() {
        // make a GET JSONArray request for all the users
            // pick out the usernames whose emails correspond and throw it into a list
            // return that list of usernames

        String usersUrl = "http://coms-309-040.class.las.iastate.edu:8080/users";

        // create a new variable for request queue
        RequestQueue queue = Volley.newRequestQueue(SocialActivity.this);

        StringRequest stringRequest = new StringRequest(Request.Method.GET, usersUrl,
                new Response.Listener<String>() {
                    /**
                     * Takes the response by the request and turns it into a card to be
                     * displayed on the RecyclerView.
                     * @param response A String returned by the StringRequest
                     */
                    @Override
                    public void onResponse(String response) {
                        try {
                            // turn response into a JSONArray
                            Log.d("Response:", response);

                            allUsers = new JSONArray(response);

                            friendsList = new String[friendsEmails.length];
                            Log.d("friendsEmails length: ", String.valueOf(friendsEmails.length));
                            Log.d("friendsList length: ", String.valueOf(friendsList.length));

                            for (int i = 0; i < allUsers.length(); i++) {
                                // create a new json object + get each object from the json array
                                // get each json object
                                JSONObject responseObj = allUsers.getJSONObject(i);

                                // get the key from the json object
                                String usernameAll = responseObj.getString("name");
                                String emailAll = responseObj.getString("email");

                                for (int j = 0; j < friendsEmails.length; j++) {
                                    if (friendsEmails[j].equals(emailAll)) {
                                        friendsList[j] = usernameAll;
                                        Log.d("usernameAll: ", usernameAll);
                                    }
                                }
                            }
                            buildListView();

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
                        Toast.makeText(SocialActivity.this, errorMessage, Toast.LENGTH_LONG).show();

                        // Here you could also add a button or a mechanism to retry the request
                    }
                }) {
        };

        queue.add(stringRequest);
    }

    // build listView
    public void buildListView() {
        if (friendsList != null) {
            arr = new ArrayAdapter<>(this, androidx.appcompat.R.layout.support_simple_spinner_dropdown_item, friendsList);
            listView.setAdapter(arr);
        } else {
            Log.e(TAG, "friendsList is null");
            // Handle null case appropriately
        }
    }

    // adding a friend
    public void addFriend(JSONObject requestBody) {

        // make the request
        RequestQueue queue = Volley.newRequestQueue(SocialActivity.this);

        StringRequest stringRequest = new StringRequest(Request.Method.PUT, url,
                new Response.Listener<String>() {
                    /**
                     * Takes the response by the request and turns it into a movie card to be
                     * displayed on the RecyclerView.
                     * @param response A String returned by the stringRequest
                     */
                    @Override
                    public void onResponse(String response) {

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
                        Toast.makeText(SocialActivity.this, errorMessage, Toast.LENGTH_LONG).show();

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

    public void removeFriend(JSONObject requestBody, String url) {

        // make the request
        RequestQueue queue = Volley.newRequestQueue(SocialActivity.this);

        StringRequest stringRequest = new StringRequest(Request.Method.DELETE, url,
                new Response.Listener<String>() {
                    /**
                     * Takes the response by the request and turns it into a movie card to be
                     * displayed on the RecyclerView.
                     * @param response A String returned by the stringRequest
                     */
                    @Override
                    public void onResponse(String response) {

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
                        Toast.makeText(SocialActivity.this, errorMessage, Toast.LENGTH_LONG).show();

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
            final int SWIPE_MIN_DISTANCE = 120; // You can define this with your preferred threshold
            final int SWIPE_THRESHOLD_VELOCITY = 200; // You can define this with your preferred speed threshold

            try {
                // Check if the swipe is horizontal and meets the minimum distance and velocity requirements
                if (Math.abs(e1.getX() - e2.getX()) > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                    // If swipe is from left to right (a right swipe)
                    if (e2.getX() > e1.getX()) {
                        // Swipe right - start TheatersActivity
                        Intent intent = new Intent(SocialActivity.this, TheatersTab.TheatersActivity.class);
                        startActivity(intent);
                        return true;
                    }
                    // Add logic for other swipes if necessary (left, up, down)
                }
            } catch (Exception e) {
                // Handle exception
                Log.e("SocialActivity", "Error processing the swipe gesture", e);
            }
            return false;
        }

    }
}