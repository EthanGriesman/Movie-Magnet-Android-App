package SettingsTab;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONArray;
import com.example.myapplication.R;

/**
 * AdminActivity provides administrative control over users. It allows viewing all user accounts,
 * changing user details, and muting users.
 *
 * PUT requests to change user and email
 * GET request to view all users (under development)
 */
public class AdminActivity extends AppCompatActivity {

    // Member variables for request queue and buttons
    private RequestQueue requestQueue;
    private Button btnMuteUsers, changeUserBtn, changeEmailBtn, btnViewReviews;

    /**
     * Initializes the activity. This method sets up the request queue and the UI elements
     * to interact with the admin functionality.
     *
     * @param savedInstanceState If the activity is being re-initialized after previously being
     *                           shut down then this Bundle contains the most recent data,
     *                           otherwise it is null.
     */
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        requestQueue = Volley.newRequestQueue(this);

        btnMuteUsers = findViewById(R.id.btnMuteUsers);
        btnViewReviews = findViewById(R.id.btnViewReviews);
        changeUserBtn = findViewById(R.id.changeUserBtn);
        changeEmailBtn = findViewById(R.id.changeEmailBtn);

        // Listener for 'Change User Details' button
        btnMuteUsers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Redirect to PutUserAdminActivity
                Intent intent = new Intent(AdminActivity.this, MuteUserActivity.class);
                startActivity(intent);
            }
        });

        btnViewReviews.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Redirect to PutUserAdminActivity
                Intent intent = new Intent(AdminActivity.this, ViewReviewsActivity.class);
                startActivity(intent);
            }
        });

        // Listener for 'Change User Details' button
        changeUserBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Redirect to PutUserAdminActivity
                Intent intent = new Intent(AdminActivity.this, AdminChangeUser.class);
                startActivity(intent);
            }
        });

        // Listener for 'Change Email' button
        changeEmailBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Redirect to PutEmailAdminActivity
                Intent intent = new Intent(AdminActivity.this, AdminChangeEmail.class);
                startActivity(intent);
            }
        });
    }
}
