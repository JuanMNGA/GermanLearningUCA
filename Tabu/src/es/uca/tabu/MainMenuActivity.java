package es.uca.tabu;

import java.util.ArrayList;
import java.util.Collections;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.support.v4.app.NavUtils;

public class MainMenuActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_menu);
		// Show the Up button in the action bar.
		setupActionBar();
		
		Button playMenu = (Button) findViewById(R.id.btnPlay);

		// Listening to register new account link
		playMenu.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				// Switching to Register screen
				Intent i = new Intent(getApplicationContext(), PlayMenuActivity.class);
				startActivity(i);
			}
		});
		
		Button newDef = (Button) findViewById(R.id.btnNewDef);

		// Listening to register new account link
		newDef.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				// Switching to Register screen
				Intent i = new Intent(getApplicationContext(), NewDefinitionActivity.class);
				startActivity(i);
			}
		});
		
		Button dictionary = (Button) findViewById(R.id.btnDictionary);

		// Listening to register new account link
		dictionary.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				// Switching to Register screen
				new initializeNotes().execute();
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
		getMenuInflater().inflate(R.menu.main_menu, menu);
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
	
	private class initializeNotes extends AsyncTask<Object, Boolean, JSONObject> {

		private ArrayList<String> mItems;
		
		// Devuelve true si consigue meter el usuario en la base de datos
		@Override
		protected JSONObject doInBackground(Object... user) {

			SharedPreferences loginPreferences;
			loginPreferences = getSharedPreferences("loginPrefs", MODE_PRIVATE);

			return ConnectionManager.getInstance().getNotes(
					loginPreferences.getInt("id", -1));
		}

		// Informa al usuario de lo sucedido
		@Override
		protected void onPostExecute(JSONObject json) {
			/**
			 * Checks for success message.
			 **/
			if (!json.isNull(TabuUtils.KEY_SUCCESS)) {
				JSONArray notes;
				try {
					notes = json.getJSONArray("notes");
					mItems = new ArrayList<String>();
					if(notes.length() > 0) {
						for(int i=0; i < notes.length(); i++) {
							mItems.add(notes.getString(i));
						}
						Collections.sort(mItems);
					}
					Intent i = new Intent(getApplicationContext(), DictionaryActivity.class);
					i.putExtra("EXTRA_WORDS", mItems);
					startActivity(i);

				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
			else {
				TabuUtils.showDialog(getResources().getString(R.string.error), getResources().getString(R.string.serverIssues),MainMenuActivity.this);
			}
		}
	}

}
