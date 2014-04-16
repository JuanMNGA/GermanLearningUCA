package es.uca.tabu;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.common.base.Function;

import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.app.ProgressDialog;
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
	Spinner article;
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

		article = (Spinner) findViewById(R.id.article);
		word = (TextView) findViewById(R.id.writeNewDef);
		definition = (TextView) findViewById(R.id.DefinitionBody);
		hint = (TextView) findViewById(R.id.HintBody);

		send = (Button) findViewById(R.id.btnSend);

		np = (NumberPicker) findViewById(R.id.levelPicker);
		np.setMinValue(1);
		np.setMaxValue(4);
		np.setValue(1);

		//Add articles to spinner depending on the language
		ArrayList<String> articles = new ArrayList<String> ();
		articles.add("");
		fillArticles(articles);
		Collections.sort(articles);


		ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(NewDefinitionActivity.this,
				android.R.layout.simple_spinner_item, articles);
		dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		article.setAdapter(dataAdapter);

		//Initialize categories
		new CategoriesQuery().execute();

		//If click send
		send.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {				

				//Validate data before sending it
				if(word.getText().toString().isEmpty()) {
					TabuUtils.showDialog(getResources().getString(R.string.error), getResources().getString(R.string.emptyWord),NewDefinitionActivity.this);
				}
				else if(definition.getText().toString().isEmpty()) {
					TabuUtils.showDialog(getResources().getString(R.string.error), getResources().getString(R.string.emptyDefinition),NewDefinitionActivity.this);
				}
				else if(hint.getText().toString().isEmpty()) {
					TabuUtils.showDialog(getResources().getString(R.string.error), getResources().getString(R.string.emptyHint),NewDefinitionActivity.this);
				}
				else if(category==null) {
					TabuUtils.showDialog(getResources().getString(R.string.error), getResources().getString(R.string.invalidCategory),NewDefinitionActivity.this);
				}
				else if(definition.getText().toString().compareTo(word.getText().toString()) == 0) {
					TabuUtils.showDialog(getResources().getString(R.string.error), getResources().getString(R.string.wordEqDef),NewDefinitionActivity.this);
				}
				else {
					TabuUtils.showConfirmDialog(" ", getResources().getString(R.string.sure),
							new Function<DialogInterface, Void>() {
						@Override
						public Void apply(DialogInterface arg0) {
							send.setEnabled(false);
							arg0.cancel();
							new sendNewDefinition().execute();
							return null;
						} 
					},
					NewDefinitionActivity.this);
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

	private void fillArticles(ArrayList<String> art) {
		Locale current = getResources().getConfiguration().locale;
		if(current == Locale.GERMAN) {
			art.add("die");
			art.add("der");
			art.add("die PL.");
			art.add("das");
		}
	}

	private class CategoriesQuery extends AsyncTask<Void, Void, JSONObject> {
		ProgressDialog dialog;

		@Override
		protected void onPreExecute() {
			dialog = ProgressDialog.show(NewDefinitionActivity.this, " ", 
					getResources().getString(R.string.updating), true);
		}

		@Override
		protected JSONObject doInBackground(Void... nothing) {
			//If there is access to Internet
			if(ConnectionManager.getInstance(NewDefinitionActivity.this).networkWorks()) {
				return ConnectionManager.getInstance().getAllCategories();
			}
			else
				return null;
		}

		@Override
		protected void onPostExecute(JSONObject json) {
			try {
				if(json == null) {
					dialog.dismiss();
					TabuUtils.showDialog(getResources().getString(R.string.error), getResources().getString(R.string.noNetwork),NewDefinitionActivity.this);
				}
				else if (!json.isNull(TabuUtils.KEY_SUCCESS)) {
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
					dialog.dismiss();
				}
				else {
					dialog.dismiss();
					TabuUtils.showDialog(getResources().getString(R.string.error), getResources().getString(R.string.serverIssues), NewDefinitionActivity.this);
				}
			} catch (JSONException e) {
				dialog.dismiss();
				e.printStackTrace();
			}
		}
	}

	private class sendNewDefinition extends AsyncTask<Void, Void, JSONObject> {
		ProgressDialog dialog;

		@Override
		protected void onPreExecute() {
			dialog = ProgressDialog.show(NewDefinitionActivity.this, " ", 
					getResources().getString(R.string.sending), true);
		}

		@Override
		protected JSONObject doInBackground(Void... nothing) {

			SharedPreferences loginPreferences;
			loginPreferences = getSharedPreferences("loginPrefs", MODE_PRIVATE);

			String prepalabra;
			String postpalabra;
			String definitionStr = definition.getText().toString().trim();
			String wordStr = word.getText().toString().trim();

			if(definitionStr.contains(wordStr)) {
				prepalabra = "";
				postpalabra= "";
				String[] parts = definitionStr.split(wordStr);

				if(TabuUtils.beginsBy(wordStr, definitionStr)) {
					prepalabra = "";
					postpalabra = parts[1];
				}
				else if(TabuUtils.endsBy(wordStr, definitionStr)) {
					postpalabra = "";
					prepalabra = parts[0];
				}
				else { // Est� por el medio de la frase
					prepalabra = parts[0];
					postpalabra = parts[1];
				}

			}
			else {
				prepalabra = definition.getText().toString();
				postpalabra = "";
			}

			//If there is access to Internet
			if(ConnectionManager.getInstance(NewDefinitionActivity.this).networkWorks()) {
				return ConnectionManager.getInstance().sendDefinition(
						loginPreferences.getInt("id", -1),
						article.getSelectedItem().toString(),
						word.getText().toString(),
						prepalabra,
						postpalabra,
						hint.getText().toString(),
						np.getValue(),
						parsedIds.get(parsedCategories.indexOf(category.getSelectedItem().toString())));
			}
			else
				return null;
		}

		@Override
		protected void onPostExecute(JSONObject json) {
			try {
				if(json == null) {
					dialog.dismiss();
					TabuUtils.showDialog(getResources().getString(R.string.error), getResources().getString(R.string.noNetwork),NewDefinitionActivity.this);
				}
				else if (!json.isNull(TabuUtils.KEY_SUCCESS)) {
					String res;
					res = json.getString(TabuUtils.KEY_SUCCESS);
					if(Integer.parseInt(res) == 1){
						dialog.dismiss();
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
						dialog.dismiss();
						TabuUtils.showDialog(getResources().getString(R.string.error), word.getText().toString() + " " + getResources().getString(R.string.exists), NewDefinitionActivity.this);
					}
				}
				else {
					dialog.dismiss();
					TabuUtils.showDialog(getResources().getString(R.string.error), getResources().getString(R.string.errorNewDef), NewDefinitionActivity.this);
				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				dialog.dismiss();
				System.out.println("Error JSON al recibir la info");
				e.printStackTrace();
			}
		}
	}

}
