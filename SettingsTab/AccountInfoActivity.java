package SettingsTab;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.myapplication.LoginActivity;
import com.example.myapplication.R;

import java.util.HashMap;
import java.util.Map;

/**
 * Activity for managing user account information.
 * Allows users to delete their account, change email, and change username.
 *
 * DELETE request removes the user object associated with an email from the database
 */
public class AccountInfoActivity extends AppCompatActivity {
    // Initialize UI
    private Button deleteButton;
    private Button changePasswordButton;
    private Button changeEmailButton;
    private Button changeUsernameButton;

    /**
     * Initializes the activity, sets the content view, and sets up listeners for the buttons.
     * @param savedInstanceState If the activity is being re-initialized after previously being shut down,
     *                           this Bundle contains the data it most recently supplied in onSaveInstanceState(Bundle).
     *                           Note: Otherwise it is null.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_info);

        deleteButton = findViewById(R.id.deleteButton);
        changePasswordButton = findViewById(R.id.changePasswordButton);
        changeEmailButton = findViewById(R.id.changeEmailButton);
        changeUsernameButton = findViewById(R.id.changeUsernameButton);

        // Handle the "DELETE ACCOUNT" button click
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDeleteConfirmationDialog();
            }
        });

        // Handle the "CHANGE PASSWORD" button click
        changePasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showChangePasswordMessage(); // Show message stating only Admins can reset passwords
            }
        });

        // Handle the "CHANGE EMAIL" button click
        changeEmailButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                redirectToChangeEmailActivity();
            }
        });

        // Handle the "CHANGE USER" button click
        changeUsernameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                redirectToChangeUserActivity();
            }
        });
    }

    /**
     * Starts the activity to change the username.
     */
    private void redirectToChangeUserActivity() {
        Intent intent = new Intent(AccountInfoActivity.this, ChangeUserActivity.class);
        startActivity(intent);
    }

    /**
     * Displays a dialog to confirm account deletion.
     * If the user confirms, the account deletion process is initiated.
     */
    private void showDeleteConfirmationDialog() {
        SharedPreferences preferences = getSharedPreferences("UserPreferences", MODE_PRIVATE);
        String enteredEmail = preferences.getString("email", "");

        if (enteredEmail == null) {
            displayToast("Email is missing");
            return; // Don't proceed without email
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Confirm deletion")
                .setMessage("Are you sure you want to delete this account?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Perform the account deletion here
                        String deleteUrl = "http://coms-309-040.class.las.iastate.edu:8080/users/delete/" + enteredEmail;
                        deleteAccount(deleteUrl);
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

    /**
     * Shows a toast message indicating that only an admin can reset the password.
     */
    private void showChangePasswordMessage() {
        displayToast("Only admin can reset the password! Request in help chat");
    }

    /**
     * Starts the activity to change the email.
     */
    private void redirectToChangeEmailActivity() {
        Intent intent = new Intent(AccountInfoActivity.this, ChangeEmailActivity.class);
        startActivity(intent);
    }

    /**
     * Displays a toast with the provided message.
     * @param message The message to be displayed in the toast.
     */
    private void displayToast(String message) {
        Toast.makeText(AccountInfoActivity.this, message, Toast.LENGTH_SHORT).show();
    }

    /**
     * Initiates a request to delete the user's account with the specified URL.
     * @param deleteUrl The URL endpoint for account deletion.
     */
    private void deleteAccount(String deleteUrl) {
        StringRequest deleteRequest = new StringRequest(Request.Method.DELETE, deleteUrl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (response.equals("{\"message\":\"success\"}")) {
                            displayToast("Account successfully deleted");
                            clearUserPreferences();
                            Intent intent = new Intent(AccountInfoActivity.this, LoginActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            displayToast("Failed to delete account");
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error.getMessage() != null) {
                    Log.e("AccountDeletion", "Error: " + error.getMessage());
                    displayToast("An error occurred: " + error.getMessage());
                } else {
                    Log.e("AccountDeletion", "Unknown Error Occurred");
                    displayToast("An unknown error occurred");
                }
            }
        });

        Volley.newRequestQueue(AccountInfoActivity.this).add(deleteRequest);
    }

    /**
     * Clears all user preferences stored in SharedPreferences upon account deletion
     */
    private void clearUserPreferences() {
        SharedPreferences preferences = getSharedPreferences("UserPreferences", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear(); // This will clear all the data in UserPreferences
        editor.apply();
    }

}
