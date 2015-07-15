package com.theronin.budgettracker.pages.categories;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.theronin.budgettracker.R;

import java.util.List;

public class CategoriesAdapter  extends RecyclerView.Adapter<CategoriesAdapter.ViewHolder> {

    private final List<String[]> categories;

    public CategoriesAdapter(List<String[]> categories) {
        this.categories = categories;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View listItemView = LayoutInflater.from(parent.getContext()).inflate(R.layout
                .list_item__category, parent, false);

        ViewHolder viewHolder = new ViewHolder(listItemView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {
        viewHolder.nameTextView.setText(categories.get(position)[0]);
        viewHolder.totalTextView.setText(categories.get(position)[1]);
        viewHolder.monthlyTextView.setText(categories.get(position)[2]);
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public TextView nameTextView;
        public TextView totalTextView;
        public TextView monthlyTextView;

        public ViewHolder(View listItemView) {
            super(listItemView);
            nameTextView = (TextView) listItemView.findViewById(R.id.name_column);
            totalTextView = (TextView) listItemView.findViewById(R.id.total_column);
            monthlyTextView = (TextView) listItemView.findViewById(R.id.monthly_column);
        }
    }
}
