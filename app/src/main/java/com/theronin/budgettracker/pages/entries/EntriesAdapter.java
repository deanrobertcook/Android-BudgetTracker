package com.theronin.budgettracker.pages.entries;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.theronin.budgettracker.R;

import java.util.List;

public class EntriesAdapter extends RecyclerView.Adapter<EntriesAdapter.ViewHolder> {

    private final List<String[]> entries;

    public EntriesAdapter(List<String[]> entries) {
        this.entries = entries;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View listItemView = LayoutInflater.from(parent.getContext()).inflate(R.layout
                .list_item__entry, parent, false);

        ViewHolder viewHolder = new ViewHolder(listItemView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {
        viewHolder.costTextView.setText(entries.get(position)[0]);
        viewHolder.categoryTextView.setText(entries.get(position)[1]);
        viewHolder.dateTextView.setText(entries.get(position)[2]);
    }

    @Override
    public int getItemCount() {
        return entries.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public TextView costTextView;
        public TextView categoryTextView;
        public TextView dateTextView;

        public ViewHolder(View listItemView) {
            super(listItemView);
            costTextView = (TextView) listItemView.findViewById(R.id.cost_column);
            categoryTextView = (TextView) listItemView.findViewById(R.id.category_column);
            dateTextView = (TextView) listItemView.findViewById(R.id.date_column);
        }
    }
}
