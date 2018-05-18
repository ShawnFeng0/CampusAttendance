package com.example.fengs.campusattendance.dataView;

import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.fengs.campusattendance.R;
import com.example.fengs.campusattendance.database.Face;

import org.litepal.crud.DataSupport;

import java.util.List;

public class FaceAdapter extends RecyclerView.Adapter<FaceAdapter.ViewHolder> {
    private List<Face> faceList;

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView faceName;
        ImageView faceImageView;
        View faceView;

        ViewHolder(View view) {
            super(view);
            faceView = view;
            faceName = view.findViewById(R.id.face_name);
            faceImageView = view.findViewById(R.id.face_image);
        }
    }

    public FaceAdapter(List<Face> faceDBLt) {
        faceList = faceDBLt;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.face_item, parent, false);
        final ViewHolder viewHolder = new ViewHolder(view);

        /* 短按大图显示 */
        viewHolder.faceView.setOnClickListener(v -> {
            int position = viewHolder.getAdapterPosition();
            Face face = faceList.get(position);
            Toast.makeText(parent.getContext(),
                    "学号: " + face.getFaceID() + ", 姓名: " + face.getFaceName(), Toast.LENGTH_SHORT).show();
            ((FaceViewActivity)(parent.getContext())).setBigImageView(face.getFaceImage());
        });

        /* 长按删除 */
        viewHolder.faceView.setOnLongClickListener(v -> {
            final int position = viewHolder.getAdapterPosition();
            final Face face = faceList.get(position);
            AlertDialog.Builder dialog = new AlertDialog.Builder(v.getContext());
            dialog.setMessage("确认删除：" + face.getFaceName());
            dialog.setPositiveButton("确定", (dialog12, which) -> {
                removeData(position);
                if (faceList.size() != 0) {
                    ((FaceViewActivity) (parent.getContext())).setBigImageView(faceList.get(position == faceList.size() ? position - 1 : position).getFaceImage());
                } else {
                    ((FaceViewActivity)(parent.getContext())).setBigImageView(R.drawable.add_photos);
                }
                dialog12.dismiss();
            }).setNegativeButton("取消", (dialog1, which) -> dialog1.dismiss());
            dialog.show();

            return true;
        });
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Face face = faceList.get(position);
        holder.faceName.setText(face.getFaceName());
        holder.faceImageView.setImageBitmap(face.getFaceImage());
    }

    @Override
    public int getItemCount() {
        return faceList.size();
    }

    private void removeData(int position) {
        DataSupport.delete(Face.class, faceList.get(position).getId());
        faceList.remove(position);
        notifyItemRemoved(position);
    }
}
