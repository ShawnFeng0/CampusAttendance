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

/**
 * recyclerView人脸信息显示适配器
 */
public class FaceAdapter extends RecyclerView.Adapter<FaceAdapter.ViewHolder> {
    private List<Face> faceList; //人脸列表

    /**
     * 每一项数据的都含有的界面信息
     */
    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView faceName; //姓名
        ImageView faceImageView; //人脸图
        View faceView; //包含以上两个视图

        ViewHolder(View view) {
            super(view);
            faceName = view.findViewById(R.id.face_name); //姓名
            faceImageView = view.findViewById(R.id.face_image); //人脸图
            faceView = view; //包含以上两个视图
        }
    }

    FaceAdapter(List<Face> faceDBLt) {
        faceList = faceDBLt; //初始化得到人脸列表
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.face_item, parent, false); //得到人脸显示的视图
        final ViewHolder viewHolder = new ViewHolder(view);

        /* 注册视图里的短按动作 短按大图显示 */
        viewHolder.faceView.setOnClickListener(v -> {
            int position = viewHolder.getAdapterPosition(); //得到当前位置
            Face face = faceList.get(position);
            Toast.makeText(parent.getContext(),
                    "学号: " + face.getFaceID() + ", 姓名: " + face.getFaceName(), Toast.LENGTH_SHORT).show(); //提示人脸信息
            ((FaceViewActivity)(parent.getContext())).setBigImageView(face.getFaceImage()); //大图显示
        });

        /* 注册视图里的长按动作 长按删除 */
        viewHolder.faceView.setOnLongClickListener(v -> {
            final int position = viewHolder.getAdapterPosition();
            final Face face = faceList.get(position);

            AlertDialog.Builder dialog = new AlertDialog.Builder(v.getContext()); //定义一个弹窗
            dialog.setMessage("确认删除：" + face.getFaceName()); //提示信息
            dialog.setPositiveButton("确定", (dialog12, which) -> { //确定按钮的动作
                removeData(position);
                if (faceList.size() != 0) {//删除之后显示上一张图
                    ((FaceViewActivity) (parent.getContext())).setBigImageView(faceList.get(position == faceList.size() ? position - 1 : position).getFaceImage());
                } else { //删除完了之后大图显示添加的界面
                    ((FaceViewActivity)(parent.getContext())).setBigImageView(R.drawable.add_photos);
                }
                dialog12.dismiss(); //关闭窗口
            }).setNegativeButton("取消", (dialog1, which) -> dialog1.dismiss()); //取消, 关闭窗口
            dialog.show(); //显示

            return true;
        });
        return viewHolder;
    }

    /**
     * 画面中出现某一项时, 运行一次此函数
     * @param holder 当前位置的viewHolder
     * @param position 位置参数
     */
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Face face = faceList.get(position); //得到当前位置的人脸数据
        holder.faceName.setText(face.getFaceName()); //设置名字
        holder.faceImageView.setImageBitmap(face.getFaceImage()); //设置图片
    }

    @Override
    public int getItemCount() {
        return faceList.size();
    }

    /**
     * 删除数据
     * @param position 删除的位置
     */
    private void removeData(int position) {
        DataSupport.delete(Face.class, faceList.get(position).getId()); //从数据库删除
        faceList.remove(position); //从数据列表中删除
        notifyItemRemoved(position); //刷新当前的位置的数据, 并显示动画
    }
}
