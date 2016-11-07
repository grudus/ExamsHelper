package com.grudus.nativeexamshelper.adapters;


import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.github.mikephil.charting.charts.Chart;
import com.grudus.nativeexamshelper.R;

import java.util.ArrayList;

public class StatisticsAdapter extends RecyclerView.Adapter<StatisticsAdapter.StatisticsViewHolder>{

    private ArrayList<Chart> data;

    public StatisticsAdapter(ArrayList<Chart> data) {
        this.data = data;
    }

    @Override
    public StatisticsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.stat_card_view, parent, false);
        return new StatisticsViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(StatisticsViewHolder holder, int position) {

        Chart chart = data.get(position);

        ViewGroup.MarginLayoutParams params = new ViewGroup.MarginLayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        params.setMargins(16, 16, 16, 16);
        chart.setLayoutParams(params);

        holder.content.addView(chart);
        holder.title.setText(chart.getDescription().getText());
    }

    @Override
    public int getItemCount() {
        return data.size();
    }



    public static class StatisticsViewHolder extends RecyclerView.ViewHolder {

        CardView cardView;
        LinearLayout content;
        TextView title;

        public StatisticsViewHolder(View itemView) {
            super(itemView);
            this.cardView = (CardView) itemView;
            this.content = (LinearLayout) cardView.findViewById(R.id.card_view_content);
            this.title = (TextView) cardView.findViewById(R.id.card_view_title);

        }
    }
}
