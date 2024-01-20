package TheatersTab;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;

import java.util.ArrayList;

public class TheaterAdapter extends RecyclerView.Adapter<TheaterAdapter.ViewHolder> {


    // a variable for array list and context
    private ArrayList<TheatersModal> theatersModalArrayList;
    private OnClickListener onClickListener;
    private Context context;

    /**
     * Constructor for this TheaterAdapter.
     * @param theatersModalArrayList The Array List of TheaterModal objects to be placed into the RecyclerView as a card.
     * @param context The current View that the user is in.
     * @param onClickListener Determines what is the user clicks is a card from the RecyclerView.
     */
    // constructor for variables
    public TheaterAdapter(ArrayList<TheatersModal> theatersModalArrayList, Context context, OnClickListener onClickListener) {
        this.theatersModalArrayList = theatersModalArrayList;
        this.context = context;
        this.onClickListener = onClickListener;
    }

    /**
     * Creates a place for theater cards to be displayed.
     * @param parent   The ViewGroup into which the new View will be added after it is bound to
     *                 an adapter position.
     * @param viewType The view type of the new View.
     * @return new ViewHolder(view)
     */
    @NonNull
    @Override
    public TheaterAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // is to inflate our layout
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.theaters_rv_item,parent,false);
        return new ViewHolder(view);
    }

    /**
     * Creates a card for a MovieModal object that can be clicked.
     * @param holder   The ViewHolder which should be updated to represent the contents of the
     *                 item at the given position in the data set.
     * @param position The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(@NonNull TheaterAdapter.ViewHolder holder, int position) {
        // set data to textviews and listview of each card layout
        TheatersModal modal = theatersModalArrayList.get(position);
        holder.theaterNameTV.setText(modal.getTheaterName());
        holder.theaterAddressTV.setText(modal.getTheaterAddress());
        holder.theaterPhoneTV.setText(modal.getTheaterPhone());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            /**
             * Sets the OnClickListener to the theater object card (from TheaterModal)
             * @param view This current View.
             */
            @Override
            public void onClick(View view) {
                if (onClickListener != null) {
                    onClickListener.onClick(modal);
                }
            }
        });
    }

    /**
     * Gets the size of the theatersModalArrayList
     * @return size of theatersModalArrayList
     */
    // used to show the number of card items in recycler view
    @Override
    public int getItemCount() {
        return theatersModalArrayList.size();
    }

    /**
     * Sets the onClickListener to the currently clicked.
     * @param onClickListener What the user clicked on
     */
    public void setOnClickListener(OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }

    /**
     * Allows for the user to click on a card in Recycler View.
     */
    public interface OnClickListener {
        void onClick(TheatersModal modal);
    }

    /**
     * A customized class of ReycylerView.ViewHolder that shows the cards in the Recycler View.
     */
    // View holder class for initializing views -- TextView and ListView
    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView theaterNameTV;
        private final TextView theaterAddressTV;
        private final TextView theaterPhoneTV;
        //private final ListView theatersMoviesLV;

        /**
         * A single item that is added to a card.
         * @param itemView the View that has the Recycler View is in
         */
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            theaterNameTV = itemView.findViewById(R.id.TheaterName);
            theaterAddressTV = itemView.findViewById(R.id.TheaterAddress);
            theaterPhoneTV = itemView.findViewById(R.id.TheaterPhone);
        }
    }

}
