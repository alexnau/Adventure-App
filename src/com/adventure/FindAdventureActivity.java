package com.adventure;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.obj.Adventure;
import com.obj.AdventureParams;
import com.obj.Global;
import com.util.HttpHelper;

public class FindAdventureActivity extends BaseActivity {
	// Progress update for GetAdventureTask
	private static enum ADVENTURE_GET_STATUS {
		FINDING_ADVENTURES,
		PREPARING_ADVENTURE
	}
	
	// Ui Elements
	TextView tv_prompt;
	
	AdventureParams params;		// Adventure parameters passed into the activity
	
	// AsyncTasks
	GetAdventureTask task_get_adventures;	// Gets possible adventures based on the given parameters
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.page_finding_adventure);
        
        params = getIntent().getParcelableExtra(INTENT_ADVENTURE_PARAMS);

        // Make sure the params are passed in correctly
        if (params == null) {
        	Global.outputError(FindAdventureActivity.this, Global.Error.INTENT_ADVENTURE_PARAMS_ERROR);
        	finish();
        }
        
        initUi();
    }
    
    @Override
    public void onPause() {
    	super.onPause();
    	
    	// Cancel the Get Adventures Task when this activity is no longer in the foreground
    	task_get_adventures.cancel(true);
    	task_get_adventures = null;
    }
    
    @Override
    protected void onResume() {
    	super.onResume();
    	
    	// Start the Get Adventures Task
        task_get_adventures = new GetAdventureTask();
        task_get_adventures.execute(params);
    }
    
    private void initUi() {
    	tv_prompt = (TextView) findViewById(R.id.tv_prompt);
    }
    
    /**
     * Parses a JSON Object into a list of Adventures
     * @param data - the JSON to parse
     * @return - an array of Adventure objects, which may contain null objects
     * @throws JSONException - if there is a parsing error
     */
    Adventure[] parseAdventures(JSONObject data) throws JSONException {
    	Adventure[] adventures = null;
    	
		JSONArray results = data.getJSONArray(Global.Google.Places.Result.Results.RESULTS);
		adventures = new Adventure[results.length()];
		
		// Parse the adventures
		for (int i = 0; i < results.length(); i++) {
			try {
				adventures[i] = new Adventure(results.getJSONObject(i));
			} catch (JSONException e) {
				Log.e(FindAdventureActivity.class.getSimpleName(), Global.Error.JSON_PARSE_ERROR + "Adventure " + String.valueOf(i) + " bad");
				adventures[i] = null;
			}
		}
		
		return adventures;
    }
    
    private class GetAdventureTask extends AsyncTask<AdventureParams, ADVENTURE_GET_STATUS, Adventure> {    	
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			
			publishProgress(ADVENTURE_GET_STATUS.FINDING_ADVENTURES);
		}

		@Override
		protected Adventure doInBackground(AdventureParams... p) {
			// Get a random number
			Random generator = new Random(System.currentTimeMillis());
			
			Adventure[] adventures = null;
			
			Adventure chosen_adventure = null;
			
			// Find the Adventures
			try {
				adventures = getAdventures(p[0]);
			} catch (IOException e) {
				Global.outputError(FindAdventureActivity.this, Global.Error.NETWORK_ERROR + "could not retrieve Adventures");
				e.printStackTrace();
				cancel(true);
				finish();
			} catch (JSONException e) {
				Global.outputError(FindAdventureActivity.this, Global.Error.JSON_PARSE_ERROR + "could not parse Adventures");
				e.printStackTrace();
				cancel(true);
				finish();
			}
			
			// Parse out any error state adventures
			ArrayList<Adventure> valid_adventures = new ArrayList<Adventure>();
			for (int i = 0; i < adventures.length; i++)
				valid_adventures.add(adventures[i]);
			
			chosen_adventure = valid_adventures.get(generator.nextInt(valid_adventures.size()));
			
			publishProgress(ADVENTURE_GET_STATUS.PREPARING_ADVENTURE);
			
			return chosen_adventure;
		}

		@Override
		protected void onCancelled() {
			super.onCancelled();
		}

		@Override
		protected void onPostExecute(Adventure result) {
			super.onPostExecute(result);
			
			// Launch the adventure or output an error if a valid one isn't found
			
			if (result == null) {
				Global.outputError(FindAdventureActivity.this, Global.Error.NO_ADVENTURES_FOUND);
				finish();
			} else {
				Intent i = new Intent();
				result.setMode(params.getMode().charAt(0));
				i.putExtra(INTENT_ADVENTURE, result);
				i.putExtra(INTENT_ADVENTURE_PARAMS_COMPLETE, true);
				
				setResult(Activity.RESULT_OK, i);
				finish();
			}
		}

		@Override
		protected void onProgressUpdate(ADVENTURE_GET_STATUS... values) {
			super.onProgressUpdate(values);
			
			switch (values[0]) {
			case FINDING_ADVENTURES:
				tv_prompt.setText(getResources().getString(R.string.finding_adventures));
				break;
			case PREPARING_ADVENTURE:
				tv_prompt.setText(getResources().getString(R.string.preparing_adventure));
				break;
			}
		}
		
		/**
		 * Gets a list of adventures via the Google Places API
		 * @param params - the parameters used in choosing palces
		 * @return - an array of places
		 * @throws IOException - network error
		 * @throws JSONException - JSON parsing error
		 */
		Adventure[] getAdventures(AdventureParams params) throws IOException, JSONException {
	        HashMap<String, String> adventure_query_params = new HashMap<String, String>();
	        adventure_query_params.put(Global.Google.Places.Param.KEY, getResources().getString(R.string.google_api_key));
	        adventure_query_params.put(Global.Google.Places.Param.LOCATION, String.valueOf(params.getLat()) + "," + String.valueOf(params.getLng()));
	        adventure_query_params.put(Global.Google.Places.Param.RADIUS, String.valueOf(params.getRadius()));
	        adventure_query_params.put(Global.Google.Places.Param.SENSOR, "true");
	        
	        if (!params.getName().equals(""))
	        	adventure_query_params.put(Global.Google.Places.Param.NAME, params.getName());
	        
	        if (!params.getKeywords().equals(""))
	        	adventure_query_params.put(Global.Google.Places.Param.KEYWORD, params.getKeywords());
	        
	        if (!params.getTypes().equals(""))
	        	adventure_query_params.put(Global.Google.Places.Param.TYPES, params.getTypes());
	        
			BufferedReader adventure_query = HttpHelper.get(Global.Google.Places.BASE_URL, adventure_query_params);
			String adventure_query_result = Global.readAll(adventure_query);
			
			return parseAdventures(new JSONObject(adventure_query_result));
		}
    }
}