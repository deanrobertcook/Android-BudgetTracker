package org.theronin.expensetracker.pages.entries.insert;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.theronin.expensetracker.model.Category;

import java.util.ArrayList;
import java.util.List;

public class CategorySelectAdapter extends RecyclerView.Adapter<CategorySelectAdapter.ViewHolder> {

    private List<Category> categories;
    private CategorySelectedListener listener;

    public CategorySelectAdapter(CategorySelectedListener listener) {
        categories = new ArrayList<>();
        this.listener = listener;
    }

    public void setCategories(List<Category> categories) {
        if (categories == null) {
            categories = new ArrayList<>();
        }
        this.categories = categories;
        //TODO sorting
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(
                android.R.layout.simple_spinner_item, parent, false);

        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final Category category = categories.get(position);
        ((TextView) holder.itemView).setText(category.name);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onCategorySelected(category.name);
            }
        });
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public ViewHolder(View itemView) {
            super(itemView);
        }
    }

    public interface CategorySelectedListener {
        void onCategorySelected(String categoryName);
    }
}
