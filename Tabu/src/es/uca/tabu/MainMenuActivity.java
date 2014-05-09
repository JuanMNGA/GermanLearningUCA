package es.uca.tabu;

import java.util.ArrayList;
import java.util.Collections;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.support.v4.app.NavUtils;

public class MainMenuActivity extends Activity {

	private static String KEY_PLAYSTATISTICS = "play";
	private static String KEY_DEFSTATISTICS = "def";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_menu);

		StatisticsManager.reset();
		
		Button playMenu = (Button) findViewById(R.id.btnPlay);
		playMenu.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				Intent i = new Intent(getApplicationContext(), PlayMenuActivity.class);
				startActivity(i);
			}
		});

		Button newDef = (Button) findViewById(R.id.btnNewDef);
		newDef.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				new getWordToDef().execute();
			}
		});

		Button dictionary = (Button) findViewById(R.id.btnDictionary);

		dictionary.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				new initializeNotes().execute();
			}
		});
		
		Button statistics = (Button) findViewById(R.id.btnStats);
		statistics.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				new initializeStatistics().execute();
			}
		});
		
	}

	@Override
	public void onBackPressed() {
		Intent mainmenu = new Intent(getApplicationContext(), LoginActivity.class);
		mainmenu.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(mainmenu);
		/**
		 * Close Login Screen
		 **/
		finish();
	}
	
	private class initializeNotes extends AsyncTask<Object, Boolean, JSONObject> {

		private ArrayList<String> mItems;

		ProgressDialog dialog;

		@Override
		protected void onPreExecute() {
			dialog = ProgressDialog.show(MainMenuActivity.this, " ", 
					getResources().getString(R.string.updating), true);
		}

		// Devuelve true si consigue meter el usuario en la base de datos
		@Override
		protected JSONObject doInBackground(Object... user) {

			SharedPreferences loginPreferences;
			loginPreferences = getSharedPreferences("loginPrefs", MODE_PRIVATE);

			if(ConnectionManager.getInstance(MainMenuActivity.this).networkWorks()) {
				return ConnectionManager.getInstance().getNotes(
						loginPreferences.getInt("id", -1));
			}
			else
				return null;
		}

		// Informa al usuario de lo sucedido
		@Override
		protected void onPostExecute(JSONObject json) {
			try {
				if(json == null) {
					dialog.dismiss();
					TabuUtils.showDialog(getResources().getString(R.string.error), getResources().getString(R.string.noNetwork),MainMenuActivity.this);
				}
				else if (!json.isNull(TabuUtils.KEY_SUCCESS)) {
					JSONArray notes;

					notes = json.getJSONArray("notes");
					mItems = new ArrayList<String>();
					if(notes.length() > 0) {
						for(int i=0; i < notes.length(); i++) {
							mItems.add(notes.getString(i));
						}
						Collections.sort(mItems);
					}
					dialog.dismiss();
					Intent i = new Intent(getApplicationContext(), DictionaryActivity.class);
					i.putExtra("EXTRA_WORDS", mItems);
					startActivity(i);
				}
				else {
					dialog.dismiss();
					TabuUtils.showDialog(getResources().getString(R.string.error), getResources().getString(R.string.serverIssues),MainMenuActivity.this);
				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				dialog.dismiss();
				e.printStackTrace();
			}
		}

	}
	
	private class getWordToDef extends AsyncTask<Object, Boolean, JSONObject> {
		ProgressDialog dialog;

		@Override
		protected void onPreExecute() {
			dialog = ProgressDialog.show(MainMenuActivity.this, " ", 
					getResources().getString(R.string.loading), true);
		}

		// Devuelve true si consigue meter el usuario en la base de datos
		@Override
		protected JSONObject doInBackground(Object... user) {

			SharedPreferences loginPreferences;
			loginPreferences = getSharedPreferences("loginPrefs", MODE_PRIVATE);

			if(ConnectionManager.getInstance(MainMenuActivity.this).networkWorks()) {
				return ConnectionManager.getInstance().getWordToDef(
						loginPreferences.getInt("id", -1), loginPreferences.getString("language", ""));
			}
			else
				return null;
		}

		// Informa al usuario de lo sucedido
		@Override
		protected void onPostExecute(JSONObject json) {
			try {
				if(json == null) {
					dialog.dismiss();
					TabuUtils.showDialog(getResources().getString(R.string.error), getResources().getString(R.string.noNetwork),MainMenuActivity.this);
				}
				else if (!json.isNull(TabuUtils.KEY_SUCCESS)) {

					if(json.getInt(TabuUtils.KEY_SUCCESS) == 2) {
						dialog.dismiss();
						TabuUtils.showDialog(" ", getResources().getString(R.string.nowordsleft),MainMenuActivity.this);
					}
					else {
						dialog.dismiss();
						
						String nombre = json.getString("nombre");
						String articulo = json.getString("articulo");
						String categoria = json.getString("categoria");
						int dificultad = json.getInt("dificultad");
						
						Intent i = new Intent(getApplicationContext(), NewDefinitionActivity.class);
						i.putExtra("EXTRA_NAME", nombre);
						i.putExtra("EXTRA_ARTICLE", articulo);
						i.putExtra("EXTRA_CATEGORY", categoria);
						i.putExtra("EXTRA_LEVEL", dificultad);
						startActivity(i);
					}
				}
				else {
					dialog.dismiss();
					TabuUtils.showDialog(getResources().getString(R.string.error), getResources().getString(R.string.serverIssues),MainMenuActivity.this);
				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				dialog.dismiss();
				e.printStackTrace();
			}
		}

	}

	private class initializeStatistics extends AsyncTask<Object, Boolean, JSONObject> {
		ProgressDialog dialog;

		@Override
		protected void onPreExecute() {
			dialog = ProgressDialog.show(MainMenuActivity.this, " ", 
					getResources().getString(R.string.loading), true);
		}

		// Devuelve true si consigue meter el usuario en la base de datos
		@Override
		protected JSONObject doInBackground(Object... user) {

			SharedPreferences loginPreferences;
			loginPreferences = getSharedPreferences("loginPrefs", MODE_PRIVATE);

			if(ConnectionManager.getInstance(MainMenuActivity.this).networkWorks()) {
				return ConnectionManager.getInstance().getStatistics(
						loginPreferences.getInt("id", -1), loginPreferences.getString("language", ""));
			}
			else
				return null;
		}

		// Informa al usuario de lo sucedido
		@Override
		protected void onPostExecute(JSONObject json) {
			try {
				if(json == null) {
					dialog.dismiss();
					TabuUtils.showDialog(getResources().getString(R.string.error), getResources().getString(R.string.noNetwork),MainMenuActivity.this);
				}
				else if (!json.isNull(TabuUtils.KEY_SUCCESS)) {
					
					StatisticsManager sm = StatisticsManager.getInstance(MainMenuActivity.this);
					
					JSONObject jsonPlayStatistics  = json.getJSONObject(KEY_PLAYSTATISTICS);
					sm.setIRatio(jsonPlayStatistics.getDouble("ratio"));
					sm.setIWorstCat(jsonPlayStatistics.getString("peorcategoria"));
					sm.setIMostPlayedLevel(jsonPlayStatistics.getInt("nivelmasjugado"));
					sm.setIBestCat(jsonPlayStatistics.getString("mejorcategoria"));
					sm.setIPlayed(jsonPlayStatistics.getInt("jugadas"));
					
					JSONObject jsonDefStatistics  = json.getJSONObject(KEY_DEFSTATISTICS);
					sm.setIAVGDefRating(jsonDefStatistics.getDouble("estrellas"));
					sm.setIDefRate(jsonDefStatistics.getDouble("ratio"));
					sm.setINumOfReports(jsonDefStatistics.getInt("reports"));
					
					dialog.dismiss();
					Intent result = new Intent(getApplicationContext(), StatisticsActivity.class);
					result.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					startActivity(result);
					finish();
				}
				else {
					dialog.dismiss();
					TabuUtils.showDialog(getResources().getString(R.string.error), getResources().getString(R.string.serverIssues),MainMenuActivity.this);
				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				dialog.dismiss();
				e.printStackTrace();
			}
		}

	}
}
