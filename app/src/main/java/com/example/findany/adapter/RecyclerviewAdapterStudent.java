package com.example.findany.adapter;

import android.content.Intent;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.example.findany.R;
import com.example.findany.Resume;
import com.example.findany.model.ModelClassStudent;

import java.util.ArrayList;
import java.util.List;

public class RecyclerviewAdapterStudent extends RecyclerView.Adapter<RecyclerviewAdapterStudent.ViewHolder> {

    private List<ModelClassStudent> userdetails;
    private List<ModelClassStudent> filteredUserDetails;

    public RecyclerviewAdapterStudent(List<ModelClassStudent> userdetails) {
        // Check if userdetails is null and initialize accordingly
        if (userdetails != null) {
            this.userdetails = new ArrayList<>(userdetails);
            this.filteredUserDetails = new ArrayList<>(userdetails);
        } else {
            this.userdetails = new ArrayList<>();
            this.filteredUserDetails = new ArrayList<>();
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.studentprofileview, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if (position >= filteredUserDetails.size()) {
            holder.showNoResultsMessage();
            holder.itemView.setOnClickListener(null);
        } else {
            ModelClassStudent student = filteredUserDetails.get(position);
            String fullName = student.getName();
            String studyYear = student.getYear();
            String studyBranch = student.getBranch();
            String imageUrl = student.getImageUrl();
            String regno = student.getRegno();

            holder.setData(fullName, studyYear, studyBranch, imageUrl);

            holder.itemView.setOnClickListener(v -> {
                // Redirect to the specified layout
                Intent intent = new Intent(holder.itemView.getContext(), Resume.class);
                intent.putExtra("RegNo", regno); // Sending regno through Intent
                holder.itemView.getContext().startActivity(intent);
            });
        }
    }


    @Override
    public int getItemCount() {
        return Math.max(filteredUserDetails.size(), 1);
    }
    public void filter(String query) {
        filteredUserDetails.clear();
        if (TextUtils.isEmpty(query)) {
            // Check if userdetails is null
            if (userdetails != null) {
                filteredUserDetails.addAll(userdetails);
            }
        } else {
            query = query.toLowerCase().trim();
            // Check if userdetails is null
            if (userdetails != null) {
                for (ModelClassStudent student : userdetails) {
                    // Filtering logic remains the same
                    if (student.getName().toLowerCase().contains(query) ||
                            student.getYear().toLowerCase().contains(query) ||
                            student.getBranch().toLowerCase().contains(query) ||
                            student.getProject().toLowerCase().contains(query) ||
                            student.getSkills().toLowerCase().contains(query) ||
                            student.getRegno().toLowerCase().contains(query)) {

                        filteredUserDetails.add(student);
                    }
                }
            }
        }
        notifyDataSetChanged();
    }
    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView name;
        private TextView year;
        private TextView branch;
        private ImageView imageView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.fullname);
            year = itemView.findViewById(R.id.year);
            branch = itemView.findViewById(R.id.branch);
            imageView = itemView.findViewById(R.id.profileimage);
        }

        public void setData(String fullName, String studyYear, String studyBranch, String imageUrl) {
            name.setText(fullName);
            if (TextUtils.isEmpty(studyYear)) {
                year.setVisibility(View.GONE);
            } else {
                year.setVisibility(View.VISIBLE);
                year.setText(studyYear);
            }
            if (TextUtils.isEmpty(studyBranch)) {
                branch.setVisibility(View.GONE);
            } else {
                branch.setVisibility(View.VISIBLE);
                branch.setText(studyBranch);
            }

            RequestOptions requestOptions = new RequestOptions()
                    .placeholder(R.drawable.defaultprofile)
                    .error(R.drawable.defaultprofile);

            Glide.with(itemView.getContext())
                    .load(imageUrl)
                    .apply(requestOptions)
                    .diskCacheStrategy(DiskCacheStrategy.ALL) // Cache both original & resized image
                    .into(imageView);
        }
        public void showNoResultsMessage() {
            name.setText("No results found");
            year.setText("");
            branch.setText("");
            Glide.with(itemView.getContext())
                    .load(R.drawable.noresults)
                    .diskCacheStrategy(DiskCacheStrategy.ALL) // Cache both original & resized image
                    .into(imageView);
        }
    }
}