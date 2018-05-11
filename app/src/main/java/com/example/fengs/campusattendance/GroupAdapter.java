package com.example.fengs.campusattendance;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.fengs.campusattendance.database.GroupDB;

import java.util.List;

public class GroupAdapter extends RecyclerView.Adapter<GroupAdapter.ViewHolder> {
    private List<GroupDB> groupDBList;

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView groupName;
        View groupView;

        ViewHolder(View view) {
            super(view);
            groupView = view;
            groupName = view.findViewById(R.id.group_name);
        }
    }

    public GroupAdapter(List<GroupDB> gDBLt) {
        groupDBList = gDBLt;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.group_item, parent, false);
        final ViewHolder viewHolder = new ViewHolder(view);
        viewHolder.groupView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = viewHolder.getAdapterPosition();
                GroupDB groupDB = groupDBList.get(position);
                Toast.makeText(v.getContext(), " you click view "
                + groupDB.getGroupID(), Toast.LENGTH_SHORT).show();
            }
        });
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        GroupDB groupDB = groupDBList.get(position);
        holder.groupName.setText(groupDB.getGroupID() + "_" + groupDB.getGroupName());
    }

    @Override
    public int getItemCount() {
        return groupDBList.size();
    }
}
