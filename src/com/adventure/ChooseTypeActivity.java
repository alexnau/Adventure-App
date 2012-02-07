package com.adventure;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import com.obj.AdventureParams;
import com.obj.Global;

public class ChooseTypeActivity extends BaseActivity {
	// Ui Elements
	Button btn_next;
	ListView lv_types;
	
	AdventureParams params;				// Adventure parameters passed into the activity
	
	String[][] places_categories;		// A 2-dimensional string array that stores the name of the category and all the subcategories in the Places API
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.page_choose_type);
        
        params = getIntent().getParcelableExtra(INTENT_ADVENTURE_PARAMS);
        
        // Make sure the params are passed in correctly
        if (params == null) {
        	Global.outputError(ChooseTypeActivity.this, Global.Error.INTENT_ADVENTURE_PARAMS_ERROR);
        	finish();
        }
        
        // Load the categories and stuff from resources
        String[] raw_places_categories = getResources().getStringArray(R.array.places_categories);
        places_categories = new String[2][raw_places_categories.length];
        
        // Manually add the "All" category
    	places_categories[0][0] = raw_places_categories[0].split(";")[0];
    	places_categories[1][0] = "";
        
        for (int i = 1; i < places_categories[0].length; i++) {
        	places_categories[0][i] = raw_places_categories[i].split(";")[0];
        	places_categories[1][i] = raw_places_categories[i].split(";")[1];
        }
        
        initUI();
    }

    /**
     * Initialize the UI
     */
    private void initUI() {
    	/* Next Button */
    	btn_next = (Button) findViewById(R.id.btn_next);
    	btn_next.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent i = new Intent(ChooseTypeActivity.this, ChooseFiltersActivity.class);
				
				// Pass the params
				params.setTypes(getTypes());
				i.putExtra(INTENT_ADVENTURE_PARAMS, params);
				
				startActivityForResult(i, INTENT_SETTINGS_RESULTCODE);
			}
    	});
    	
    	/* Types ListView */
    	lv_types = (ListView) findViewById(R.id.lv_types);
    	
    	lv_types.setAdapter(new ArrayAdapter<String>(ChooseTypeActivity.this, android.R.layout.simple_list_item_single_choice, places_categories[0]));
    	lv_types.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
    	lv_types.setItemChecked(0, true);
    }
    
    /**
     * Gets the types out of the ListView and formats them into a list separated by pipes
     * @return - a formatted String of the selected types
     */
    private String getTypes() {
    	return places_categories[1][lv_types.getCheckedItemPosition()];
    }
}
