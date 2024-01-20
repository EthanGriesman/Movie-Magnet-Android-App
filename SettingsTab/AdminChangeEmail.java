package SettingsTab;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.myapplication.LoginActivity;
import com.example.myapplication.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class AdminChangeEmail extends AppCompatActivity {

    private Spinner spinnerEmails;
    private EditText newEmailEditText, userEmailEditText;
    private ArrayAdapter<String> adapter;
    private ArrayList<String> userList = new ArrayList<>();
    private String selectedEmail;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_put_user_email);

        spinnerEmails = findViewById(R.id.spinnerEmails);  // Make sure this ID exists in your layout file
        Button buttonSelectUsers = findViewById(R.id.buttonSelectEmails);
        newEmailEditText = findViewById(R.id.newEmailEditText);
        Button changeEmailButton = findViewById(R.id.changeEmailButton);

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, userList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerEmails.setAdapter(adapter);

        SharedPreferences preferences = getSharedPreferences("UserPreferences", MODE_PRIVATE);

        boolean isAdmin = preferences.getBoolean("isAdmin", false);

        if (isAdmin) {
            userEmailEditText.setVisibility(View.VISIBLE);
        }

        buttonSelectUsers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fetchEmails();
                spinnerEmails.post(new Runnable() {
                    @Override
                    public void run() {
                        if (adapter.getCount() > 0) {
                            showEmailSelectionDialog();
                        }
                    }
                });
            }
        });

        changeEmailButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newEmail = newEmailEditText.getText().toString().trim();
                Log.d("ChangeEmailActivity", "New Email Entered: " + newEmail);
                if (newEmail.isEmpty()) {
                    displayToast("New email cannot be empty");
                } else {
                    updateEmail(selectedEmail, newEmail);
                }
            }
        });
    }

    private void showEmailSelectionDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select an Email");
        builder.setAdapter(adapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                selectedEmail = adapter.getItem(which);
                showCustomToast("Email selected: " + selectedEmail);
            }
        });
        builder.setNegativeButton("Cancel", null);
        AlertDialog dialog = builder.create();
        dialog.show();
        Window window = dialog.getWindow();
        if (window != null) {
            WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
            layoutParams.copyFrom(dialog.getWindow().getAttributes());
            layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
            layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
            window.setAttributes(layoutParams);
        }
    }

    private void showCustomToast(String message) {
        // Create a custom view for the toast
        View layout = getLayoutInflater().inflate(R.layout.custom_login_toast2, findViewById(R.id.custom_login_layout));

        // Set the text message
        TextView text = layout.findViewById(R.id.toast_text);
        text.setText(message);

        // Create and display the custom toast
        Toast toast = new Toast(getApplicationContext());
        toast.setDuration(Toast.LENGTH_SHORT); // Adjust the duration as needed
        toast.setView(layout);
        toast.show();
    }

    private void fetchEmails() {
        Log.d("AdminChangeEmail", "Fetching emails from server");
        String url = "http://coms-309-040.class.las.iastate.edu:8080/users";
        RequestQueue queue = Volley.newRequestQueue(this);
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONArray jsonArray = new JSONArray(response);
                            userList.clear();
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject userObject = jsonArray.getJSONObject(i);
                                String userEmail = userObject.getString("email");
                                userList.add(userEmail);
                            }
                            adapter.notifyDataSetChanged();
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(AdminChangeEmail.this, "Error parsing user data", Toast.LENGTH_SHORT).show();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(AdminChangeEmail.this, "Error fetching users: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        queue.add(stringRequest);
    }

    /**
     * Displays a Toast message on the screen.
     * @param message The message to be displayed.
     */
    private void displayToast(String message) {
        Toast.makeText(AdminChangeEmail.this, message, Toast.LENGTH_SHORT).show();
    }

    /**
     * Sends a request to update the username of the user identified by their email.
     * It sends a PUT request to the server and handles the response or error.
     * @param selectedEmail The email of the user whose username is to be updated.
     * @param newEmail The new email to be set for the user.
     */
    private void updateEmail(String selectedEmail, String newEmail) {
        String updateUrl = "http://coms-309-040.class.las.iastate.edu:8080/users/updateEmail/" + selectedEmail;
        String requestBody = newEmail;
        StringRequest updateRequest = new StringRequest(Request.Method.PUT, updateUrl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if ("{\"message\":\"success\"}".equals(response)) {
                            displayToast("Email successfully updated");
                            Intent intent = new Intent(AdminChangeEmail.this, LoginActivity.class);
                            startActivity(intent);
                        } else {
                            displayToast("Failed to update user's email");
                            Log.e("ChangeUserActivity", "Server Response: " + response);
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                displayToast("Failed to update username. Please try again.");
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

        Volley.newRequestQueue(AdminChangeEmail.this).add(updateRequest);
    }
}
