package com.example.findany;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class searchanyAdapter extends RecyclerView.Adapter<searchanyAdapter.SearchResultViewHolder> {
    private List<SearchResult> searchResults;

    public searchanyAdapter(List<SearchResult> searchResults) {
        this.searchResults = searchResults;
    }

    @NonNull
    @Override
    public SearchResultViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.searchanylecturers, parent, false);
        return new SearchResultViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SearchResultViewHolder holder, int position) {
        SearchResult result = searchResults.get(position);

        // Set the data to the TextViews in your ViewHolder
        holder.roomNo.setText("Room No: " + result.getRoomNo());
        holder.lecturerName.setText("Lecturer Name: " + result.getLecturerName());
        holder.beforeText.setText("Before: " + result.getBeforeText());
        holder.ongoingText.setText("Ongoing: " + result.getOngoingText());
        holder.afterText.setText("After: " + result.getAfterText());
    }

    @Override
    public int getItemCount() {
        return searchResults.size();
    }

    public class SearchResultViewHolder extends RecyclerView.ViewHolder {
        TextView beforeText;
        TextView ongoingText;
        TextView afterText;
        TextView roomNo;
        TextView lecturerName;

        public SearchResultViewHolder(@NonNull View itemView) {
            super(itemView);
            beforeText = itemView.findViewById(R.id.beforetext);
            ongoingText = itemView.findViewById(R.id.ongoingtext);
            afterText = itemView.findViewById(R.id.aftertext);
            roomNo = itemView.findViewById(R.id.officeroom);
            lecturerName = itemView.findViewById(R.id.lecturername);
        }
    }

    // Method to update the search results
    public void setSearchResults(List<SearchResult> results) {
        searchResults = results;
        notifyDataSetChanged();
    }
}