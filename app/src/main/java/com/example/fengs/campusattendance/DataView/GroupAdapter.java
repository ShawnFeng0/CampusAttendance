package com.example.fengs.campusattendance.DataView;

import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.fengs.campusattendance.R;
import com.example.fengs.campusattendance.database.GroupDB;

import org.litepal.crud.DataSupport;

import java.util.List;

public class GroupAdapter extends RecyclerView.Adapter<GroupAdapter.ViewHolder> {
    private List<GroupDB> groupDBList;

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView groupName;
        TextView groupCount;
        View groupView;

        ViewHolder(View view) {
            super(view);
            groupView = view;
            groupName = view.findViewById(R.id.group_name);
            groupCount = view.findViewById(R.id.group_count);
        }
    }

    GroupAdapter(List<GroupDB> gDBLt) {
        groupDBList = gDBLt;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull final ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.group_item, parent, false);
        final ViewHolder viewHolder = new ViewHolder(view);
        viewHolder.groupView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = viewHolder.getAdapterPosition();
                GroupDB groupDB = groupDBList.get(position);
                ((GroupViewActivity)(parent.getContext())).faceView(groupDB); //进入人脸界面
            }
        });
        viewHolder.groupView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                final int position = viewHolder.getAdapterPosition();
                final GroupDB groupDB = groupDBList.get(position);
                AlertDialog.Builder dialog = new AlertDialog.Builder(v.getContext());
                dialog.setMessage("确认删除：" + groupDB.getGroupID() + "_" + groupDB.getGroupName() + "?");
                dialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        removeData(position);
                        dialog.dismiss();
                    }
                }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                dialog.show();

                return true;
            }

        });
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        GroupDB groupDB = groupDBList.get(position);
        holder.groupName.setText(groupDB.getGroupID() + ":" + groupDB.getGroupName());
        holder.groupCount.setText("人数: " + groupDB.getFaces().size());
    }

    @Override
    public int getItemCount() {
        return groupDBList.size();
    }

    private void removeData(int position) {
        DataSupport.delete(GroupDB.class, groupDBList.get(position).getId());
        groupDBList.remove(position);
        notifyItemRemoved(position);
    }

}
