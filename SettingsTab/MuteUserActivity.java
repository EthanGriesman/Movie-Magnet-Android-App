package SettingsTab;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import java.net.URLEncoder;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.myapplication.LoginActivity;
import com.example.myapplication.R;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.HashMap;

public class MuteUserActivity extends AppCompatActivity {
    private JSONArray usersJsonArray;
    private Spinner spinnerEmails;
    private ArrayAdapter<String> adapter;
    private final ArrayList<String> emailList = new ArrayList<>();
    private HashMap<String, JSONObject> userEmailToDataMap = new HashMap<>();
    private String selectedEmail;
    private boolean muteState;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mute_user);
        spinnerEmails = findViewById(R.id.spinnerUsers);

        Button buttonSelectUsers = findViewById(R.id.buttonSelectUsers);
        Button buttonMuteUser = findViewById(R.id.buttonMuteUser);
        Button buttonUnMuteUser = findViewById(R.id.buttonUnMuteUser);

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, emailList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerEmails.setAdapter(adapter);

        buttonSelectUsers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("MuteUserActivity", "Select Users Button Clicked");
                fetchEmails();
                spinnerEmails.post(new Runnable() {
                    @Override
                    public void run() {
                        if (adapter.getCount() > 0) {
                            showUserSelectionDialog();
                        }
                    }
                });
            }
        });

        spinnerEmails.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedEmail = (String) parent.getItemAtPosition(position);
                Toast.makeText(MuteUserActivity.this, "Email selected: " + selectedEmail, Toast.LENGTH_SHORT).show();
                Log.d("MuteUserActivity", "User selected: " + selectedEmail);
                spinnerEmails.setVisibility(View.GONE);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        buttonMuteUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMuteRequest(selectedEmail);
            }
        });

        buttonUnMuteUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendUnmuteRequest(selectedEmail);
            }
        });
    }

    private void showUserSelectionDialog() {
        // Create a new AlertDialog.Builder to build a dialog interface
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        // Set the title of the dialog
        builder.setTitle("Select an Email");
        // Set an adapter for the dialog, which provides a list of items to select from
        builder.setAdapter(adapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Get the selected email when an item is clicked
                selectedEmail = adapter.getItem(which);
                // Iterate through the users JSON array to find the user object with the selected email
                for (int i = 0; i < usersJsonArray.length(); i++) {
                    try {
                        // Get the user object at the current index
                        JSONObject userObject = usersJsonArray.getJSONObject(i);
                        // Check if the email of this user object matches the selected email
                        if (userObject.getString("email").equals(selectedEmail)) {
                            // Toggle the mute state of the selected user
                            muteState = !userObject.getBoolean("mute");
                            // Log the JSON object of the selected user for debugging purposes
                            Log.d("MuteUserActivity", "Selected User Entry: " + userObject);
                            // Break the loop as the required user is found
                            break;
                        }
                    } catch (JSONException e) {
                        // Handle any JSON parsing exceptions
                        e.printStackTrace();
                    }
                }
                // Log the email selected and the new mute state for debugging purposes
                Log.d("MuteUserActivity", "Email selected: " + selectedEmail + ", New Mute State: " + muteState);
                // Show a custom toast message with the selected email
                showCustomToast("Email selected: " + selectedEmail);
            }
        });
        // Set a "Cancel" button on the dialog with a null click listener (does nothing when clicked)
        builder.setNegativeButton("Cancel", null);
        // Create and show the AlertDialog
        AlertDialog dialog = builder.create();
        dialog.show();
    }


    private void showCustomToast(String message) {
        Log.d("MuteUserActivity", "Showing custom toast: " + message);
        View layout = getLayoutInflater().inflate(R.layout.custom_login_toast2, findViewById(R.id.custom_login_layout));
        TextView text = layout.findViewById(R.id.toast_text);
        text.setText(message);
        Toast toast = new Toast(getApplicationContext());
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(layout);
        toast.show();
    }

    private void fetchEmails() {
        // Define the URL for the API endpoint to fetch user data.
        String url = "http://coms-309-040.class.las.iastate.edu:8080/users";
        // Initialize a new Volley request queue for network requests.
        RequestQueue queue = Volley.newRequestQueue(this);
        // Create a new StringRequest for a GET request to the specified URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // This block is executed upon successful response from the server.
                        try {
                            // Convert the response string into a JSONArray.
                            usersJsonArray = new JSONArray(response);
                            // Clear existing data in emailList and userEmailToDataMap.
                            emailList.clear();
                            userEmailToDataMap.clear();
                            // Iterate through the JSONArray.
                            for (int i = 0; i < usersJsonArray.length(); i++) {
                                // Extract each user object from the JSONArray.
                                JSONObject userObject = usersJsonArray.getJSONObject(i);
                                // Extract the email address from each user object.
                                String userEmail = userObject.getString("email");
                                // Add the extracted email to the emailList.
                                emailList.add(userEmail);
                                // Map the user's email to their JSONObject in userEmailToDataMap.
                                userEmailToDataMap.put(userEmail, userObject);
                            }
                            // Notify the adapter that the data set has changed to update the UI.
                            adapter.notifyDataSetChanged();
                        } catch (JSONException e) {
                            // Handle JSON parsing exceptions.
                            e.printStackTrace();
                            Toast.makeText(MuteUserActivity.this, "Error parsing user data", Toast.LENGTH_SHORT).show();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // This block is executed when there is an error in the network request.
                Toast.makeText(MuteUserActivity.this, "Error fetching users", Toast.LENGTH_SHORT).show();
            }
        });

        // Add the request to the Volley request queue to be executed.
        queue.add(stringRequest);
    }

    private void sendMuteRequest(String selectedEmail) {

        // Construct the URL for the request
        String url = "http://coms-309-040.class.las.iastate.edu:8080/users/admin/mute/" + selectedEmail;

        // Create a PUT request to mute the user
        StringRequest muteRequest = new StringRequest(Request.Method.PUT, url,
                response -> {
                    // Check if the response is successful
                    if (response.equals("{\"message\":\"success\"}")) {
                        showCustomToast(selectedEmail + " has been muted");
                        updateMuteStatusInPreferences(selectedEmail, true);
                    } else {
                        // Handle failure response
                        Toast.makeText(MuteUserActivity.this, "Failed to mute user: " + response, Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    // Detailed error handling
                    if (error.networkResponse != null) {
                        String errorData = new String(error.networkResponse.data, StandardCharsets.UTF_8);
                        Log.e("MuteUserActivity", "Error muting user: " + errorData);
                        Toast.makeText(MuteUserActivity.this, "Error muting user: " + errorData, Toast.LENGTH_LONG).show();
                    } else {
                        Log.e("MuteUserActivity", "Network error: " + error.toString());
                        Toast.makeText(MuteUserActivity.this, "Network error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }) {
            @Override
            public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }
        };

        // Add the request to the Volley request queue
        Volley.newRequestQueue(this).add(muteRequest);
    }

    private void updateMuteStatusInPreferences(String email, boolean isMuted) {
        SharedPreferences sharedPref = getSharedPreferences("AppSettings", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean(email + "_isMuted", isMuted);
        editor.apply();
    }

    private void sendUnmuteRequest(String selectedEmail) {
        // Construct the URL for the request
        String url = "http://coms-309-040.class.las.iastate.edu:8080/users/admin/unmute/" + selectedEmail;

        // Create a PUT request to unmute the user
        StringRequest unmuteRequest = new StringRequest(Request.Method.PUT, url,
                response -> {
                    // Check if the response is successful
                    if (response.equals("{\"message\":\"success\"}")) {
                        showCustomToast(selectedEmail + " is now unmuted");
                        updateMuteStatusInPreferences(selectedEmail, false);
                    } else {
                        // Handle failure response
                        Toast.makeText(MuteUserActivity.this, "Failed to unmute user: " + response, Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    // Detailed error handling
                    if (error.networkResponse != null) {
                        String errorData = new String(error.networkResponse.data, StandardCharsets.UTF_8);
                        Log.e("UnmuteUserActivity", "Error unmuting user: " + errorData);
                        Toast.makeText(MuteUserActivity.this, "Error unmuting user: " + errorData, Toast.LENGTH_LONG).show();
                    } else {
                        Log.e("UnmuteUserActivity", "Network error: " + error.toString());
                        Toast.makeText(MuteUserActivity.this, "Network error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }) {
            @Override
            public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }
        };

        // Add the request to the Volley request queue
        Volley.newRequestQueue(this).add(unmuteRequest);
    }






}
