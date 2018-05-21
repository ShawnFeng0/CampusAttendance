package com.example.fengs.campusattendance.dataView;

import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.fengs.campusattendance.R;
import com.example.fengs.campusattendance.database.Face;

import java.util.List;

public class SignInFaceAdapter extends RecyclerView.Adapter<SignInFaceAdapter.ViewHolder> {

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

    public SignInFaceAdapter(List<Face> faceDBLt) {
         setFaceList(faceDBLt);
    }

    public List<Face> getFaceList() {
        return faceList;
    }

    private void setFaceList(List<Face> faceList) {
        this.faceList = faceList;
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.sign_in_face_item, parent, false);
        final ViewHolder viewHolder = new ViewHolder(view);

        viewHolder.faceView.setOnClickListener(v -> {
            int position = viewHolder.getAdapterPosition();
            Face face = faceList.get(position);
            Toast.makeText(parent.getContext(),
                    "学号: " + face.getFaceID() + "  " + "姓名: " + face.getFaceName(), Toast.LENGTH_SHORT).show();
        });

        viewHolder.faceView.setOnLongClickListener(v -> false); //返回false 就会执行短按的内容, 返回true则不会
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

    public void removeDataDelayToDisplay(int position, long delayMillisToDisplay) {
        faceList.remove(position);
        new Handler().postDelayed(() -> notifyItemRemoved(position), delayMillisToDisplay);
    }
}
