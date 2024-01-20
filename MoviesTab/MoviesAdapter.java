package MoviesTab;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;

import java.util.ArrayList;

import TheatersTab.TheaterAdapter;
import TheatersTab.TheatersModal;

public class MoviesAdapter extends RecyclerView.Adapter<MoviesAdapter.ViewHolder> {

    // variables for array list and context
    /**
     * Holds the collection of MovieModel objects.
     */
    private ArrayList<MoviesModel> moviesModelArrayList;
    /**
     * Holds the View that it is being called from.
     */
    private Context context;
    /***
     * Holds the OnClickListener object to determine what is being clicked.
     */
    private OnClickListener onClickListener;

    /**
     * Constructor for this MoviesAdapter.
     * @param moviesModelArrayList The Array List of MovieModel objects to be placed into the RecyclerView as a card
     * @param context The current View that the user is in.
     * @param onClickListener Determines what is the user clicks is a card from the RecyclerView.
     */
    public MoviesAdapter(ArrayList<MoviesModel> moviesModelArrayList, Context context, MoviesAdapter.OnClickListener onClickListener) {
        this.moviesModelArrayList = moviesModelArrayList;
        this.context = context;
        this.onClickListener = onClickListener;
    }

    /**
     * Creates a place for movie cards to be displayed.
     * @param parent   The ViewGroup into which the new View will be added after it is bound to
     *                 an adapter position.
     * @param viewType The view type of the new View.
     * @return new ViewHolder(view)
     */
    @NonNull
    @Override
    public MoviesAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.movies_rv_item, parent, false);
        return new ViewHolder(view);
    }

    /**
     * Creates a card for a MovieModel object that can be clicked.
     * @param holder   The ViewHolder which should be updated to represent the contents of the
     *                 item at the given position in the data set.
     * @param position The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(@NonNull MoviesAdapter.ViewHolder holder, int position) {
        // setting up data to see data in recycler view
        MoviesModel model = moviesModelArrayList.get(position);
        holder.movieTitleTV.setText(model.getMovieTitle());
        holder.movieGenreTV.setText(model.getMovieGenre());
        holder.movieRatingTV.setText(model.getMovieRating());
        holder.movieRuntimeTV.setText(model.getMovieRuntime());
        holder.movieTimesTV.setText(model.getMovieTimesStr());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            /**
             * Sets the OnClickListener to the movie object card (from MovieModel)
             * @param view This current View.
             */
            @Override
            public void onClick(View view) {
                if (onClickListener != null) {
                    onClickListener.onClick(model);
                }
            }
        });
    }

    /**
     * Gets the size of the moviesModelArrayList
     * @return size of movieModelArrayList
     */
    @Override
    public int getItemCount() {
        // return the size of array list
        return moviesModelArrayList.size();
    }

    /**
     * Sets the onClickListener to the currently clicked.
     * @param onClickListener What the user clicked on
     */
    public void setOnClickListener(MoviesAdapter.OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }

    /**
     * Allows for the user to click on a card in Recycler View.
     */
    public interface OnClickListener {
        void onClick(MoviesModel model);
    }

    /**
     * A customized class of ReycylerView.ViewHolder that shows the cards in the Recycler View.
     */
    public class ViewHolder extends RecyclerView.ViewHolder {
        // variables for view
        private TextView movieTitleTV, movieRatingTV, movieGenreTV, movieRuntimeTV, movieTimesTV;

        /**
         * A single item that is added to a card.
         * @param itemView the View that has the Recycler View is in
         */
        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            // putting views with their ids
            movieTitleTV = itemView.findViewById(R.id.MovieTitle);
            movieRatingTV = itemView.findViewById(R.id.MovieRating);
            movieGenreTV = itemView.findViewById(R.id.MovieGenre);
            movieRuntimeTV = itemView.findViewById(R.id.MovieRuntime);
            movieTimesTV = itemView.findViewById(R.id.MovieTimes);
        }
    }
}
