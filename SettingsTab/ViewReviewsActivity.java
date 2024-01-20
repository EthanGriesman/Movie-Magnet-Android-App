package SettingsTab;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import com.example.myapplication.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ViewReviewsActivity extends AppCompatActivity {

    private EditText editTextMovieName;
    private static final String TAG = "ViewReviewsActivity";
    private HashMap<Integer, String> reviewsMap = new HashMap<>();
    private Button buttonReviewsByMovie;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_reviews);

        editTextMovieName = findViewById(R.id.editTextMovieName);
        buttonReviewsByMovie = findViewById(R.id.buttonReviewsByMovie);

        buttonReviewsByMovie.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String movieName = editTextMovieName.getText().toString();
                if (!movieName.isEmpty()) {
                    fetchReviewsForMovie(movieName);
                } else {
                    Toast.makeText(ViewReviewsActivity.this, "Please enter a movie name", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void fetchReviewsForMovie(String movieName) {
        String url = "http://coms-309-040.class.las.iastate.edu:8080/reviews/movie/" + movieName;
        Log.d(TAG, "Fetching reviews for movie: " + movieName);

        RequestQueue queue = Volley.newRequestQueue(this);

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                response -> {
                    Log.d(TAG, "Response received: " + response);
                    try {
                        JSONArray reviewsArray = new JSONArray(response);
                        reviewsMap.clear();

                        for (int i = 0; i < reviewsArray.length(); i++) {
                            JSONObject reviewObject = reviewsArray.getJSONObject(i);
                            int id = reviewObject.getInt("id");
                            String comment = reviewObject.optString("comment");
                            reviewsMap.put(id, comment);
                        }

                        showCommentsDialog(new ArrayList<>(reviewsMap.values()));
                    } catch (JSONException e) {
                        Log.e(TAG, "JSON parsing error: ", e);
                        Toast.makeText(this, "Error parsing JSON", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Log.e(TAG, "Error fetching reviews: " + error.toString());
                    Toast.makeText(this, "Error fetching data", Toast.LENGTH_SHORT).show();
                });

        queue.add(stringRequest);
    }

    private void showCommentsDialog(ArrayList<String> comments) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, comments);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Movie Reviews")
                .setAdapter(adapter, (dialog, which) -> {
                    String selectedComment = adapter.getItem(which);
                    // Find the review ID by the comment
                    int reviewId = findReviewIdByComment(selectedComment);
                    showDeleteConfirmationDialog(Integer.parseInt(String.valueOf(reviewId)));
                })
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private int findReviewIdByComment(String comment) {
        for (Map.Entry<Integer, String> entry : reviewsMap.entrySet()) {
            if (entry.getValue().equals(comment)) {
                return entry.getKey();
            }
        }
        return -1; // Return -1 or throw an exception if not found
    }



    private void showDeleteConfirmationDialog(int reviewId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Confirm Delete")
                .setMessage("Do you want to delete this review?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    // Assuming 'admin@example.com' is the admin email
                    deleteReview(reviewId, "admin@moviemagnet.com");
                })
                .setNegativeButton("No", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void deleteReview(int reviewId, String email) {
        String url = "http://coms-309-040.class.las.iastate.edu:8080/reviews/" + reviewId + "/" + email;

        RequestQueue queue = Volley.newRequestQueue(this);
        StringRequest stringRequest = new StringRequest(Request.Method.DELETE, url,
                response -> {
                    try {
                        JSONObject jsonResponse = new JSONObject(response);
                        String message = jsonResponse.optString("message");
                        if (response.equals("{\"message\":\"success\"}")) {
                            Log.d(TAG, "Review deleted successfully");
                            showCustomToast("Review deleted!");
                        } else {
                            Log.d(TAG, "Response message: " + message);
                            Toast.makeText(this, "Response message: " + message, Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        Log.e(TAG, "JSON parsing error: ", e);
                        Toast.makeText(this, "Error parsing JSON", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Log.e(TAG, "Error deleting review: " + error.toString());
                    Toast.makeText(this, "Error deleting review", Toast.LENGTH_SHORT).show();
                });

        queue.add(stringRequest);
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


}


