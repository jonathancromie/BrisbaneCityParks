package com.jonathancromie.brisbanecityparks;

import android.support.v7.util.SortedList;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.util.SortedListAdapterCallback;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

/**
 * Created by Jonathan on 11/28/2015.
 */
public class ParkAdapter extends RecyclerView.Adapter<ParkAdapter.ParkViewHolder> {

//    private SortedList<Parks> parks;

    public static class ParkViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        TextView parkName;
        TextView parkStreet;
        TextView parkSuburb;
        TextView parkDistance;

        ParkViewHolder(View itemView) {
            super(itemView);
            cardView = (CardView)itemView.findViewById(R.id.cardView);
            parkName = (TextView)itemView.findViewById(R.id.parkName);
            parkStreet = (TextView)itemView.findViewById(R.id.parkStreet);
            parkSuburb = (TextView) itemView.findViewById(R.id.parkSuburb);
            parkDistance = (TextView) itemView.findViewById(R.id.parkDistance);
        }
    }

    SortedList<Parks> parks;
    LayoutInflater layoutInflater;
    ParkAdapter(LayoutInflater layoutInflater, List<Parks> items){
        this.layoutInflater = layoutInflater;
//        this.parks = parks;
        this.parks = new SortedList<Parks>(Parks.class, new SortedListAdapterCallback<Parks>(this) {
            @Override
            public int compare(Parks park1, Parks park2) {
                if (park1.distance < park2.distance) {
                    return -1;
                }
                else if (park1.distance > park2.distance) {
                    return 1;
                }
                return 0;
            }

            @Override
            public boolean areContentsTheSame(Parks oldItem, Parks newItem) {
                return false;
            }

            @Override
            public boolean areItemsTheSame(Parks item1, Parks item2) {
                return false;
            }
        });

        for (Parks park : items) {
            parks.add(park);
        }
    }

    public void addPark(Parks park) {
        parks.add(park);
    }

    @Override
    public ParkViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_list_park, parent, false);
        ParkViewHolder parkViewHolder = new ParkViewHolder(view);
        return parkViewHolder;
    }

    @Override
    public void onBindViewHolder(ParkViewHolder holder, int position) {
        holder.parkName.setText(parks.get(position).name);
        holder.parkStreet.setText(parks.get(position).street);
        holder.parkSuburb.setText(parks.get(position).suburb);
        int distance = (int) parks.get(position).distance;
        holder.parkDistance.setText(distanceToString(distance));
    }

    @Override
    public int getItemCount() {
        return parks.size();
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    private String distanceToString(double distance) {
        return String.valueOf(distance / 1000) + " km";
    }
}
