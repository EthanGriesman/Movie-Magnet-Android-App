package HelpChatWebSocket;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.myapplication.R;

import SettingsTab.SettingsActivity;

/**
 * This activity represents the user interface for a help chat system where users can send and receive messages
 * via a web socket connection. Only able to DM admin users as opposed to chat within SocialTab
 */
public class HelpChatActivity extends AppCompatActivity implements HelpChatListener {
    private String username;
    private Button sendBtn, disconnectBtn;
    private EditText msgEtx;
    private TextView msgTv;

    /**
     * Called when the activity is starting. This is where most initialization should go.
     *
     * @param savedInstanceState If the activity is being re-initialized after previously being shut down
     *                           then this Bundle contains the data it most recently supplied in onSaveInstanceState(Bundle).
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help_chat);

        sendBtn = findViewById(R.id.bt2);
        disconnectBtn = findViewById(R.id.disconnectBtn);
        msgEtx = findViewById(R.id.et2);
        msgTv = findViewById(R.id.tx1);

        username = getIntent().getStringExtra("username");
        Log.d("HelpChatActivity", "Received username: " + username);

        if (!TextUtils.isEmpty(username)) {
            LayoutInflater inflater = getLayoutInflater();
            View customToastLayout = inflater.inflate(R.layout.custom_toast, findViewById(R.id.custom_toast_message));

            TextView toastMessage = customToastLayout.findViewById(R.id.custom_toast_message);
            String welcomeMessage = "Welcome to the Help Chat, " + username;
            toastMessage.setText(welcomeMessage);

            Toast customToast = new Toast(this);
            customToast.setView(customToastLayout);
            customToast.setDuration(Toast.LENGTH_SHORT);
            customToast.show();

            HelpChatManager.getInstance().connectWebSocket(username);
            HelpChatManager.getInstance().setWebSocketListener(this);
            checkIfUserIsMuted();
            
        } else {
            Log.e("HelpChatActivity", "Username is empty or not found in intent extras.");
        }

        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                    try {
                        String message = msgEtx.getText().toString();
                        Log.d("HelpChatActivity", "Sending message: " + message);
                        // Check if it's a direct message
                        if (message.startsWith("@")) {
                            // Extract recipient's username and message content
                            String[] parts = message.split(" ", 2);
                            if (parts.length == 2) {
                                String recipientUsername = parts[0].substring(1); // Remove "@" symbol
                                String dmMessage = parts[1];
                                // Send the DM to the server
                                HelpChatManager.getInstance().sendMessage("@" + recipientUsername + " ", dmMessage);
                            } else {
                                // Handle invalid DM format
                                Log.e("HelpChatActivity", "Invalid DM format: " + message);
                            }
                        } else {
                            // Send a regular message
                            HelpChatManager.getInstance().sendMessage(username, message);
                        }
                    } catch (Exception e) {
                        Log.e("HelpChatActivity", "Exception while sending message: " + e.getMessage());
                    }
            }
        });

        disconnectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    HelpChatManager.getInstance().disconnectWebSocket();
                    startActivity(new Intent(HelpChatActivity.this, SettingsActivity.class));
                } catch (Exception e) {
                    Log.e("HelpChatActivity", "Exception while disconnecting: " + e.getMessage());
                }
            }
        });
    }

    private void checkIfUserIsMuted() {
        String url = "http://coms-309-040.class.las.iastate.edu:8080/users";

        if (username == null || username.isEmpty()) {
            // Fetch username from shared preferences if it's not provided
            SharedPreferences sharedPref = getSharedPreferences("AppSettings", MODE_PRIVATE);
            username = sharedPref.getString("currentUsername", "");
        }

        Log.d("HelpChatActivity", "Checking mute status for user: " + username);

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                response -> {
                    try {
                        Log.d("HelpChatActivity", "Received response: " + response);
                        JSONArray users = new JSONArray(response);

                        for (int i = 0; i < users.length(); i++) {
                            JSONObject userObject = users.getJSONObject(i);
                            String userEmail = userObject.getString("name");

                            if (userEmail.equals(username)) {
                                boolean isMuted = userObject.getBoolean("mute");
                                Log.d("HelpChatActivity", "User " + username + " is muted: " + isMuted);

                                updateMuteStatus(username, isMuted);

                                if (isMuted) {
                                    runOnUiThread(() -> {
                                        sendBtn.setEnabled(false);
                                        showCustomToast("You are muted and cannot send messages.");
                                    });
                                }
                                break;
                            }
                        }
                    } catch (JSONException e) {
                        Log.e("HelpChatActivity", "JSON Parsing error: " + e.getMessage());
                    }
                },
                error -> Log.e("HelpChatActivity", "Error fetching user data: " + error.toString()));

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(stringRequest);
    }

    private void updateMuteStatus(String username, boolean isMuted) {
        SharedPreferences sharedPref = getSharedPreferences("AppSettings", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean(username + "_isMuted", isMuted);
        editor.apply();
    }


    private void showCustomToast(String message) {
        Log.d("MuteUserActivity", "Showing custom toast: " + message);
        View layout = getLayoutInflater().inflate(R.layout.custom_login_toast3, findViewById(R.id.custom_login_layout));
        TextView text = layout.findViewById(R.id.toast_text);
        text.setText(message);
        Toast toast = new Toast(getApplicationContext());
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(layout);
        toast.show();
    }


    /**
     * Called when a message is received from the web socket. Updates the chat text view to display the message.
     *
     * @param message The message received from the web socket.
     */
    @Override
    public void onWebSocketMessage(String message) {
        runOnUiThread(() -> {
            String s = msgTv.getText().toString();
            msgTv.setText(s + "\n" + message);
        });
    }

    /**
     * Called when the web socket is closed. Updates the UI to reflect the connection has been closed.
     *
     * @param code    The closure code.
     * @param reason  The reason for the closure.
     * @param remote  Indicates if the closure was initiated by the remote host.
     */
    @Override
    public void onWebSocketClose(int code, String reason, boolean remote) {
        String closedBy = remote ? "server" : "local";
        runOnUiThread(() -> {
            String s = msgTv.getText().toString();
            msgTv.setText(s + "---\nconnection closed by " + closedBy + "\nreason: " + reason);
        });
    }

    /**
     * Called when the web socket connection is established.
     *
     * @param handshakedata The handshake data from the server.
     */
    @Override
    public void onWebSocketOpen(ServerHandshake handshakedata) {
    }

    /**
     * Called when there is an error with the web socket.
     *
     * @param ex The exception that occurred.
     */
    @Override
    public void onWebSocketError(Exception ex) {
        Log.e("HelpChatActivity", "WebSocket error: " + ex.getMessage());
        // Add additional handling if needed
    }
}
