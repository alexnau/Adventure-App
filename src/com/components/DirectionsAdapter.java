package com.components;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.adventure.R;
import com.obj.Global;
import com.obj.Placemark;

public class DirectionsAdapter extends ArrayAdapter<Placemark> {
    private ArrayList<Placemark> items;
    Context ctx;
    
    public DirectionsAdapter(Context context, int textViewResourceId, ArrayList<Placemark> items) {
            super(context, textViewResourceId, items);
            this.items = items;
            
            ctx = context;
    }
    
    @Override
	protected void finalize() throws Throwable {
		super.finalize();
    	items.clear();
    	ctx = null;
	}

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
    	View v = convertView;
    	if (v == null) {
    		LayoutInflater vi = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    		v = vi.inflate(R.layout.li_directions, null);
    	}
    	
    	((TextView) v.findViewById(R.id.tv_directions)).setText(items.get(position).getTitle());
    	
    	if (items.get(position).getDescription() != null)
    		((TextView) v.findViewById(R.id.tv_description)).setText(items.get(position).getDescription().replaceAll(Global.HTML_ENCODED_SPACE, " "));
		
        return v;
    }
}