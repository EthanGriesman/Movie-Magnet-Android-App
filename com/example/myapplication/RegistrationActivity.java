package com.example.myapplication;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
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
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;

/**
 * Activity to register a new user with email, username, password, and DOB
 *
 * POST request used to append new account created into database with existing users.
 */
public class RegistrationActivity extends AppCompatActivity {
    // Declare UI elements
    EditText userNameEditText, ageGroupEditText, passwordEditText, emailEditText;
    Button registerButton;
    TextView emailErrorTextView;
    // Fade out animation for invalid email
    private AlphaAnimation fadeOutAnimation;
    private Handler handler = new Handler();

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        // Initialize UI elements
        userNameEditText = findViewById(R.id.userNameEditText);
        ageGroupEditText = findViewById(R.id.ageGroupEditText);
        EditText password = findViewById(R.id.passwordEditText);
        EditText email = findViewById(R.id.emailEditText);
        registerButton = findViewById(R.id.registerButton);
        emailErrorTextView = findViewById(R.id.emailErrorTextView);
        emailErrorTextView.setVisibility(View.GONE); // Initially not visible

        // Create a fade-out animation
        fadeOutAnimation = new AlphaAnimation(1.0f, 0.0f);
        fadeOutAnimation.setDuration(5000); // 5 seconds (adjust as needed)

        // Set a click listener for the registration button
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Get user-entered data
                String enteredName = userNameEditText.getText().toString();
                String enteredAgeGroup = ageGroupEditText.getText().toString();
                String enteredPassword = password.getText().toString();
                String enteredEmail = email.getText().toString();

                // Check if each field has a valid entry
                if (!TextUtils.isEmpty(enteredName) && !TextUtils.isEmpty(enteredAgeGroup) &&
                        !TextUtils.isEmpty(enteredPassword) && !TextUtils.isEmpty(enteredEmail) &&
                        isValidEmail(enteredEmail)) {
                    // Create a JSON object to represent the user data
                    JSONObject userData = new JSONObject();
                    try {
                        userData.put("name", enteredName);
                        userData.put("ageGroup", enteredAgeGroup);
                        userData.put("password", enteredPassword);
                        userData.put("email", enteredEmail);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    // Send the JSON data in the body of the POST request
                    registerUser(userData);
                } else {
                    // Display an error message if any field is empty or email is invalid
                    if (!isValidEmail(enteredEmail)) {
                        // Show the email validation error message
                        emailErrorTextView.setVisibility(View.VISIBLE);
                        // Apply the fade-out animation
                        emailErrorTextView.startAnimation(fadeOutAnimation);

                        // Hide the error message after animation ends
                        fadeOutAnimation.setAnimationListener(new Animation.AnimationListener() {
                            @Override
                            public void onAnimationStart(Animation animation) {
                                // Not needed
                            }
                            @Override
                            public void onAnimationEnd(Animation animation) {
                                emailErrorTextView.setVisibility(View.GONE);
                            }
                            @Override
                            public void onAnimationRepeat(Animation animation) {
                                // Not needed
                            }
                        });
                    } else {
                        // Hide the email validation error message if the email is valid
                        Toast.makeText(RegistrationActivity.this, "Please fill in all remaining fields!", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    /**
     * Helper method to test if a user entered a valid email.
     *
     * @param email The email to validate.
     * @return True if the email is valid, false otherwise.
     */
    private boolean isValidEmail(String email) {
        // Define a regular expression pattern for valid email addresses
        String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";

        // Use the Pattern and Matcher classes to check if the email matches the pattern
        return email.matches(emailPattern);
    }

    /**
     * Register the user and handle the registration request using a POST request.
     *
     * @param userData The JSON object containing user's registration data.
     */
    private void registerUser(JSONObject userData) {
        String registrationUrl = "http://coms-309-040.class.las.iastate.edu:8080/users";

        // Create a JSON registration request using POST
        JsonObjectRequest registrationRequest = new JsonObjectRequest(Request.Method.POST, registrationUrl, userData,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // Registration successful, display a message
                        Toast.makeText(RegistrationActivity.this, "New account created!", Toast.LENGTH_SHORT).show();
                        // Redirect to the login activity or any other desired activity
                        Intent intent = new Intent(RegistrationActivity.this, LoginActivity.class);
                        startActivity(intent);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // Display an error message on request failure
                        Log.e("RegistrationActivity", "Registration Error: " + error.getMessage());
                        Toast.makeText(RegistrationActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

        // Add the registration request to the Volley request queue
        Volley.newRequestQueue(RegistrationActivity.this).add(registrationRequest);
    }
}
