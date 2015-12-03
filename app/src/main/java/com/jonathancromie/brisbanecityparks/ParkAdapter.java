package com.jonathancromie.brisbanecityparks;

import android.support.v4.app.FragmentManager;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.util.SortedList;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.util.SortedListAdapterCallback;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

/**
 * Created by Jonathan on 11/28/2015.
 */
public class ParkAdapter extends RecyclerView.Adapter<ParkAdapter.ParkViewHolder> {
    public static class ParkViewHolder extends RecyclerView.ViewHolder {
        ImageView parkImage;
        CardView cardView;
        TextView parkName;
        TextView parkStreet;
        TextView parkSuburb;
        TextView parkDistance;
        Button explore;
        Button share;

        ParkViewHolder(View itemView) {
            super(itemView);
            cardView = (CardView) itemView.findViewById(R.id.cardView);
            parkImage = (ImageView) itemView.findViewById(R.id.parkImage);
            parkName = (TextView) itemView.findViewById(R.id.parkName);
//            parkStreet = (TextView) itemView.findViewById(R.id.parkStreet);
//            parkSuburb = (TextView) itemView.findViewById(R.id.parkSuburb);
            parkDistance = (TextView) itemView.findViewById(R.id.parkDistance);
            explore = (Button) itemView.findViewById(R.id.explore);
            explore.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    explore();
                }
            });
        }

        public void explore() {
            FragmentManager fragmentManager = ((AppCompatActivity) itemView.getContext()).getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            ParkFragment fragment = new ParkFragment();
            fragmentTransaction.replace(R.id.content_frame, fragment);
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
//            fragment.getActivity().setTitle(parkName.getText().toString());


        }
    }

    SortedList<Park> parks;
    LayoutInflater layoutInflater;
    ParkAdapter(LayoutInflater layoutInflater, List<Park> items){
        this.layoutInflater = layoutInflater;
        this.parks = new SortedList<Park>(Park.class, new SortedListAdapterCallback<Park>(this) {
            @Override
            public int compare(Park park1, Park park2) {
                if (park1.distance < park2.distance) {
                    return -1;
                }
                else if (park1.distance > park2.distance) {
                    return 1;
                }
                return 0;
            }

            @Override
            public boolean areContentsTheSame(Park oldItem, Park newItem) {
                return false;
            }

            @Override
            public boolean areItemsTheSame(Park item1, Park item2) {
                return false;
            }
        });

        for (Park park : items) {
            parks.add(park);
        }
    }

    public void addPark(Park park) {
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
//        holder.parkStreet.setText(parks.get(position).street);
//        holder.parkSuburb.setText(parks.get(position).suburb);
        int distance = (int) parks.get(position).distance;
        holder.parkDistance.setText(distanceToString(distance));

//        String[] colors = holder.itemView.getResources().getStringArray(R.array.color_array);
//        holder.cardView.setCardBackgroundColor(Color.parseColor(colors[position]));

//        holder.parkImage.setImageBitmap(decodeSampledBitmapFromResource(holder.itemView.getResources(),
//                R.drawable.park, 100, 100));


    }

    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    public static Bitmap decodeSampledBitmapFromResource(Resources res, int resId,
                                                         int reqWidth, int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(res, resId, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeResource(res, resId, options);
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
