package SocialTab;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.myapplication.R;

import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

import MoviesTab.MoviesActivity;
import MoviesTab.MoviesModel;

public class GroupChat extends AppCompatActivity implements WebSocketListener{
    // variables for ui components
    private Button backBtn, sendBtn;
    private EditText msgEtx;
    private TextView msgTv;

    // friend url
    private String url;

    // user info
    private String username;
    private String ageGroup;
    private String email;

    // message
    private String message;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat);

        /* initialize UI elements */
        sendBtn = findViewById(R.id.bt2);
        msgEtx = findViewById(R.id.et2);
        msgTv = findViewById(R.id.tx1);
        backBtn = findViewById(R.id.backBtn); // Initialize the connect button

        SharedPreferences sh = getSharedPreferences("UserPreferences", MODE_PRIVATE);
        username = sh.getString("username", "");
        ageGroup = sh.getString("ageGroup", "");
        email = sh.getString("email", "");

        url = "http://coms-309-040.class.las.iastate.edu:8080/users/friends/" + email;


        // Establish WebSocket connection and set listener
        WebSocketManager.getInstance().connectWebSocket(username, ageGroup);
        WebSocketManager.getInstance().setWebSocketListener(GroupChat.this);

        /* send button listener */
        sendBtn.setOnClickListener(v -> {
            message = msgEtx.getText().toString();

            try {
                if (message.startsWith("@")) {
                    // Extract recipient's username and message content
                    String[] parts = message.split(" ", 2);
                    if (parts.length == 2) {
                        String recipientUsername = parts[0].substring(1); // Remove "@" symbol
                        String dmMessage = parts[1];
                        // new message with recipient added for direct message
                        message = "@" + recipientUsername + " " + dmMessage;

                        // add them as a friend --
                        // create a requestBody
                        try {
                            // create json body object
                            JSONObject requestBody = new JSONObject();
                            requestBody.put("recipientUsername", recipientUsername);
                            Log.d("requestBody = ", requestBody.toString());
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
                // send message
                WebSocketManager.getInstance().sendMessage(message);

            } catch (Exception e) {
                Log.d("ExceptionSendMessage:", e.getMessage().toString());
            }
        });

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(GroupChat.this, SocialActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onWebSocketOpen(ServerHandshake handshakedata) {
    }

    @Override
    public void onWebSocketMessage(String message) {
        /**
         * In Android, all UI-related operations must be performed on the main UI thread
         * to ensure smooth and responsive user interfaces. The 'runOnUiThread' method
         * is used to post a runnable to the UI thread's message queue, allowing UI updates
         * to occur safely from a background or non-UI thread.
         */
        runOnUiThread(() -> {
            String s = msgTv.getText().toString();
            msgTv.setText(s + "\n"+message);
        });
    }

    @Override
    public void onWebSocketClose(int code, String reason, boolean remote) {
        String closedBy = remote ? "server" : "local";
        runOnUiThread(() -> {
            String s = msgTv.getText().toString();
            msgTv.setText(s + "---\nconnection closed by " + closedBy + "\nreason: " + reason);
        });
    }

    @Override
    public void onWebSocketError(Exception ex) {

    }
}