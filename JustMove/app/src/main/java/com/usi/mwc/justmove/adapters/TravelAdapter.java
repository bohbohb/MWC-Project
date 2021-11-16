package com.usi.mwc.justmove.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;

import com.usi.mwc.justmove.R;
import com.usi.mwc.justmove.model.TravelModel;
import com.usi.mwc.justmove.utils.Utils;

import java.util.List;

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
        holder.tvTest.setText("Position : " + String.valueOf(position));
    }

    @Override
    public int getItemCount() {
        return travelsList.size();
    }

    protected class TravelViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView tvTest = itemView.findViewById(R.id.tvTest);

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
