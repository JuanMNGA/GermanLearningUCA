package es.uca.tabu;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.common.base.Function;

import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;
import android.support.v4.app.NavUtils;

public class GameActivity extends Activity implements RatingBar.OnRatingBarChangeListener {

	private GameManager gameManager;

	private TextView definition;
	private MarkableButton submit;
	private MarkableButton clue;
	private MarkableButton dictionary;
	private EditText word;
	private TextView article;

	RatingBar rb;
	boolean rated; // To make rating optional
	float lastRating=0;

	ProgressDialog dialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_game);

		submit = (MarkableButton)findViewById(R.id.submit);
		definition = (TextView)findViewById(R.id.definition);
		word = (EditText) findViewById(R.id.word);
		clue = (MarkableButton) findViewById(R.id.pista);
		dictionary = (MarkableButton) findViewById(R.id.dictionary);
		article = (TextView) findViewById(R.id.article);

		rb = (RatingBar) findViewById(R.id.ratingBar);
		rated = false;
		rb.setOnRatingBarChangeListener(this);
		rb.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_UP) {
					float touchPositionX = event.getX();
					float width = rb.getWidth();
					float starsf = (touchPositionX / width) * 5.0f;
					int stars = (int)starsf + 1;

					if(stars == lastRating) {
						rb.setRating(0.0f);
						// Report dialog
						AlertDialog.Builder editalert = new AlertDialog.Builder(GameActivity.this);
						editalert.setTitle("Report");
						editalert.setMessage("Enter report reason here:");
						final EditText input = new EditText(GameActivity.this);
						LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
						        LinearLayout.LayoutParams.MATCH_PARENT,
						        LinearLayout.LayoutParams.MATCH_PARENT);
						input.setLayoutParams(lp);
						editalert.setView(input);

						editalert.setPositiveButton("Report", new DialogInterface.OnClickListener() {
						    public void onClick(DialogInterface dialog, int whichButton) {
						    	gameManager.getCurrentQuestion().setReport(input.getText().toString());
						    }
						});


						editalert.show();
					}
					else
						rb.setRating(stars);
						
					lastRating = rb.getRating();

					//Toast.makeText(MainActivity.this, String.valueOf("test"), Toast.LENGTH_SHORT).show();                   
					v.setPressed(false);
				}
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					v.setPressed(true);
				}

				if (event.getAction() == MotionEvent.ACTION_CANCEL) {
					v.setPressed(false);
				}
				return true;
			}});



		/*
		rb.setOnTouchListener(new OnTouchListener() {
		    @Override
		    public boolean onTouch(View v, MotionEvent event) {
		        if (event.getAction() == MotionEvent.ACTION_UP) {
		            // TODO perform your action here
					if(rb.getRating() == lastRating)
						rb.setRating(0.0f);

					lastRating = rb.getRating();
		        }
		        return false;
		    }
		});*/

		// Show the Up button in the action bar.
		setupActionBar();

		ArrayList<Integer> categories;
		int questions;
		int level;

		Bundle extras = getIntent().getExtras();
		if(extras != null) {
			categories = extras.getIntegerArrayList("EXTRA_CHECKED_CATEGORIES");
			questions = extras.getInt("EXTRA_NUM_OF_QUESTIONS");
			level = extras.getInt("EXTRA_LEVEL");
			gameManager = GameManager.getInstance(this);
			gameManager.setNumOfQuestions(questions);
			gameManager.setCategories(categories);
			gameManager.setLevel(level);
			gameManager.initialize();
		}
		else
		{
			System.out.println("NO CATEGORIES SELECTED");
		}

		clue.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Question current = gameManager.getCurrentQuestion();
				if((current.getArticle() != null && !current.getArticle().isEmpty()) && !current.isClue()) {
					clue.setChecked(true);
					article.setVisibility(View.VISIBLE);
					article.setText(getString(R.string.articleword) + current.getArticle());
					current.setClue(true);
				}
			} 
		});

		dictionary.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				int startSelection = definition.getSelectionStart();
				int endSelection = definition.getSelectionEnd();
				System.out.println("START: " + String.valueOf(startSelection) + ", " + "END: " + String.valueOf(endSelection));

				if(startSelection != endSelection) {
					String selectedText = definition.getText().toString().substring(startSelection, endSelection);
					if(!selectedText.contains(" ")) {
						Toast.makeText(GameActivity.this, selectedText + " added", Toast.LENGTH_SHORT)
						.show();
					}
					else {
						Toast.makeText(GameActivity.this, "You have selected more than one word", Toast.LENGTH_SHORT)
						.show();
					}
				}
				else {
					Toast.makeText(GameActivity.this,"No text selected", Toast.LENGTH_SHORT)
					.show();
				}
			} 
		});

		submit.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {

				String finalWord = word.getText().toString();
				if(finalWord.length() != 0) {
					submit.setEnabled(false);
					if(ConnectionManager.getInstance(GameActivity.this).networkWorks()) {

						final Question current = gameManager.getCurrentQuestion();
						current.increaseTries();
						if(gameManager.validWord(current.getId(), finalWord)) {
							submit.setChecked(true);
							//System.out.println("FUNCIONA");
							current.setSuccess(true);
							if(rated)
								current.setPuntuacion((int) rb.getRating());
							requestNextQuestion();
						}
						else {
							//System.out.println("NO FUNCIONA");

							String message;

							if(current.getTries() != GameManager.MAX_TRIES) {
								message = getResources().getString(R.string.wrongAnswer1) + " - " + String.valueOf(current.getTries()) + "/" + String.valueOf(GameManager.MAX_TRIES);
							}
							else {
								message = getResources().getString(R.string.wrongAnswer2);
							}
							// Avisa del fallo y cuando OK o cambia de pregunta o reintenta
							TabuUtils.showImageDialog(" ", message,
									new Function<DialogInterface, Void>() {
								@Override
								public Void apply(DialogInterface arg0) {
									arg0.cancel();

									if(rated)
										current.setPuntuacion((short) rb.getRating());

									if(current.getTries() == GameManager.MAX_TRIES) {
										current.setSuccess(false);
										requestNextQuestion();
									}
									else {
										submit.setEnabled(true);
										word.setText("");
									}
									return null;
								} 
							},
							GameActivity.this,
							R.drawable.reject);
						}
					}
					else {
						TabuUtils.showDialog(getResources().getString(R.string.error), getResources().getString(R.string.noNetwork),GameActivity.this);
						submit.setEnabled(true);
					}
				}
			} 
		});
	}

	public void requestNextQuestion(){
		Question current = gameManager.next();

		if(current != null) {
			rb.setRating(0);
			rated = false;
			word.setText("");
			clue.setText("");

			if((current.getArticle() != null && !current.getArticle().isEmpty())) {
				//article.setText(getString(R.string.articleword) + current.getArticle());
				article.setText(current.getArticle());
			}

			definition.setText(current.getDefinition());
			definition.setTextIsSelectable(true);
			submit.setEnabled(true);
			clue.setEnabled(true);
			clue.setChecked(false);
			submit.setChecked(false);
		}
		else {

			new SendStadistics().execute(gameManager.getQuestions());
		}
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
		getMenuInflater().inflate(R.menu.game, menu);
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

	@Override
	public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
		// TODO Auto-generated method stub
		rated = true;
	}

	private class SendStadistics extends AsyncTask<Object, Boolean, JSONObject> {

		// Devuelve true si consigue meter el usuario en la base de datos
		@Override
		protected JSONObject doInBackground(Object... questionList) {

			SharedPreferences loginPreferences;
			SharedPreferences.Editor loginPrefsEditor;
			loginPreferences = getSharedPreferences("loginPrefs", MODE_PRIVATE);
			//loginPrefsEditor = loginPreferences.edit();

			return ConnectionManager.getInstance().storeStadistics(
					loginPreferences.getInt("id", -1),
					(ArrayList<Question>)questionList[0]);
		}

		// Informa al usuario de lo sucedido
		@Override
		protected void onPostExecute(JSONObject json) {
			/**
			 * Checks for success message.
			 **/
			if (!json.isNull(TabuUtils.KEY_SUCCESS)) {

				TabuUtils.showDialog(" ", getString(R.string.OkStadistics),
						new Function<DialogInterface, Void>() { //Function to switch to ResultActivity when dialog button clicked
					@Override
					public Void apply(DialogInterface arg0) {
						arg0.cancel();

						Intent conclusion = new Intent(getApplicationContext(), ResultActivity.class);
						conclusion.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
						startActivity(conclusion);
						finish();
						return null;
					} 
				},
				GameActivity.this);

			}
			else {
				TabuUtils.showDialog(getResources().getString(R.string.error), getResources().getString(R.string.errorQuestions),GameActivity.this);
				submit.setEnabled(true);
				clue.setEnabled(true);
				clue.setChecked(false);
				submit.setChecked(false);
			}
		}
	}

}
