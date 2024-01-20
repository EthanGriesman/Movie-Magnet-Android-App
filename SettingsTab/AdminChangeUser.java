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

public class AdminChangeUser extends AppCompatActivity {

    private Spinner spinnerUsers;
    private EditText newUsernameEditText, userEmailEditText;
    private Button changeUsernameButton, buttonSelectUsers;
    private ArrayAdapter<String> adapter;
    private ArrayList<String> userList = new ArrayList<>();
    private String selectedEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_put_user_admin);

        spinnerUsers = findViewById(R.id.spinnerUsers);
        buttonSelectUsers = findViewById(R.id.buttonSelectUsers);
        newUsernameEditText = findViewById(R.id.newUsernameEditText);
        changeUsernameButton = findViewById(R.id.changeUsernameButton);

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, userList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerUsers.setAdapter(adapter);

        SharedPreferences preferences = getSharedPreferences("UserPreferences", MODE_PRIVATE);
        boolean isAdmin = preferences.getBoolean("isAdmin", false);

        if (isAdmin) {
            userEmailEditText.setVisibility(View.VISIBLE);
        }

        buttonSelectUsers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fetchUsers();
                spinnerUsers.post(new Runnable() {
                    @Override
                    public void run() {
                        if (adapter.getCount() > 0) {
                            showUserSelectionDialog();
                        }
                    }
                });
            }
        });

        changeUsernameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newUsername = newUsernameEditText.getText().toString();
                if (newUsername.isEmpty()) {
                    displayToast("New username cannot be empty");
                } else {
                    updateUsername(selectedEmail, newUsername);
                }
            }
        });
    }

    private void showUserSelectionDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select a User");
        builder.setAdapter(adapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                selectedEmail = adapter.getItem(which);
                Toast.makeText(AdminChangeUser.this, "User selected: " + selectedEmail, Toast.LENGTH_SHORT).show();
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

    private void fetchUsers() {
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
                            Toast.makeText(AdminChangeUser.this, "Error parsing user data", Toast.LENGTH_SHORT).show();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(AdminChangeUser.this, "Error fetching users: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        queue.add(stringRequest);
    }

    private void displayToast(String message) {
        Toast.makeText(AdminChangeUser.this, message, Toast.LENGTH_SHORT).show();
    }

    private void updateUsername(String selectedEmail, String newUsername) {
        if (selectedEmail == null || selectedEmail.isEmpty()) {
            displayToast("Please select a user first");
            return;
        }
        // Assuming selectedUser is the email of the user to be updated
        String updateUrl = "http://coms-309-040.class.las.iastate.edu:8080/users/updateUsername/" + selectedEmail;
        String requestBody = newUsername;
        StringRequest updateRequest = new StringRequest(Request.Method.PUT, updateUrl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if ("{\"message\":\"success\"}".equals(response)) {
                            displayToast("Username successfully updated");
                            Intent intent = new Intent(AdminChangeUser.this, LoginActivity.class);
                            startActivity(intent);
                        } else {
                            displayToast("Failed to update username");
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

        Volley.newRequestQueue(AdminChangeUser.this).add(updateRequest);
    }
}
