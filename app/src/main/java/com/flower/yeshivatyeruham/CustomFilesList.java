package com.flower.yeshivatyeruham;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.List;

import static com.flower.yeshivatyeruham.DataClass.SMBRoot;
import static com.flower.yeshivatyeruham.SksActivity.isAudioFile;

public class CustomFilesList extends RecyclerView.Adapter <CustomFilesList.ViewHolder> {

    /**
     * Custom List to every list that contains folders/files list (as in sks, student, download, etc.)
     */
    private final Activity context;
    List<String> names, paths;
    Boolean isFav = false;
    Boolean isMsg = false;
    private OnItemClickListener mOnItemClickListener;
    private View.OnLongClickListener mOnLongClickListener;


    public interface OnItemClickListener {
        public void onItemClick(View view, int position);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public TextView pathTv, txtName;
        public ImageView image;
        Context ctx;
        public ViewHolder(View v) {
            super(v);
            txtName = (TextView) v.findViewById(R.id.txt_name);
            pathTv = (TextView) v.findViewById(R.id.path_tv);
            image = (ImageView) v.findViewById(R.id.listImage);
            ctx = image.getContext();
        }
    }

    public CustomFilesList(Activity context, List names, List paths, OnItemClickListener onItemClickListener, View.OnLongClickListener onLongClickListener, String type) {
        this.context = context;
        this.names = names;
        mOnItemClickListener = onItemClickListener;
        mOnLongClickListener = onLongClickListener;
        if (type.equals("favorites")){
                isFav = true;
                this.paths = paths;
        }
        else if (type.equals("messages")){
            isMsg = true;
            this.paths = paths;
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent,
                                                         int viewType) {
        // create a new view
        LayoutInflater inflater = context.getLayoutInflater();
        View v = inflater.inflate(R.layout.row_files, parent, false);
        // set the view's size, margins, paddings and layout parameters
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public int getItemCount() {
        return names.size();
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        String name = names.get(position).toString();
        if (isFav){
            holder.pathTv.setVisibility(View.VISIBLE);
            holder.pathTv.setText(paths.get(position).toString().replaceAll(SMBRoot, ""));
        }
        if (isMsg){
            holder.pathTv.setVisibility(View.VISIBLE);
            holder.pathTv.setText(paths.get(position).toString());
        }
        String ext = name.substring(name.indexOf(".") + 1).toLowerCase();

        //set click listener
        holder.itemView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (mOnItemClickListener != null)
                    mOnItemClickListener.onItemClick(v, position);
            }
        });

        //set long click listener
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (mOnLongClickListener != null)
                    mOnLongClickListener.onLongClick(v);
                return true;
            }
        });

        //choose the right icon

        int icon;
        if (name.endsWith("/")) {
            name = name.substring(0, name.lastIndexOf("/"));
            icon = R.drawable.folder;
        }
        else if (ext.equals("png")) {
            icon = R.drawable.png;
        }
        else if (ext.equals("xml")) {
            icon = R.drawable.xml;
        }
        else if (ext.equals("txt")) {
            icon = R.drawable.txt;
        }
        else if (ext.equals("doc")||ext.equals("docx")) {
            icon = R.drawable.doc;
        }
        else if (ext.equals("jpg")||ext.equals("bmp")||ext.equals("gif")||ext.equals("img")||ext.equals("jpeg")) {
            icon = R.drawable.jpg;
        }
        else if (ext.equals("pdf")) {
            icon = R.drawable.pdf;
        }
        else if (ext.equals("zip")||ext.equals("rar")||ext.equals("tar")||ext.equals("7z")) {
            icon = R.drawable.zip;
        }
        else if (ext.equals("html")||ext.equals("htm")) {
            icon = R.drawable.html;
        }
        else if (ext.equals("mpg")||ext.equals("mp4")||ext.equals("3gp")||ext.equals("avi")||ext.equals("aaf")||
                ext.equals("aiff")||ext.equals("flv")||ext.equals("mkv")||ext.equals("mpeg")||ext.equals("mp4")) {
            icon = R.drawable.mpg;
        }
        else if (ext.equals("ppt")||ext.equals("pps")||ext.equals("pptx")||ext.equals("ppsx")) {
            icon = R.drawable.ppt;
        }
        else if (isAudioFile(name)) {
            icon = R.drawable.mp3;
        }else if (isFav){
            icon = R.drawable.folder;
            holder.txtName.setTextColor(Color.BLACK);
        }else if (isMsg){
            icon = R.drawable.big_msg_icon;
            holder.txtName.setTextColor(Color.BLACK);
        }else {
            icon = R.drawable.generic_document;
        }
        Glide.with(holder.ctx)
                .load(icon)
                .placeholder(R.drawable.generic)
                .into(holder.image);
        holder.txtName.setText(name);
    }

//    @Override
//    public View getView(final int position, final View view, final ViewGroup parent) {
//        LayoutInflater inflater = context.getLayoutInflater();
//        View rowView = inflater.inflate(R.layout.row_sks, null, true);
//        String name = names.get(position).toString();
//        TextView txtName = (TextView) rowView.findViewById(R.id.txt_name);
//        TextView pathTv = (TextView) rowView.findViewById(R.id.path_tv);
//        image = (ImageView) rowView.findViewById(R.id.listImage);
//        if (isFav){
//            pathTv.setVisibility(View.VISIBLE);
//            pathTv.setText(paths.get(position).toString().replaceAll(SMBRoot, ""));
//        }
//
//        String ext = name.substring(name.indexOf(".") + 1).toLowerCase();
//
//        if (name.endsWith("/")) {
//            name = name.substring(0, name.lastIndexOf("/"));
//            Glide.with(context)
//                .load(R.drawable.folder)
//                .placeholder(R.drawable.generic_document)
//                .into(image);
//        }
//        else if (ext.equals("png")) {
//            Glide.with(context)
//                    .load(R.drawable.png)
//                    .placeholder(R.drawable.generic_document)
//                    .into(image);
//        }
//        else if (ext.equals("xml")) {
//            Glide.with(context)
//                    .load(R.drawable.xml)
//                    .placeholder(R.drawable.generic_document)
//                    .into(image);
//        }
//        else if (ext.equals("txt")) {
//            Glide.with(context)
//                    .load(R.drawable.txt)
//                    .placeholder(R.drawable.generic_document)
//                    .into(image);
//        }
//        else if (ext.equals("doc")||ext.equals("docx")) {
//            Glide.with(context)
//                    .load(R.drawable.doc)
//                    .placeholder(R.drawable.generic_document)
//                    .into(image);
//        }
//        else if (ext.equals("jpg")||ext.equals("bmp")||ext.equals("gif")||ext.equals("img")||ext.equals("jpeg")) {
//            Glide.with(context)
//                    .load(R.drawable.jpg)
//                    .placeholder(R.drawable.generic_document)
//                    .into(image);
//        }
//        else if (ext.equals("pdf")) {
//            Glide.with(context)
//                    .load(R.drawable.pdf)
//                    .placeholder(R.drawable.generic_document)
//                    .into(image);
//        }
//        else if (ext.equals("zip")||ext.equals("rar")||ext.equals("tar")||ext.equals("7z")) {
//            Glide.with(context)
//                    .load(R.drawable.zip)
//                    .placeholder(R.drawable.generic_document)
//                    .into(image);
//        }
//        else if (ext.equals("html")||ext.equals("htm")) {
//            Glide.with(context)
//                    .load(R.drawable.pdf)
//                    .placeholder(R.drawable.generic_document)
//                    .into(image);
//        }
//        else if (ext.equals("mpg")||ext.equals("mp4")||ext.equals("3gp")||ext.equals("avi")||ext.equals("aaf")||
//                ext.equals("aiff")||ext.equals("flv")||ext.equals("mkv")||ext.equals("mpeg")||ext.equals("mp4")) {
//            Glide.with(context)
//                    .load(R.drawable.mpg)
//                    .placeholder(R.drawable.generic_document)
//                    .into(image);
//        }
//        else if (ext.equals("ppt")||ext.equals("pps")) {
//            Glide.with(context)
//                    .load(R.drawable.ppt)
//                    .placeholder(R.drawable.generic_document)
//                    .into(image);
//        }
//        else if (isAudioFile(name)) {
//            Glide.with(context)
//                    .load(R.drawable.mp3)
//                    .placeholder(R.drawable.generic_document)
//                    .into(image);
//        }else if (!isFav){
//            Glide.with(context)
//                    .load(R.drawable.generic_document)
//                    .placeholder(R.drawable.generic_document)
//                    .into(image);
//            txtName.setTextColor(Color.BLACK);
//        }else Glide.with(context)
//                    .load(R.drawable.folder)
//                    .placeholder(R.drawable.generic)
//                    .into(image);
//        txtName.setText(name);
//        return rowView;
//    }
}