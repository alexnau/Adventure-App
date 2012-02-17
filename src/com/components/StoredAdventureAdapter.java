package com.components;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.adventure.R;

import com.obj.StoredAdventure;

public class StoredAdventureAdapter extends ArrayAdapter<StoredAdventure> {
	private ArrayList<StoredAdventure> items;
	
    Context ctx;
    
    public StoredAdventureAdapter(Context context, int textViewResourceId, ArrayList<StoredAdventure> items) {
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
    		v = vi.inflate(R.layout.li_previous_adventure, null);
    	}
    	
    	((TextView) v.findViewById(R.id.tv_destination)).setText(items.get(position).getDestination());
		((TextView) v.findViewById(R.id.tv_start_time)).setText(items.get(position).getStart_time());
    	
        return v;
    }
}