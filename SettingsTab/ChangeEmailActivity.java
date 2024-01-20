package SettingsTab;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.myapplication.LoginActivity;
import com.example.myapplication.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Activity for changing the current user's email address. It performs a check to ensure the new email is not already in use
 * and updates the email if it is unique. A successful update redirects the user to the LoginActivity.
 *
 * PUT request to modify the email originally specified by user during registration
 */
public class ChangeEmailActivity extends AppCompatActivity {

    private EditText newEmailEditText; // EditTextd field for inputting new email
    private Button changeEmailButton; // Button to submit the email change request

    /**
     * Called when the activity is starting. This is where most initialization should go: calling setContentView(int)
     * to inflate the activity's UI, using findViewById(int) to programmatically interact with widgets in the UI,
     * setting up listeners, and initializing class-scope variables.
     *
     * @param savedInstanceState If the activity is being re-initialized after previously being shut down,
     *                           this Bundle contains the data it most recently supplied in onSaveInstanceState(Bundle).
     *                           Note: Otherwise it is null.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_email);

        newEmailEditText = findViewById(R.id.newEmailEditText);
        changeEmailButton = findViewById(R.id.changeEmailButton);

        SharedPreferences preferences = getSharedPreferences("UserPreferences", MODE_PRIVATE);
        String currentEmail = preferences.getString("email", "");
        Log.d("ChangeEmailActivity", "Current Email: " + currentEmail);

        changeEmailButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newEmail = newEmailEditText.getText().toString().trim();
                Log.d("ChangeEmailActivity", "New Email Entered: " + newEmail);
                if (newEmail.isEmpty()) {
                    displayToast("New email cannot be empty");
                } else {
                    checkIfEmailExists(newEmail);
                }
            }
        });
    }

    /**
     * Checks if the new email entered by the user already exists by making a GET request to the server.
     *
     * @param newEmail The new email address to be validated against the server's user database.
     */
    private void checkIfEmailExists(String newEmail) {
        String url = "http://coms-309-040.class.las.iastate.edu:8080/users";

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        Log.d("ChangeEmailActivity", "GET Response: " + response.toString());
                        try {
                            boolean emailExists = false;
                            for (int i = 0; i < response.length(); i++) {
                                JSONObject user = response.getJSONObject(i);

                                if (user.has("email")) {
                                    String email = user.getString("email");
                                    if (newEmail.equals(email)) {
                                        emailExists = true;
                                        break;
                                    }
                                } else {
                                    Log.d("ChangeEmailActivity", "Email field is missing in JSON object");
                                }
                            }

                            if (emailExists) {
                                Log.d("ChangeEmailActivity", "New email is already in use: " + newEmail);
                                displayToast("{\"message\":\"Email already exists\"}");
                            } else {
                                Log.d("ChangeEmailActivity", "New email is not in use: " + newEmail);
                                SharedPreferences preferences = getSharedPreferences("UserPreferences", MODE_PRIVATE);
                                String currentEmail = preferences.getString("email", "");
                                updateEmail(currentEmail, newEmail);
                            }

                        } catch (JSONException e) {
                            Log.e("ChangeEmailActivity", "Json parsing error: " + e.getMessage());
                            displayToast("Something went wrong. Please try again.");
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                displayToast("Error checking email existence");
                Log.e("ChangeEmailActivity", "Error: " + error.getMessage());
            }
        });

        Volley.newRequestQueue(this).add(jsonArrayRequest);
    }

    /**
     * Updates the current user's email to the new email if it does not already exist in the user database.
     *
     * @param currentEmail The current email address of the user.
     * @param newEmail     The new email address to replace the current email.
     */
    private void updateEmail(String currentEmail, String newEmail) {
        String updateUrl = "http://coms-309-040.class.las.iastate.edu:8080/users/updateEmail/" + currentEmail;
        String requestBody = newEmail;

        StringRequest updateRequest = new StringRequest(Request.Method.PUT, updateUrl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if ("{\"message\":\"success\"}".equals(response)) {
                            displayToast("Email successfully updated");
                            Intent intent = new Intent(ChangeEmailActivity.this, LoginActivity.class);
                            startActivity(intent);
                        } else {
                            displayToast("Failed to update email");
                            Log.e("ChangeEmailActivity", "Server Response: " + response);
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                handleVolleyError(error);
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

        Volley.newRequestQueue(ChangeEmailActivity.this).add(updateRequest);
    }

    /**
     * Displays a short toast message on the screen.
     *
     * @param message The message text to be shown in the toast.
     */
    private void displayToast(String message) {
        Toast.makeText(ChangeEmailActivity.this, message, Toast.LENGTH_SHORT).show();
    }

    /**
     * Handles the error received from Volley when a network request fails.
     *
     * @param error The VolleyError object containing details about the error occurred during the network request.
     */
    private void handleVolleyError(VolleyError error) {
        if (error instanceof com.android.volley.NetworkError) {
            displayToast("Cannot connect to Internet...Please check your connection!");
        } else if (error instanceof com.android.volley.ServerError) {
            displayToast("The server could not be found. Please try again after some time!!");
        } else if (error instanceof com.android.volley.AuthFailureError) {
            displayToast("Authentication failed. Please check your credentials.");
        } else if (error instanceof com.android.volley.NoConnectionError) {
            displayToast("Cannot connect to Internet...Please check your connection!");
        } else if (error instanceof com.android.volley.TimeoutError) {
            displayToast("Connection TimeOut! Please check your internet connection.");
        } else {
            displayToast("Something went wrong. Please try again.");
            Log.e("ChangeEmailActivity", "Error: " + error.getMessage());
        }
    }
}
