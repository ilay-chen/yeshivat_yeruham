package com.flower.yeshivatyeruham;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.Transformation;
import com.bumptech.glide.load.engine.Resource;
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.BitmapResource;
import com.bumptech.glide.signature.StringSignature;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static com.flower.yeshivatyeruham.DataClass.PicSavePath;


public class CustomContactsList extends RecyclerView.Adapter <CustomContactsList.ViewHolder> {

    private final Activity context;
    private List arrName, arrNUm, arrGroup, checkedList;
    private Boolean showPics;
    private OnItemClickListener mOnItemClickListener;
    private View.OnLongClickListener mOnLongClickListener;

    public interface OnItemClickListener {
        public void onItemClick(View view, int position);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public TextView txtName, txtNum, txtGroup;
        public ImageView image;
        Context context;

        public ViewHolder(View v) {
            super(v);
            txtName = (TextView) v.findViewById(R.id.txt_name);
            txtNum = (TextView) v.findViewById(R.id.txt_num);
            txtGroup = (TextView) v.findViewById(R.id.txt_group);
            image = (ImageView) v.findViewById(R.id.listImage);
            context = image.getContext();
        }
    }


    public CustomContactsList(Activity context,
                              List arrName, List arrNUm, List arrGroup, ArrayList checkedList, Boolean showPics, OnItemClickListener onItemClickListener, View.OnLongClickListener onLongClickListener) {
        this.context = context;
        this.arrName = arrName;
        this.arrNUm = arrNUm;
        this.arrGroup = arrGroup;
        this.checkedList = checkedList;
        this.showPics = showPics;
        mOnItemClickListener = onItemClickListener;
        mOnLongClickListener = onLongClickListener;
    }
    public CustomContactsList(Activity context, List arrNUm, Boolean showPics, OnItemClickListener onItemClickListener, View.OnLongClickListener onLongClickListener){
        this.context = context;
        arrName = ContactSelectedActivity.createNameArr(context, arrNUm);
        this.arrNUm = arrNUm;
        arrGroup = ContactSelectedActivity.createArrGroup(context, arrNUm);
        checkedList = ContactSelectedActivity.createCheckedList(arrNUm);
        this.showPics = showPics;
        mOnItemClickListener = onItemClickListener;
        mOnLongClickListener = onLongClickListener;
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent,
                                         int viewType) {
        // create a new view
        LayoutInflater inflater = context.getLayoutInflater();
        View v = inflater.inflate(R.layout.row_contacts, parent, false);
        // set the view's size, margins, paddings and layout parameters
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element

        holder.txtName.setText(arrName.get(position).toString());
        holder.txtNum.setText(arrNUm.get(position).toString());
        holder.txtGroup.setText(arrGroup.get(position).toString());
        if (checkedList.contains(position))
            holder.itemView.setBackgroundResource(R.color.listview_pressed_color);
        else {
            holder.itemView.setBackgroundResource(R.color.listview_default_color);
            ;
        }

//        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
//        Boolean showPics = sharedPreferences.getBoolean("show_pics",
//                context.getResources().getBoolean(R.bool.show_pics_default));
        if (showPics) {
            File f = new File(PicSavePath + arrNUm.get(position).toString() + ".jpeg");
            Glide.with(holder.context)
                    .load(f)
                    .bitmapTransform(new CropSquareTransformation(context))
                    .placeholder(R.drawable.small_contact_pic)
                    .signature(new StringSignature((String.valueOf(f.lastModified()))))
                    .into(holder.image);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                mOnItemClickListener.onItemClick(v, position);
            }
        });

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                mOnLongClickListener.onLongClick(v);
                return true;
            }
        });

    }


    @Override
    public int getItemCount() {
        return arrName.size();
    }


//    @Override
//    public View getView(final int position, final View view, final ViewGroup parent) {
//        LayoutInflater inflater = context.getLayoutInflater();
//        View rowView = inflater.inflate(R.layout.row_contacts, null, true);
//
//        TextView txtName = (TextView) rowView.findViewById(R.id.txt_name);
//        TextView txtNum = (TextView) rowView.findViewById(R.id.txt_num);
//        TextView txtGroup = (TextView) rowView.findViewById(R.id.txt_group);
//        image = (ImageView)  rowView.findViewById(R.id.listImage);
//        Context context = image.getContext();
//
//        txtName.setText(arrName.get(position).toString());
//        txtNum.setText(arrNUm.get(position).toString());
//        txtGroup.setText(arrGroup.get(position).toString());
//        if (checkedList.contains(position))
//            rowView.setBackgroundResource(R.color.listview_pressed_color);
//
//        File f = new File(PicSavePath + arrNUm.get(position).toString() + ".jpeg");
//
//        Glide.with(context)
//            .load(f)
//            .bitmapTransform(new CropSquareTransformation(context))
//            .placeholder(R.drawable.small_contact_pic)
//            .signature(new StringSignature((String.valueOf(f.lastModified()))))
//            .into(image);
//
//        return rowView;
//    }

    public static class CropSquareTransformation implements Transformation<Bitmap> {

        private BitmapPool mBitmapPool;
        private int mWidth;
        private int mHeight;

        public CropSquareTransformation(Context context) {
            this(Glide.get(context).getBitmapPool());
        }

        public CropSquareTransformation(BitmapPool pool) {
            this.mBitmapPool = pool;
        }

        @Override
        public Resource<Bitmap> transform(Resource<Bitmap> resource, int outWidth, int outHeight) {
            Bitmap source = resource.get();
            int size = Math.min(source.getWidth(), source.getHeight());

            mWidth = (source.getWidth() - size) / 2;
            mHeight = (source.getHeight() - size) / 2;

            Bitmap.Config config =
                    source.getConfig() != null ? source.getConfig() : Bitmap.Config.ARGB_8888;
            Bitmap bitmap = mBitmapPool.get(mWidth, mHeight, config);
            if (bitmap == null) {
                bitmap = Bitmap.createBitmap(source, mWidth, mHeight, size, size);
            }

            return BitmapResource.obtain(bitmap, mBitmapPool);
        }

        @Override
        public String getId() {
            return "CropSquareTransformation(width=" + mWidth + ", height=" + mHeight + ")";
        }
    }


}