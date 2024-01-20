package com.example.myapplication;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.view.MotionEvent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import SocialTab.SocialActivity;

/**
 * Activity for user login using email and password with POST and GET requests.
 * POST request used for user authentication
 * GET request used for retrieval of newly logged in user for later use thru SharedPreferences
 */
public class LoginActivity extends AppCompatActivity {

    // UI elements
    EditText password, email;
    Button loginButton, registerButton;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_screen);

        // Initialize UI elements
        password = findViewById(R.id.password);
        email = findViewById(R.id.email);
        loginButton = findViewById(R.id.loginButton);
        registerButton = findViewById(R.id.registerButton);

        // Set an OnTouchListener for the Log In button
        loginButton.setOnTouchListener((view, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                // Get user-entered data
                String enteredEmail = email.getText().toString();
                String enteredPassword = password.getText().toString();

                // Check if entered data is not empty
                if (!TextUtils.isEmpty(enteredEmail) && !TextUtils.isEmpty(enteredPassword)) {
                    if (checkIfAccountDeleted(enteredEmail)) {
                        Toast.makeText(LoginActivity.this, "This account has been deleted.", Toast.LENGTH_SHORT).show();
                    }
                    try {
                        // Create a JSON object with login data
                        JSONObject loginData = new JSONObject();
                        loginData.put("email", enteredEmail);
                        loginData.put("password", enteredPassword);

                        // Make the JSON login request
                        loginUser(loginData);

                        // Navigate to HomeActivity
                        Intent homeIntent = new Intent(LoginActivity.this, HomeActivity.class);
                        Intent socialIntent = new Intent(LoginActivity.this, SocialActivity.class);
                        startActivity(homeIntent);

                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(LoginActivity.this, "JSON Exception: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // Display an error message if data is not valid
                    Toast.makeText(LoginActivity.this, "Please enter a valid email and password", Toast.LENGTH_SHORT).show();
                }
            }
            return true;
        });

        // Set an OnClickListener for the registration button
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Start the registration activity
                Intent intent = new Intent(LoginActivity.this, RegistrationActivity.class);
                startActivity(intent);
            }
        });
    }

    private boolean checkIfAccountDeleted(String enteredEmail) {
        SharedPreferences preferences = getSharedPreferences("UserPreferences", MODE_PRIVATE);
        return preferences.getBoolean(email + "_deleted", false);
    }

    /**
     * Log in the user and handle the login request using a POST request to validate successful login
     *
     * @param loginData The JSON object containing user's login data.
     */
    private void loginUser(JSONObject loginData) {
        String authenticationUrl = "http://coms-309-040.class.las.iastate.edu:8080/users/auth";

        // Create a StringRequest for the POST request
        StringRequest authenticationRequest = new StringRequest(Request.Method.POST, authenticationUrl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (response.equals("{\"message\":\"failure\"}")) {
                            // Invalid password, show an error message to the user
                            Toast.makeText(LoginActivity.this, "Error: account deleted or invalid password", Toast.LENGTH_SHORT).show();
                        } else {
                            String enteredEmail = email.getText().toString();
                            if (!response.equals("null")) {
                                Log.d("LoginResponse", response);
                                Log.d("EnteredEmail", enteredEmail);
                                saveEmailToSharedPreferences(enteredEmail);
                                sendGetRequest(enteredEmail);
                                Intent homeIntent = new Intent(LoginActivity.this, HomeActivity.class);
                                startActivity(homeIntent);
                                finish(); // Close login activity
                            }
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("LoginError", error.toString()); // Log the error
                        // Display a generic error message on network error
                        Toast.makeText(LoginActivity.this, "Network Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        Intent failedIntent = new Intent(LoginActivity.this, LoginActivity.class);
                        startActivity(failedIntent);
                    }
                }) {
            @Override
            public byte[] getBody() {
                // Convert the JSON object to a string and return it as the request body
                return loginData.toString().getBytes();
            }

            @Override
            public String getBodyContentType() {
                return "application/json";
            }
        };

        // Add the login request to the Volley request queue
        Volley.newRequestQueue(LoginActivity.this).add(authenticationRequest);
    }

    /**
     * Custom toast message for login
     * @param message
     */
    private void showCustomToast(String message) {
        // Create a custom view for the toast
        View layout = getLayoutInflater().inflate(R.layout.custom_login_toast, findViewById(R.id.custom_login_layout));

        // Set the text message
        TextView text = layout.findViewById(R.id.toast_text);
        text.setText(message);

        // Create and display the custom toast
        Toast toast = new Toast(getApplicationContext());
        toast.setDuration(Toast.LENGTH_SHORT); // Adjust the duration as needed
        toast.setView(layout);
        toast.show();
    }

    /**
     * Upon successful login, store the "email" entry from the JSON Array as a string
     * @param email
     */
    private void saveEmailToSharedPreferences(String email) {
        SharedPreferences preferences = getSharedPreferences("UserPreferences", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("email", email);
        editor.apply();
    }

    /**
     * Send a GET request to retrieve the username associated with the entered email.
     *
     * @param enteredEmail The email entered during login.
     */
    private void sendGetRequest(String enteredEmail) {
        // Construct the URL with the entered email
        String getRequestUrl = "http://coms-309-040.class.las.iastate.edu:8080/users";

        StringRequest getRequest = new StringRequest(Request.Method.GET, getRequestUrl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Handle the GET request response here
                        Log.d("GETResponse", response);

                        if (response != null && !response.isEmpty()) {
                            try {
                                JSONArray jsonArray = new JSONArray(response);
                                boolean isUserFound = false;
                                String foundUsername = null;

                                for (int i = 0; i < jsonArray.length(); i++) {
                                    JSONObject userObject = jsonArray.getJSONObject(i);
                                    String userEmail = userObject.optString("email", "");
                                    String userAgeGroup = userObject.optString("ageGroup", "");

                                    if (userEmail.equals(enteredEmail)) {
                                        // User's email found
                                        isUserFound = true;

                                        // Email matched, store email and find the username
                                        saveEmailToSharedPreferences(enteredEmail);
                                        // store the AgeGroup
                                        saveAgeGroupToSharedPreferences(userAgeGroup);
                                        int index = i;
                                        while (index >= 0) {
                                            JSONObject userObjectReverse = jsonArray.getJSONObject(index);
                                            if (userObjectReverse.has("name")) {
                                                foundUsername = userObjectReverse.optString("name", "");
                                                break; // Exit the loop when the username is found
                                            }
                                            index--;
                                        }
                                        break; // Exit the loop when the email is found
                                    }
                                }

                                if (isUserFound & foundUsername != null) {
                                    showCustomToast("Login successful!");
                                    saveUsernameToSharedPreferences(foundUsername);
                                    showCustomToast("Welcome, " + foundUsername);
                                    Intent homeIntent = new Intent(LoginActivity.this, HomeActivity.class);
                                    startActivity(homeIntent);
                                    finish(); // Close login activity
                                } else {
                                    // User's email not found, show error and redirect to login
                                    Log.e("GETResponse", "No user found with the entered email.");
                                    showCustomToast("Account doesn't exist!");
                                    Intent failedIntent = new Intent(LoginActivity.this, LoginActivity.class);
                                    startActivity(failedIntent);
                                    finish(); // Close current activity
                                }

                            } catch (JSONException e) {
                                // Handle JSON parsing error
                                Log.e("GETResponse", "Error parsing JSON: " + e.getMessage());
                            }
                        } else {
                            // Log an error if the response is empty
                            Log.e("GETResponse", "Response is empty");
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // Log the error
                        Log.e("GETError", "Error during GET request: " + error.toString());

                        // Display a generic error message on network error
                        Toast.makeText(LoginActivity.this, "Network Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

        // Add the GET request to the Volley request queue
        Volley.newRequestQueue(LoginActivity.this).add(getRequest);
    }


    /**
     * Save the "username" from JSON Array as a string to SharedPreferences for later use.
     *
     * @param username The username to be saved.
     */
    private void saveUsernameToSharedPreferences(String username) {
        SharedPreferences preferences = getSharedPreferences("UserPreferences", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("username", username);
        editor.apply();
    }

    /**
     * Save the rating to SharedPreferences for later use.
     *
     * @param ageGroup The ageGroup to be saved.
     */
    private void saveAgeGroupToSharedPreferences(String ageGroup) {
        SharedPreferences preferences = getSharedPreferences("UserPreferences", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("ageGroup", ageGroup);
        editor.apply();
    }

}
