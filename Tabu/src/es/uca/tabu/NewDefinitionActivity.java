package es.uca.tabu;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.common.base.Function;

import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.Spinner;
import android.widget.TextView;
import android.support.v4.app.NavUtils;

public class NewDefinitionActivity extends Activity {

	Spinner category=null;
	NumberPicker np;
	Button send;
	TextView article;
	TextView word;
	TextView definition;
	TextView hint;
	
	ArrayList<String> parsedCategories;
	ArrayList<Integer> parsedIds;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_new_definition);
		// Show the Up button in the action bar.
		setupActionBar();
		
		article = (TextView) findViewById(R.id.article);
		word = (TextView) findViewById(R.id.writeNewDef);
		definition = (TextView) findViewById(R.id.DefinitionBody);
		hint = (TextView) findViewById(R.id.HintBody);
		
		send = (Button) findViewById(R.id.btnSend);
		
		np = (NumberPicker) findViewById(R.id.levelPicker);
		np.setMinValue(1);
		np.setMaxValue(4);
		np.setValue(1);
		
		//Initialize categories
		new CategoriesQuery().execute();
		
		//If click send
		send.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				//Comprobar artículos según idioma actual
				
				if(!(word.getText().toString().isEmpty() &&
					definition.getText().toString().isEmpty() &&
					hint.getText().toString().isEmpty() &&
					category!=null)) {
					
					new sendNewDefinition().execute();
					
				}
			} 
		});
	}

	/**
	 * Set up the {@link android.app.ActionBar}.
	 */
	private void setupActionBar() {

		getActionBar().setDisplayHomeAsUpEnabled(true);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.new_definition, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			// This ID represents the Home or Up button. In the case of this
			// activity, the Up button is shown. Use NavUtils to allow users
			// to navigate up one level in the application structure. For
			// more details, see the Navigation pattern on Android Design:
			//
			// http://developer.android.com/design/patterns/navigation.html#up-vs-back
			//
			NavUtils.navigateUpFromSameTask(this);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private class CategoriesQuery extends AsyncTask<Void, Void, JSONObject> {
		
		@Override
		protected JSONObject doInBackground(Void... nothing) {
			return ConnectionManager.getInstance().getAllCategories();
		}

		@Override
		protected void onPostExecute(JSONObject json) {
			try {
				if (!json.isNull(TabuUtils.KEY_SUCCESS)) {
					parsedCategories = new ArrayList<String>();
					parsedIds = new ArrayList<Integer>();
					JSONArray categories = json.getJSONArray(TabuUtils.KEY_CATEGORIES);
					JSONArray ids = json.getJSONArray(TabuUtils.KEY_IDS);
					for(int i=0; i<categories.length(); i++) {
						parsedCategories.add((String) categories.get(i));
						parsedIds.add(ids.getInt(i));
					}
					
					category = (Spinner) findViewById(R.id.spinner1);
					
					//Add items to spinner
					ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(NewDefinitionActivity.this,
							android.R.layout.simple_spinner_item, parsedCategories);
					dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
					category.setAdapter(dataAdapter);
					
				}
				else {
					TabuUtils.showDialog(getResources().getString(R.string.error), getResources().getString(R.string.serverIssues), NewDefinitionActivity.this);
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}
	
	private class sendNewDefinition extends AsyncTask<Void, Void, JSONObject> {
		
		@Override
		protected JSONObject doInBackground(Void... nothing) {
			
			SharedPreferences loginPreferences;
			loginPreferences = getSharedPreferences("loginPrefs", MODE_PRIVATE);
			
			return ConnectionManager.getInstance().sendDefinition(
					loginPreferences.getInt("id", -1),
					article.getText().toString(),
					word.getText().toString(),
					definition.getText().toString(),
					hint.getText().toString(),
					np.getValue(),
					parsedIds.get(parsedCategories.indexOf(category.getSelectedItem().toString())));
		}

		@Override
		protected void onPostExecute(JSONObject json) {
			if (!json.isNull(TabuUtils.KEY_SUCCESS)) {

				String res;
				try {
					res = json.getString(TabuUtils.KEY_SUCCESS);
					if(Integer.parseInt(res) == 1){
						TabuUtils.showDialog(" ", getString(R.string.ok),
								new Function<DialogInterface, Void>() { 
							@Override
							public Void apply(DialogInterface arg0) {
								arg0.cancel();
		
								Intent conclusion = new Intent(getApplicationContext(), MainMenuActivity.class);
								conclusion.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
								startActivity(conclusion);
								finish();
								return null;
							} 
						},
						NewDefinitionActivity.this);
					}
					else {
						TabuUtils.showDialog(getResources().getString(R.string.error), word.getText().toString() + " " + getResources().getString(R.string.exists), NewDefinitionActivity.this);
					}
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					System.out.println("Error JSON al recibir la info");
					e.printStackTrace();
				}
			}
			else {
				TabuUtils.showDialog(getResources().getString(R.string.error), getResources().getString(R.string.errorNewDef), NewDefinitionActivity.this);
			}
		}
	}
	
}
