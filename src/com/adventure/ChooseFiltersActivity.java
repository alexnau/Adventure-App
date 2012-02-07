package com.adventure;

import java.net.URLEncoder;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.obj.AdventureParams;
import com.obj.Global;

public class ChooseFiltersActivity extends BaseActivity {
	// Ui Elements
	Button btn_next;
	EditText et_keywords;
	EditText et_name;
	RadioButton rb_bicycling;
	RadioButton rb_driving;
	RadioButton rb_walking;
	RadioGroup rg_mode;
	
	AdventureParams params;		// Adventure parameters passed into the activity
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.page_choose_filters);
        
        params = getIntent().getParcelableExtra(INTENT_ADVENTURE_PARAMS);
        
        // Make sure the params are passed in correctly
        if (params == null) {
        	Global.outputError(ChooseFiltersActivity.this, Global.Error.INTENT_ADVENTURE_PARAMS_ERROR);
        	finish();
        }
        
        initUI();
    }

	/**
     * Initialize the UI
     */
    private void initUI() {
    	/* Start Button */
    	btn_next = (Button) findViewById(R.id.btn_next);
    	btn_next.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent i = new Intent(ChooseFiltersActivity.this, ChooseRadiusActivity.class);
				
				// Pass the params
				params.setKeywords(URLEncoder.encode(et_keywords.getText().toString()));
				// TODO CONSTANTS
				switch (rg_mode.getCheckedRadioButtonId()) {
				case R.id.rb_bicycling:
					params.setMode("bicycling");
					break;
				case R.id.rb_driving:
					params.setMode("driving");
					break;
				case R.id.rb_walking:
					params.setMode("walking");
					break;
				}
				params.setName(URLEncoder.encode(et_name.getText().toString()));
				i.putExtra(INTENT_ADVENTURE_PARAMS, params);
				
				startActivityForResult(i, INTENT_SETTINGS_RESULTCODE);
			}
    	});
    	
    	et_keywords = (EditText) findViewById(R.id.et_keywords);
    	et_name = (EditText) findViewById(R.id.et_name);
    	
    	/* RadioButtons */
    	rg_mode = (RadioGroup) findViewById(R.id.rg_mode);
    	
    	rb_bicycling = (RadioButton) findViewById(R.id.rb_bicycling);
    	rb_driving = (RadioButton) findViewById(R.id.rb_driving);
    	rb_walking = (RadioButton) findViewById(R.id.rb_walking);
    }
}
