package com.usi.mwc.justmove.adapters;

import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.usi.mwc.justmove.R;
import com.usi.mwc.justmove.model.TravelModel;
import com.usi.mwc.justmove.utils.Utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class TravelAdapter extends RecyclerView.Adapter<TravelAdapter.TravelViewHolder> {
    private final List<TravelModel> travelsList;
    private final OnItemClickListener listener;

    public TravelAdapter(List<TravelModel> travelsList, OnItemClickListener listener) {
        this.travelsList = travelsList;
        this.listener = listener;
    }

    @Override
    public TravelAdapter.TravelViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(
            R.layout.travel_card,
            parent,
false
        );
        return new TravelViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(TravelAdapter.TravelViewHolder holder, int position) {
        TravelModel currentItem = travelsList.get(position);
        Double[] cent = Utils.centroid(currentItem.getPoints());
        holder.tvCardDistance.setText("Distance : " + String.format("%.2f", currentItem.getDistance()) + " km");;

        SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss, dd.MM.yyyy");
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, dd MMM yyyy");
        SimpleDateFormat hourMinuteFormat = new SimpleDateFormat("HH:mm");
//        SimpleDateFormat hourFormat = new SimpleDateFormat("HH:mm");
//        SimpleDateFormat hourWithSecondsFormat = new SimpleDateFormat("yyy-MM-dd HH:mm:ss");
        try {
            Date travelDate = format.parse(currentItem.getDateTravel());
//            String a = hourFormat.format(travelDate);
//            Long startTime = hourFormat.parse(a).getTime();
//            Long endTime = hourWithSecondsFormat.parse("1970-01-01 " + currentItem.getTime()).getTime();
//            Long t = startTime + endTime;
//            holder.tvCardTime.setText(String.format("%02d:%02d:%02d",
//                    TimeUnit.MILLISECONDS.toHours(t),
//                    TimeUnit.MILLISECONDS.toMinutes(t) -
//                            TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(t)), // The change is in this line
//                    TimeUnit.MILLISECONDS.toSeconds(t) -
//                            TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(t))));
            holder.tvCardDate.setText(dateFormat.format(travelDate));
            String hourMinDiff = "";
            hourMinDiff.concat(" - ");
            hourMinDiff.concat(hourMinuteFormat.format(travelDate));
            // hourMinDiff.concat(" the second ")  // TODO: set the previous hour:minute
            holder.tvCardTime.setText(hourMinDiff);

        } catch (ParseException e) {
            e.printStackTrace();
        }
        String[] splittedTime = currentItem.getTime().split(":");
        holder.tvCardDuration.setText(String.format("%s h %s m %s s", splittedTime[0], splittedTime[1], splittedTime[2]));
        holder.tvCardPublibike.setText(currentItem.getPublibike() == 0 ? "No Publibike" : "Publibike");
        holder.tvCardTravelName.setText(currentItem.getName());


    }

    @Override
    public int getItemCount() {
        return travelsList.size();
    }

    protected class TravelViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView tvCardDistance = itemView.findViewById(R.id.tvCardDistance);
        TextView tvCardTravelName = itemView.findViewById(R.id.tvCardTravelName);
        TextView tvCardDuration = itemView.findViewById(R.id.tvCardDuration);
        TextView tvCardPublibike = itemView.findViewById(R.id.tvCardPublibike);
        TextView tvCardDate = itemView.findViewById(R.id.tvCardDate);
        TextView tvCardTime = itemView.findViewById(R.id.tvCardTime);

        public TravelViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            int position = this.getAdapterPosition();
            if (position != RecyclerView.NO_POSITION) {
                listener.onClickItem(view, position, travelsList.get(position));
            }
        }
    }

    public interface OnItemClickListener {
        void onClickItem(View v, int position, TravelModel t);
    }
}
