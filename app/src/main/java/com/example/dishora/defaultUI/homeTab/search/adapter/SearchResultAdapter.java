package com.example.dishora.defaultUI.homeTab.search.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dishora.R;
import com.example.dishora.defaultUI.homeTab.search.model.SearchResultItem;

import java.util.List;

public class SearchResultAdapter extends RecyclerView.Adapter<SearchResultAdapter.ViewHolder> {

    private Context context;
    private List<SearchResultItem> searchList;

    public SearchResultAdapter(Context context, List<SearchResultItem> searchList) {
        this.context = context;
        this.searchList = searchList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_search_result, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SearchResultItem item = searchList.get(position);
        holder.txtFoodName.setText(item.getFoodName());
        holder.txtStoreName.setText(item.getStoreName());
        holder.txtRating.setText(String.valueOf(item.getRating()) + " ★★★★★");
        holder.txtReviews.setText("(" + item.getReviews() + ")");
        holder.txtTime.setText(item.getTime());
        holder.txtPrice.setText(item.getPrice());
        holder.imgFood.setImageResource(item.getImageResId()); // if URL, use Glide
    }

    @Override
    public int getItemCount() {
        return searchList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgFood;
        TextView txtFoodName, txtStoreName, txtRating, txtReviews, txtTime, txtPrice;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgFood = itemView.findViewById(R.id.imgFood);
            txtFoodName = itemView.findViewById(R.id.txtFoodName);
            txtStoreName = itemView.findViewById(R.id.txtStoreName);
            txtRating = itemView.findViewById(R.id.txtRating);
            txtReviews = itemView.findViewById(R.id.txtReviews);
            txtTime = itemView.findViewById(R.id.txtTime);
            txtPrice = itemView.findViewById(R.id.txtPrice);
        }
    }
}

