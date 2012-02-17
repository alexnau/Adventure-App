package com.adventure;

import java.util.ArrayList;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.components.StoredAdventureAdapter;
import com.obj.ParcelableGeoPoint;
import com.obj.StoredAdventure;
import com.util.DataHelper;

public class BrowsePreviousActivity extends BaseActivity {
	// Ui Elements
	ListView lv_previous_adventures;
	
	ArrayList<StoredAdventure> data;
	StoredAdventureAdapter adapter;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.page_previous_adventures);
        
        data = new ArrayList<StoredAdventure>();
        
        // TODO POPULATE DATA
        DataHelper helper = new DataHelper(BrowsePreviousActivity.this);
        Cursor c = helper.query(DataHelper.Table.HISTORY, null, null, null, null, null, null);
        while (c.moveToNext()) {
    		String destination = c.getString(1);
    		String distance_travelled = c.getString(2);
    		String duration = c.getString(3);
    		String route = c.getString(4);
    		String start_time = c.getString(5);
    		
        	data.add(new StoredAdventure(destination, distance_travelled, duration, route, start_time));
        }
        
        adapter = new StoredAdventureAdapter(BrowsePreviousActivity.this, R.layout.li_previous_adventure, data);
        
        initUi();
    }
    
    private void initUi() {
    	lv_previous_adventures = (ListView) findViewById(R.id.lv_previous_adventures);
    	lv_previous_adventures.setAdapter(adapter);
    	
    	lv_previous_adventures.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View v, int pos, long id) {
				Intent i = new Intent(BrowsePreviousActivity.this, ViewAdventureActivity.class);
    			i.putExtra(INTENT_ADVENTURE_PATH, ParcelableGeoPoint.stringToArrayList(data.get(pos).getRoute()));
    			i.putExtra(INTENT_DISTANCE_TRAVELED, data.get(pos).getDistance_traveled());
    			i.putExtra(INTENT_DESTINATION, data.get(pos).getDestination());
    			i.putExtra(INTENT_DURATION, Long.parseLong(data.get(pos).getDuration()));
    			i.putExtra(INTENT_START_TIME, data.get(pos).getStart_time());
    			
    			startActivity(i);
			}
		});
    }
}
