package com.example.fengs.campusattendance.DataView;

import android.content.DialogInterface;
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
        viewHolder.faceView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = viewHolder.getAdapterPosition();
                Face face = faceList.get(position);
                Toast.makeText(v.getContext(), " you click view "
                        + face.getStudentName(), Toast.LENGTH_SHORT).show();
            }
        });
        viewHolder.faceView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                final int position = viewHolder.getAdapterPosition();
                final Face face = faceList.get(position);
                AlertDialog.Builder dialog = new AlertDialog.Builder(v.getContext());
                dialog.setMessage("确认删除：" + face.getStudentName());
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
        Face face = faceList.get(position);
        holder.faceName.setText(face.getStudentName());
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
