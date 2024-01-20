package SettingsTab;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.TooltipCompat;

import com.example.myapplication.HomeActivity;
import com.example.myapplication.LoginActivity;
import com.example.myapplication.R;

import HelpChatWebSocket.HelpChatActivity;

/**
 * An Activity that provides various settings options to the user,
 * such as account information, submitting tickets, and logout functionality.
 * Admin users can also access admin settings from here.
 */
public class SettingsActivity extends AppCompatActivity {

    /**
     * Initializes the Activity, setting the content view and configuring buttons
     * and listeners for user interaction.
     *
     * @param savedInstanceState A Bundle containing the activity's previously saved state.
     *                           If the activity has never existed before, the value of the Bundle is null.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        // Initialize the logout button
        Button logoutButton = findViewById(R.id.logoutButton);
        // Initialize the account information button
        Button accountInfoButton = findViewById(R.id.accountInfoButton);
        // Initialize the submit ticket button for support requests
        Button submitTicketButton = findViewById(R.id.submitTicketButton);
        // Initialize the admin settings button, visible only for admin users
        Button adminButton = findViewById(R.id.adminButton);
        ImageView backArrowImage = findViewById(R.id.imageView);
        String username = getIntent().getStringExtra("username");

        // Setting up a click listener for the back arrow
        backArrowImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Redirect user to Home page
                Intent intent = new Intent(SettingsActivity.this, HomeActivity.class);
                startActivity(intent);
            }
        });

        // Setting up a click listener for the ADMIN button (only accessible to admin users)
        adminButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = getUsernameFromSharedPreferences();
                if ("Admin".equals(username)) {
                    Log.d("SettingsActivity", "Admin button clicked by Admin user.");
                    Intent intent = new Intent(SettingsActivity.this, AdminActivity.class);
                    startActivity(intent);
                } else {
                    Log.e("SettingsActivity", "Non-admin user tried to access Admin page.");
                    showCustomToast2("Page only accessible to admin users!");
                }
            }
        });

        // Setting up a click listener for the LOG OUT button
        // Setting up a click listener for the LOG OUT button
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(SettingsActivity.this)
                        .setTitle("Log Out")
                        .setMessage("Are you sure you want to leave Movie Magnet?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // User clicked "Yes"
                                showCustomToast("Logging out. Goodbye!");
                                Intent intent = new Intent(SettingsActivity.this, LoginActivity.class);
                                startActivity(intent);
                            }
                        })
                        .setNegativeButton("No", null)
                        .show();
            }
        });


        // Setting up a click listener for the ACCOUNT INFO button
        accountInfoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SettingsActivity.this, AccountInfoActivity.class);
                startActivity(intent);
            }
        });

        // Setting up a click listener for the HELP CHAT button
        submitTicketButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = getUsernameFromSharedPreferences();
                Log.d("SettingsActivity", "Username from SharedPreferences: " + username);
                Intent intent = new Intent(SettingsActivity.this, HelpChatActivity.class);
                intent.putExtra("username", username);
                Log.d("SettingsActivity", "Starting HelpChatActivity with username: " + username);
                startActivity(intent);
            }
        });

        TooltipCompat.setTooltipText(submitTicketButton, "Chat with an admin!");
    }

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

    private void showCustomToast2(String message) {
        // Create a custom view for the toast
        View layout = getLayoutInflater().inflate(R.layout.custom_login_toast2, findViewById(R.id.custom_login_layout));
        TextView text = layout.findViewById(R.id.toast_text);
        text.setText(message);
        Toast toast = new Toast(getApplicationContext());
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(layout);
        toast.show();
    }



    /**
     * Retrieves the stored username from SharedPreferences.
     *
     * @return A string value representing the username, or an empty string if not found.
     */
    private String getUsernameFromSharedPreferences() {
        SharedPreferences preferences = getSharedPreferences("UserPreferences", MODE_PRIVATE);
        return preferences.getString("username", "");
    }
}
