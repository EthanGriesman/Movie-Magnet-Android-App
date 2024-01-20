package MoviesTab;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.myapplication.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import android.util.Log;
import android.content.SharedPreferences;

public class ReviewActivity extends AppCompatActivity {

    private static final String TAG = "ReviewActivity";
    private EditText editTextReview, editTextMovieName;

    private String selectedMovieName = "";
    private Spinner spinnerMovieTitles;
    private ArrayList<String> movieTitlesList = new ArrayList<>();
    private ArrayList<String> movieTitles = new ArrayList<>();
    private ArrayList<Integer> movieIds = new ArrayList<>();
    private ArrayAdapter<String> movieTitlesAdapter;
    private Button buttonSendReview;
    private String userAgeGroup; // Variable to store user's age group
    private int movieId;

    private int userId;
    private String movieName, movieRating; // Additional movie details

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review);

        editTextReview = findViewById(R.id.editTextReview);
        editTextMovieName = findViewById(R.id.editTextMovieName); // EditText for movie name
        buttonSendReview = findViewById(R.id.buttonSendReview);
        Button buttonSelectMovie = findViewById(R.id.buttonSelectMovie);

        buttonSelectMovie.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String selectedMovieName = editTextMovieName.getText().toString();
                if (!selectedMovieName.isEmpty()) {
                    Log.d(TAG, "Selected movie: " + selectedMovieName);
                    showCustomToast("Movie selected!");
                } else {
                    Toast.makeText(ReviewActivity.this, "Please enter a movie name", Toast.LENGTH_SHORT).show();
                }
            }
        });

        buttonSendReview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String reviewText = editTextReview.getText().toString();
                String selectedMovieName = editTextMovieName.getText().toString();

                if (reviewText.isEmpty()) {
                    showCustomToast("Please enter a review");
                    return; // Stop the function if review text is empty
                }

                if (selectedMovieName.isEmpty()) {
                    showCustomToast("Please select the movie you wish to review");
                    return; // Stop the function if no movie is selected
                }

                sendReviewToServer(); // Send the review if both conditions are met
            }
        });

    }

    private void showCustomToast(String message) {
        View layout = getLayoutInflater().inflate(R.layout.custom_login_toast4, findViewById(R.id.custom_login_layout));
        TextView text = layout.findViewById(R.id.toast_text);
        text.setText(message);
        Toast toast = new Toast(getApplicationContext());
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(layout);
        toast.show();
    }


    private void sendReviewToServer() {
        String reviewText = editTextReview.getText().toString();
        String selectedMovieName = editTextMovieName.getText().toString();
        String url = "http://coms-309-040.class.las.iastate.edu:8080/reviews";
        Log.d(TAG, "Review text: " + reviewText);

        RequestQueue queue = Volley.newRequestQueue(this);

        JSONObject jsonReview = new JSONObject();
        try {
            jsonReview.put("movie", selectedMovieName);
            jsonReview.put("comment", reviewText);
            Log.d(TAG, "JSON for review being sent: " + jsonReview);
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e(TAG, "JSON exception: " + e.getMessage());
            Toast.makeText(this, "Error creating JSON", Toast.LENGTH_SHORT).show();
            return;
        }

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                response -> {
                    Log.d(TAG, "Server Response: " + response);
                    showCustomToast("Review sent. Thank you!");
                    Intent intent = new Intent(ReviewActivity.this, MoviesActivity.class);
                    startActivity(intent);
                    finish();
                },
                error -> {
                    Log.e(TAG, "Volley Error: " + error.toString());
                    if (error.networkResponse != null) {
                        Log.e(TAG, "Error Response Code: " + error.networkResponse.statusCode);
                        Toast.makeText(ReviewActivity.this, "Error: " + error.toString() + " Status Code: " + error.networkResponse.statusCode, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(ReviewActivity.this, "Network error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }) {
            @Override
            public byte[] getBody() {
                return jsonReview.toString().getBytes(StandardCharsets.UTF_8);
            }

            @Override
            public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }
        };

        queue.add(stringRequest);
    }
}
