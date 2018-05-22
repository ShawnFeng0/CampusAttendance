package com.example.fengs.campusattendance.dataView;

import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.fengs.campusattendance.R;
import com.example.fengs.campusattendance.database.GroupDB;

import org.litepal.crud.DataSupport;

import java.util.List;
/**
 * recyclerView组信息显示适配器
 */
public class GroupAdapter extends RecyclerView.Adapter<GroupAdapter.ViewHolder> {
    private List<GroupDB> groupDBList; //组列表

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView groupName; //组名 group.toString()
        TextView groupCount; //组内人数显示
        View groupView; //包含以上两个视图

        ViewHolder(View view) {
            super(view);
            groupView = view;
            groupName = view.findViewById(R.id.group_name);
            groupCount = view.findViewById(R.id.group_count);
        }
    }

    GroupAdapter(List<GroupDB> gDBLt) {
        groupDBList = gDBLt; //初始化得到组列表
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull final ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.group_item, parent, false); //得到组显示的视图
        final ViewHolder viewHolder = new ViewHolder(view);

        /* 短按进入人脸界面 */
        viewHolder.groupView.setOnClickListener(v -> {
            int position = viewHolder.getAdapterPosition(); //得到当前位置
            GroupDB groupDB = groupDBList.get(position); //得到当前组信息
            ((GroupViewActivity)(parent.getContext())).faceView(groupDB); //进入人脸界面, 传递选中的组信息
        });

        /* 长按删除 */
        viewHolder.groupView.setOnLongClickListener(v -> {
            final int position = viewHolder.getAdapterPosition();
            final GroupDB groupDB = groupDBList.get(position);

            //弹出删除窗口
            AlertDialog.Builder dialog = new AlertDialog.Builder(v.getContext());
            dialog.setMessage(String.format("确认删除：%s?", groupDB.toString()));
            dialog.setPositiveButton("确定", (dialog1, which) -> {
                removeData(position); //删除当前数据
                dialog1.dismiss();
            }).setNegativeButton("取消", (dialog12, which) -> dialog12.dismiss());
            dialog.show();

            return true;
        });
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        GroupDB groupDB = groupDBList.get(position);
        holder.groupName.setText(groupDB.toString());
        holder.groupCount.setText("人数: " + groupDB.getFaces().size());
    }

    @Override
    public int getItemCount() {
        return groupDBList.size();
    }

    /**
     * 删除数据
     * @param position 删除的位置
     */
    private void removeData(int position) {
        DataSupport.delete(GroupDB.class, groupDBList.get(position).getId()); //从数据库删除
        groupDBList.remove(position); //从数据列表中删除
        notifyItemRemoved(position); //刷新当前的位置的数据, 并显示动画
    }

}
