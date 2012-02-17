package com.adventure;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;

import com.obj.AdventureParams;
import com.obj.Global;
import com.obj.PreferencesManager;

public class StartActivity extends BaseActivity {
	/***
	 * NOTE: This activity is a MapActivity (with an unused Map) so that when we do use the map for real, it does not take 
	 * a long time to load.
	 */
	
	// Ui Elements
	Button btn_new_adventure;
	Button btn_previous_adventures;
	Button btn_settings;
	ImageView iv_background;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Initialize the preferences manager
        Global.preferences_manager = new PreferencesManager(getApplicationContext());
        
        setContentView(R.layout.page_start);
        
        initUI();
    }
    
    @Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (data != null && data.getBooleanExtra(INTENT_ADVENTURE_PARAMS_COMPLETE, false)) {
			Intent i = new Intent(StartActivity.this, AdventureActivity.class);
			
			i.putExtras(data);
			startActivity(i);
		} else {
			super.onActivityResult(requestCode, resultCode, data);
		}
	}

	/**
     * Initialize the UI
     */
    private void initUI() {
    	/* New Adventure Button */
    	btn_new_adventure = (Button) findViewById(R.id.btn_new_adventure);
    	btn_new_adventure.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent i = new Intent(StartActivity.this, ChooseTypeActivity.class);
				
				// Pass the params
				AdventureParams params = new AdventureParams();
				i.putExtra(INTENT_ADVENTURE_PARAMS, params);
				
				startActivityForResult(i, INTENT_SETTINGS_RESULTCODE);
			}
    	});
    	
    	/* Previous Adventures Button */
    	btn_previous_adventures = (Button) findViewById(R.id.btn_previous_adventures);
    	btn_previous_adventures.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent i = new Intent(StartActivity.this, BrowsePreviousActivity.class);
				startActivity(i);
			}
    	});
    	
    	/* Settings Button */
    	btn_settings = (Button) findViewById(R.id.btn_settings);
    	btn_settings.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent i = new Intent(StartActivity.this, PreferencesActivity.class);
				startActivity(i);
			}
    	});
    }
}
