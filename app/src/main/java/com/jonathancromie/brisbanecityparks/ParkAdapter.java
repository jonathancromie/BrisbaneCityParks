package com.jonathancromie.brisbanecityparks;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

/**
 * Created by Jonathan on 11/28/2015.
 */
public class ParkAdapter extends RecyclerView.Adapter<ParkAdapter.ParkViewHolder> {

    public static class ParkViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        TextView parkName;
        TextView parkStreet;
        TextView parkSuburb;

        ParkViewHolder(View itemView) {
            super(itemView);
            cardView = (CardView)itemView.findViewById(R.id.cardView);
            parkName = (TextView)itemView.findViewById(R.id.parkName);
            parkStreet = (TextView)itemView.findViewById(R.id.parkStreet);
            parkSuburb = (TextView) itemView.findViewById(R.id.parkSuburb);
        }
    }

    List<Parks> parks;
    ParkAdapter(List<Parks> parks){
        this.parks = parks;
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
    }

    @Override
    public int getItemCount() {
        return parks.size();
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    //    /**
//     * Adapter context
//     */
//    Context mContext;
//
//    /**
//     * Adapter View layout
//     */
//    int mLayoutResourceId;
//
//    public ParkAdapter(Context context, int layoutResourceId) {
//        super(context, layoutResourceId);
//
//        mContext = context;
//        mLayoutResourceId = layoutResourceId;
//    }
//
//    /**
//     * Returns the view for a specific item on the list
//     */
//    @Override
//    public View getView(int position, View convertView, ViewGroup parent) {
//        View row = convertView;
//
//        final Parks currentItem = getItem(position);
//
//        if (row == null) {
//            LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
//            row = inflater.inflate(mLayoutResourceId, parent, false);
//        }
//
//        row.setTag(currentItem);
////        final CheckBox checkBox = (CheckBox) row.findViewById(R.id.checkToDoItem);
////        checkBox.setText(currentItem.getText());
////        checkBox.setChecked(false);
////        checkBox.setEnabled(true);
////
////        checkBox.setOnClickListener(new View.OnClickListener() {
////
////            @Override
////            public void onClick(View arg0) {
////                if (checkBox.isChecked()) {
////                    checkBox.setEnabled(false);
////                    if (mContext instanceof ToDoActivity) {
////                        ToDoActivity activity = (ToDoActivity) mContext;
////                        activity.checkItem(currentItem);
////                    }
////                }
////            }
////        });
//
//        return row;
//    }
}
