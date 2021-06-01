package com.flower.yeshivatyeruham;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;


public class PathsAdapter extends RecyclerView.Adapter <PathsAdapter.ViewHolder> {

    private final Activity context;
    ArrayList names;
    private OnItemClickListener mOnItemClickListener;

    public interface OnItemClickListener {
        public void onItemClick(View view, int position);
    }


    public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        TextView btn;
        public ViewHolder(View v) {
            super(v);
            btn = (TextView) v.findViewById(R.id.path_btn);
        }
    }


    public PathsAdapter(Activity context, ArrayList names, OnItemClickListener onItemClickListener) {
        this.context = context;
        this.names = names;
        mOnItemClickListener = onItemClickListener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent,
                                                         int viewType) {
        // create a new view
        LayoutInflater inflater = context.getLayoutInflater();
        View v = inflater.inflate(R.layout.row_path, parent, false);
        // set the view's size, margins, paddings and layout parameters
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        String name = names.get(position).toString();
        if (position == 0){
            name = "ראשי";
        }
        holder.btn.setText(name);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnItemClickListener != null)
                    mOnItemClickListener.onItemClick(v, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return names.size();
    }
}
