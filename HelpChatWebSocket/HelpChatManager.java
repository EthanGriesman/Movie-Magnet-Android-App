package HelpChatWebSocket;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;

public class HelpChatManager {

    private static HelpChatManager instance;
    private MyWebSocketClient webSocketClient;
    private HelpChatListener webSocketListener;
    private Context context;

    private HelpChatManager() {}

    public static synchronized HelpChatManager getInstance() {
        if (instance == null) {
            instance = new HelpChatManager();
        }
        return instance;
    }

    public void setContext(Context context) {
        this.context = context.getApplicationContext();
    }

    public void setWebSocketListener(HelpChatListener listener) {
        this.webSocketListener = listener;
    }

    public void removeWebSocketListener() {
        this.webSocketListener = null;
    }

    public void connectWebSocket(String username) {
        try {
            // Generate a WebSocket URL specific to the user for DM
            String serverUrl = "ws://coms-309-040.class.las.iastate.edu:8080/chat/admin/" + username;

            URI serverUri = URI.create(serverUrl);
            webSocketClient = new MyWebSocketClient(serverUri);
            webSocketClient.connect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendMessage(String username, String message) {
        if (webSocketClient != null && webSocketClient.isOpen()) {
            // Send a direct message with a format that includes the username
            webSocketClient.send("@" + username + " " + message);
        }
    }

    public void disconnectWebSocket() {
        if (webSocketClient != null && webSocketClient.isOpen()) {
            webSocketClient.shouldReconnect = false; // Ensure that the WebSocket doesn't try to reconnect after disconnecting
            webSocketClient.close();
        }
    }

    private class MyWebSocketClient extends WebSocketClient {
        private static final int RECONNECT_DELAY = 5000; // Reconnect delay in milliseconds

        public boolean shouldReconnect = true; // Made this public for external access

        private MyWebSocketClient(URI serverUri) {
            super(serverUri);
        }

        @Override
        public void onOpen(ServerHandshake handshakedata) {
            Log.d("WebSocket", "Connected");
            if (webSocketListener != null) {
                webSocketListener.onWebSocketOpen(handshakedata);
            }
        }

        @Override
        public void onMessage(String message) {
            Log.d("WebSocket", "Received message: " + message);
            if (webSocketListener != null) {
                webSocketListener.onWebSocketMessage(message);
            }
        }

        @Override
        public void onClose(int code, String reason, boolean remote) {
            Log.d("WebSocket", "Closed with code: " + code + ", reason: " + reason + ", remote: " + remote);

            if (shouldReconnect) {
                if (webSocketClient != null) {
                    // Reconnect immediately
                    URI serverUri = webSocketClient.getURI();
                    webSocketClient = new MyWebSocketClient(serverUri);
                    webSocketClient.connect();
                }
            }

            if (webSocketListener != null) {
                webSocketListener.onWebSocketClose(code, reason, remote);
            }

            // Show a Toast message when the WebSocket is closed
            if (context != null) {
                Handler mainHandler = new Handler(Looper.getMainLooper());
                mainHandler.post(() -> Toast.makeText(context, "Leaving Help Chat!", Toast.LENGTH_SHORT).show());
            }
        }

        @Override
        public void onError(Exception ex) {
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(() -> {
                Log.d("WebSocket", "Error");
                if (webSocketListener != null) {
                    webSocketListener.onWebSocketError(ex);
                }
            });
        }
    }
}
