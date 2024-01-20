package MoviesTab;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.example.myapplication.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import TheatersTab.TheaterAdapter;
import TheatersTab.TheatersModal;

public class TheatersAvailable extends AppCompatActivity implements TheaterAdapter.OnClickListener {

    // variables for ui components
    private RecyclerView theatersRV;
    private Button backBtn;

    // variable for adapter -- class and array list
    private TheaterAdapter adapter;
    private ArrayList<TheatersModal> theatersModalArrayList;

    // Has the string form of the list of available theaters the movie plays at
    private String availableTheaters;

    // link to the theater
    private String theaterLink;

    /**
     * Sets up page for user viewing. This includes button clicking instructions to go back to
     * Movies Tab.
     *
     * Whichever movie the user clicked, the theaters that have that movie will appear on
     * the page, and they will be able to move back to the theaters tab by clicking the "Go back" button.
     * @param savedInstanceState If the activity is being re-initialized after previously
     *                           being shut down then this Bundle contains the data it most
     *                           recently supplied in onSaveInstanceState(Bundle)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_theaters_available);

        theatersRV = findViewById(R.id.RVTheaters);
        backBtn = findViewById(R.id.backBtn);

        availableTheaters = getIntent().getStringExtra("availableTheaters");

        theatersModalArrayList = new ArrayList<>();

        theatersRV.setVisibility(View.VISIBLE);

        try {
            Log.d("availableTheaters", availableTheaters);

            JSONArray theaters = new JSONArray(availableTheaters);
            for (int i = 0; i < theaters.length(); i++) {
                // get each json object
                JSONObject availableTheatersObj = theaters.getJSONObject(i);

                // get the key from the json object
                String theaterName = availableTheatersObj.getString("name");
                String theaterZip = availableTheatersObj.getString("zip");
                String theaterCity = availableTheatersObj.getString("city");
                String theaterAddress = availableTheatersObj.getString("address") + " " + theaterCity + ", " + theaterZip;
                String theaterPhone = availableTheatersObj.getString("phoneNumber");
                String theaterLink = availableTheatersObj.getString("url");
                theatersModalArrayList.add(new TheatersModal(theaterName, theaterAddress, theaterPhone, theaterLink));
                buildRecyclerView();
            }

        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        backBtn.setOnClickListener(new View.OnClickListener(){

            /**
             * Switches the user to the MoviesActivity class.
             * @param view This current View
             */
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(TheatersAvailable.this, MoviesActivity.class);
                startActivity(intent);
            }
        });

    }

    /**
     * Populates the Recycler View to show the movie(s) that are avaialable in the
     * searched theater.
     */
    private void buildRecyclerView() {
        // initialize adapter class
        adapter = new TheaterAdapter(theatersModalArrayList, TheatersAvailable.this, (TheaterAdapter.OnClickListener) this);

        // add layout manager to recycler view
        LinearLayoutManager manager = new LinearLayoutManager(this);
        theatersRV.setHasFixedSize(true);

        // set layout manager to the recycler view
        theatersRV.setLayoutManager(manager);

        // set adapter to the recycler view
        theatersRV.setAdapter(adapter);
    }

    @Override
    public void onClick(TheatersModal modal) {
        Uri uri = Uri.parse(modal.getTheaterlink()); // missing 'http://' will cause crashed
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(intent);
    }
}
