package SettingsTab;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.myapplication.LoginActivity;
import com.example.myapplication.R;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

/**
 * Activity that allows the user to change their username. It provides a form to input a new username and
 * communicate with a backend server to update it.
 *
 * PUT request overrides username initially entered in by user during registration
 */
public class ChangeUserActivity extends AppCompatActivity {

    private EditText newUsernameEditText; // EditText for the new username input.
    private Button changeUsernameButton; // EditText for the new username input.



    /**
     * Initializes the activity. This includes setting up the user interface, retrieving the current user's email from
     * SharedPreferences, and preparing the button's onClick listener to handle the update process.
     *
     * @param savedInstanceState A Bundle containing the activity's previously saved state. If the activity
     *                           has never existed before, the value of savedInstanceState will be null.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_user);

        newUsernameEditText = findViewById(R.id.newUsernameEditText);
        changeUsernameButton = findViewById(R.id.changeUsernameButton);

        SharedPreferences preferences = getSharedPreferences("UserPreferences", MODE_PRIVATE);
        String currentEmail = preferences.getString("email", "");
        Log.d("ChangeUserActivity", "Current Email: " + currentEmail);

        changeUsernameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newUsername = newUsernameEditText.getText().toString();
                if (newUsername.isEmpty()) {
                    displayToast("New username cannot be empty");
                } else {
                    updateUsername(currentEmail, newUsername);
                }
            }
        });
    }

    /**
     * Displays a toast message on the screen. Toast messages are a great way to provide feedback that is less
     * intrusive than a dialog.
     *
     * @param message The string message to display in the toast.
     */
    private void displayToast(String message) {
        Toast.makeText(ChangeUserActivity.this, message, Toast.LENGTH_SHORT).show();
    }

    /**
     * Handles updating the username by making a PUT request to the server. It sends the new username
     * and handles the server's response, indicating success or failure to the user.
     *
     * @param currentEmail The email address associated with the current username, used to identify the user.
     * @param newUsername  The new username that the user wishes to update to.
     */
    private void updateUsername(String currentEmail, String newUsername) {
        String updateUrl = "http://coms-309-040.class.las.iastate.edu:8080/users/updateUsername/" + currentEmail;

        String requestBody = newUsername;

        StringRequest updateRequest = new StringRequest(Request.Method.PUT, updateUrl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if ("{\"message\":\"success\"}".equals(response)) {
                            displayToast("Username successfully updated");
                            Intent intent = new Intent(ChangeUserActivity.this, LoginActivity.class);
                            startActivity(intent);
                        } else {
                            displayToast("Failed to update username");
                            Log.e("ChangeUserActivity", "Server Response: " + response);
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error instanceof TimeoutError) {
                    Log.e("ChangeUserActivity", "Timeout Error");
                    displayToast("The connection has timed out. Please try again.");
                } else if (error instanceof NoConnectionError) {
                    Log.e("ChangeUserActivity", "No Connection Error");
                    displayToast("No internet connection. Please check your connection and try again.");
                } else if (error instanceof ServerError) {
                    Log.e("ChangeUserActivity", "Server Error");
                    ServerError serverError = (ServerError) error;
                    if (serverError.networkResponse != null) {
                        Log.e("ChangeUserActivity", "Server Error Status Code: " + serverError.networkResponse.statusCode);
                        try {
                            String body = new String(serverError.networkResponse.data, "UTF-8");
                            Log.e("ChangeUserActivity", "Server Error Body: " + body);
                            displayToast("Server error: " + body);
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                            displayToast("An error occurred while processing the server error");
                        }
                    } else {
                        displayToast("An error occurred on the server side");
                    }
                } else if (error instanceof NetworkError) {
                    Log.e("ChangeUserActivity", "Network Error");
                    displayToast("Network error. Please check your connection and try again.");
                } else {
                    if (error != null) {
                        if (error.getMessage() != null) {
                            Log.e("ChangeUserActivity", "Volley Error: " + error.getMessage());
                        } else {
                            Log.e("ChangeUserActivity", "Unknown Volley Error");
                        }
                    } else {
                        Log.e("ChangeUserActivity", "Unknown Error Occurred");
                    }
                    displayToast("An unknown error occurred");
                }
            }
        }) {
            @Override
            public byte[] getBody() {
                return requestBody.getBytes();
            }

            @Override
            public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }
        };

        Volley.newRequestQueue(ChangeUserActivity.this).add(updateRequest);
    }
}
