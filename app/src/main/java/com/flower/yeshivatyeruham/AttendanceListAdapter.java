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
import java.util.HashSet;

import static com.flower.yeshivatyeruham.DataClass.PicSavePath;

public class AttendanceListAdapter extends RecyclerView.Adapter<AttendanceListAdapter.ViewHolder>{
   private AttendanceResponse ar;
   private final Activity context;

   private HashSet<Integer> loading= new HashSet<>();
    private OnItemClickListener mOnItemClickListener;
    public interface OnItemClickListener {
        public void onItemClick(View view, int position);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
       public TextView mTextView;
       public ImageView image;
       Context context;




       public ViewHolder(View v) {
           super(v);
           mTextView = (TextView) v.findViewById(R.id.txt_name_attendance_resp);
           image = (ImageView) v.findViewById(R.id.attendance_response_listImage);
           context = image.getContext();


       }
   }
    public AttendanceListAdapter(AttendanceResponse ar, Activity context, OnItemClickListener onItemClickListener){
       this.ar=ar;
       this.context=context;
       this.mOnItemClickListener=onItemClickListener;

    }

       @Override
   public  ViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
           LayoutInflater inflater = context.getLayoutInflater();
           View v = inflater.inflate(R.layout.row_attendance_response, parent, false);


           ViewHolder vh = new ViewHolder(v);
           return vh;
       }
   @Override
   public void onBindViewHolder(ViewHolder holder, final int position) {
       // - get element from your dataset at this position
       // - replace the contents of the view with that element
       holder.mTextView.setText(ar.getName(position));
       if (ar.isHere(position) == null) {
           holder.itemView.setBackgroundResource(R.color.did_not_respond);


       } else if (ar.isHere(position)){
           holder.itemView.setBackgroundResource(R.color.attendance_here);
       }
       else {
           holder.itemView.setBackgroundResource(R.color.attendance_not_here);



       }
       if(loading.contains(position)) {
           holder.itemView.setBackgroundResource(R.color.background_color);
       }

//        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
//        Boolean showPics = sharedPreferences.getBoolean("show_pics",
//                context.getResources().getBoolean(R.bool.show_pics_default));
       if (ar.getShowPics()) {
           File f = new File(PicSavePath + ar.getNum(position) + ".jpeg");
           Glide.with(holder.context)
                   .load(f)
                   .bitmapTransform(new CropSquareTransformation(ar.getContext()))
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

       }

   @Override
   public int getItemCount() {
       return ar.getItemCount();
   }

   public void addToList(int pos){
       loading.add(pos);
   }
    public void removeFromList(int pos){
        loading.remove(pos);

    }

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
