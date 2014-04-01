package es.uca.tabu;

import java.util.ArrayList;

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
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;
import android.support.v4.app.NavUtils;

public class GameActivity extends Activity implements RatingBar.OnRatingBarChangeListener {

	private GameManager gameManager;

	private MarkableButton submit;
	private MarkableButton clue;
	private MarkableButton dictionary;
	private TextView remember;
	private LinearLayout rememberBox;
	private TextView rememberInside; 

	private TextView prepalabra;
	private TextView postpalabra;
	private EditText palabra;

	RatingBar rb;
	boolean rated; // To make rating optional
	float lastRating=0;

	ProgressDialog dialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//Hide Action bar
		getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
		getActionBar().hide();
		setContentView(R.layout.activity_game);

		submit = (MarkableButton)findViewById(R.id.submit);
		clue = (MarkableButton) findViewById(R.id.pista);
		dictionary = (MarkableButton) findViewById(R.id.dictionary);
		rememberBox = (LinearLayout) findViewById(R.id.rememberBox);

		prepalabra = (TextView) findViewById(R.id.prepalabra);
		palabra = (EditText) findViewById(R.id.palabra);
		postpalabra = (TextView) findViewById(R.id.postpalabra);

		// Rating bar
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
								if(input.getText().toString() != "")
									gameManager.getCurrentQuestion().setReport(input.getText().toString());
								else
									TabuUtils.showDialog(
											getResources().getString(R.string.error), 
											getResources().getString(R.string.noReason),
											GameActivity.this);
							}
						});
						editalert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int whichButton) {
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
		//setupActionBar();

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
				clue.setChecked(true);
				TabuUtils.showDialog("Clue" , current.getClue(),GameActivity.this);				
				current.setClue(true);
			} 
		});

		dictionary.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				int startSelection = prepalabra.getSelectionStart();
				int endSelection = prepalabra.getSelectionEnd();

				SharedPreferences loginPreferences;
				loginPreferences = getSharedPreferences("loginPrefs", MODE_PRIVATE);
				
				if(startSelection != endSelection) {
					String selectedText = prepalabra.getText().toString().substring(startSelection, endSelection);
					if(!selectedText.contains(" ")) {
						gameManager.addWordToBloc(loginPreferences.getInt("id", -1), selectedText);
					}
					else {
						Toast.makeText(GameActivity.this, getString(R.string.oneword), Toast.LENGTH_SHORT)
						.show();
					}
				}
				else {
					startSelection = postpalabra.getSelectionStart();
					endSelection = postpalabra.getSelectionEnd();

					if(startSelection != endSelection) {
						String selectedText = postpalabra.getText().toString().substring(startSelection, endSelection);
						if(!selectedText.contains(" ")) {
							gameManager.addWordToBloc(loginPreferences.getInt("id", -1), selectedText);
						}
						else {
							Toast.makeText(GameActivity.this, getString(R.string.oneword), Toast.LENGTH_SHORT)
							.show();
						}
					} 
					else {
						Toast.makeText(GameActivity.this,getString(R.string.noText), Toast.LENGTH_SHORT)
						.show();
					}
				}
			} 
		});

		submit.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {

				String finalWord = palabra.getText().toString();
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
										palabra.setText("");
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
		final Question current = gameManager.next();

		if(current != null) {
			rb.setRating(0);
			rated = false;
			palabra.setText("");
			clue.setText("");
			rememberBox.setVisibility(View.GONE);

			// PREPALABRA - PALABRA - POSTPALABRA STUFF
			prepalabra.setText(current.getPrepalabra());
			postpalabra.setText(current.getPostpalabra());
			
			// Get left margin in dp
			int margins = TabuUtils.pxToDp(this, 20);
			
			//lastLine + word to guess... fits in textView?
			int start = prepalabra.getLayout().getLineStart(prepalabra.getLineCount()-1);
			int end = prepalabra.getLayout().getLineEnd(prepalabra.getLineCount()-1);
			String lastLine = prepalabra.getText().toString().substring(start,end);
			String lastLineFilled = lastLine + "    " + current.getName(); // 4 extra characters because of the editText interface
			float lineHeight = prepalabra.getLineHeight() * prepalabra.getLineSpacingMultiplier() + prepalabra.getLineSpacingExtra();
			if(TabuUtils.isTooLarge(prepalabra, lastLineFilled)){
				//New line
				System.out.println("NUEVA LINEA");
				FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
				params.setMargins(margins, (int) ((int) (lineHeight * (prepalabra.getLineCount())) - (palabra.getHeight()/4) + (lineHeight/4)), 0, 0);
				palabra.setLayoutParams(params);

				// Place postlabra at the bottom
				FrameLayout.LayoutParams params2 = new FrameLayout.LayoutParams(postpalabra.getMeasuredWidth(), LayoutParams.WRAP_CONTENT);
				params2.setMargins(margins, (int) ((int) (lineHeight * (prepalabra.getLineCount()+1)) + (lineHeight/4)), 0, 0);
				postpalabra.setLayoutParams(params2);
				
			}
			else {
				// Same Line
				float lastLineWidth = prepalabra.getPaint().measureText(lastLine);
				int spaceLeft = (int) (prepalabra.getMeasuredWidth() - lastLineWidth);
				FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(spaceLeft, LayoutParams.WRAP_CONTENT);
				/*int factor;
				if(prepalabra.getLineCount() == 1) {
					factor = 0;
				}
				else {
					factor = (int) (lineHeight * (prepalabra.getLineCount()-1));
				}
				 */
				params.setMargins((int) lastLineWidth + margins, (int) ((int) (lineHeight * (prepalabra.getLineCount()-1)) - (palabra.getHeight()/4) + (lineHeight/4)), 0, 0);
				palabra.setLayoutParams(params);

				// Place postlabra at the bottom
				FrameLayout.LayoutParams params2 = new FrameLayout.LayoutParams(postpalabra.getMeasuredWidth(), LayoutParams.WRAP_CONTENT);
				params2.setMargins(margins, (int) (lineHeight * (prepalabra.getLineCount()) + (lineHeight/4)), 0, 0);
				postpalabra.setLayoutParams(params2);
			}
			
			// Check if there is an article
			if((current.getArticle() != null && !current.getArticle().isEmpty())) {
				rememberBox.setVisibility(View.VISIBLE);
				System.out.println("Tiene artículo");
				//article.setText(getString(R.string.articleword) + current.getArticle());
				//article.setText(current.getArticle());

				// Creates a remember textview and place it at the right-top corner of rememberbox
				//rememberBox.setGravity(Gravity.RIGHT);

				remember = (TextView) findViewById(R.id.remember);
				rememberInside = (TextView) findViewById(R.id.rememberInside);

				// Display remember! message on top-right corner of rememberBox
				// width and height of remember box
				BitmapDrawable bd = (BitmapDrawable) this.getResources().getDrawable(R.drawable.remember);
				int width = bd.getBitmap().getWidth();
				int height = bd.getBitmap().getHeight();
				//remember dimensions
				int max_height1 = (int) (height*0.23);
				int max_width1 = (int) (width*0.076);
				remember.setTextSize(TabuUtils.getFontSizeFromBounds(remember.getText().toString(), max_width1, max_height1));

				//Display remember contain
				final int max_height2 = 40;
				final int max_width2 = (int) (width*0.5 - margins);
				rememberInside.setText(current.getArticle());
				rememberInside.setTextSize(TabuUtils.getFontSizeFromBounds(rememberInside.getText().toString(), max_width2, max_height2));
				rememberInside.setEllipsize(null);
				rememberInside.setTextColor(Color.parseColor(TabuUtils.getArticleColor(current.getArticle())));

				//Listener to add the input of the user: "DER WORD"
				palabra.addTextChangedListener(new TextWatcher() {

					@Override
					public void beforeTextChanged(CharSequence s, int start,
							int count, int after) {
						// TODO Auto-generated method stub

					}

					@Override
					public void onTextChanged(CharSequence s, int start,
							int before, int count) {
						// TODO Auto-generated method stub

					}

					@Override
					public void afterTextChanged(Editable s) {
						// TODO Auto-generated method stub
						String text;
						text = current.getArticle() + " " + s.toString();
						String formattedText = "<font color=" + TabuUtils.getArticleColor(current.getArticle()) + ">" + current.getArticle() + " </font> <font color=#000000>" + s.toString() + "</font>";
						rememberInside.setText(Html.fromHtml(formattedText));
						rememberInside.setTextSize(TabuUtils.getFontSizeFromBounds(text, max_width2, max_height2));

					}

				});

			}

			prepalabra.setTextIsSelectable(true);
			postpalabra.setTextIsSelectable(true);
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
