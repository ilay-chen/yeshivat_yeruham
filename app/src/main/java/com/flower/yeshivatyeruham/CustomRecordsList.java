package com.flower.yeshivatyeruham;

/**
 * Created by ilay on 15/07/2015.
 */

import android.app.Activity;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;

/**
 * custom list of lessons that had recorded.
 * if already uploaded - mark it.
 */
public class CustomRecordsList extends ArrayAdapter<String> {

    private final Activity context;
    private List<String> text;
    ListView data;

    public CustomRecordsList(Activity context,
                             List<String> text, ListView data) {
        super(context, R.layout.row_records, text);
        this.context = context;
        this.text = text;
        this.data = data;
    }
    @Override
    public View getView(final int position, final View view, final ViewGroup parent) {
        LayoutInflater inflater = context.getLayoutInflater();

            View rowView = inflater.inflate(R.layout.row_records, null, true);

            TextView txtTitle = (TextView) rowView.findViewById(R.id.txt);

        //if already uploaded - mark it.
        if(text.get(position).contains("~")) {
            rowView.setBackgroundColor(Color.parseColor("#15000000"));
    }
        //if already uploaded - mark it.
        if(position<text.size()) {
            txtTitle.setText(text.get(position).replace("~","✓  "));
        }

        if(position>=text.size()) {rowView.setVisibility(View.GONE);}
             //data.removeViewInLayout(view);}

        return rowView;
    }
}