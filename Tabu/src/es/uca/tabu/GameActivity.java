package es.uca.tabu;

import java.util.ArrayList;

import org.json.JSONObject;

import com.google.common.base.Function;

import es.uca.tabu.utils.Environment;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.annotation.SuppressLint;
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
import android.view.View.MeasureSpec;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.Spinner;
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
	//private FrameLayout rememberBox;
	private TextView rememberInside; 

	private TextView prepalabra;
	private TextView postpalabra;
	private TextView time;
	private EditText palabra;

	BalloonHint bh = null;

	RatingBar rb;
	boolean rated; // To make rating optional
	float lastRating=0;

	ProgressDialog dialog;

	//TabuCountDownTimer timerCount;
	float fontSize;

	@Override
	public void onDestroy()
	{
		if(bh != null){
			bh.needForceDismiss();
			bh.delayedDismiss(0);
		}

		super.onDestroy();
	}

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
		//rememberBox = (FrameLayout) findViewById(R.id.rememberBox);
		prepalabra = (TextView) findViewById(R.id.prepalabra);
		palabra = (EditText) findViewById(R.id.palabra);
		postpalabra = (TextView) findViewById(R.id.postpalabra);
		time = (TextView) findViewById(R.id.timer);
		rb = (RatingBar) findViewById(R.id.ratingBar);
		
		fontSize = new TextView(this).getTextSize();
		
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
						editalert.setTitle(getResources().getString(R.string.report));
						editalert.setMessage(getResources().getString(R.string.reportReason));
						final Spinner reason = new Spinner(GameActivity.this);
						LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
								LinearLayout.LayoutParams.MATCH_PARENT,
								LinearLayout.LayoutParams.MATCH_PARENT);
						reason.setLayoutParams(lp);
						editalert.setView(reason);
						final ArrayList<String> reasons = new ArrayList<String>();
						TabuUtils.fillReportReasons(GameActivity.this, reasons);
						ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(GameActivity.this,
								android.R.layout.simple_spinner_item, reasons);
						dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
						reason.setAdapter(dataAdapter);

						editalert.setPositiveButton("Report", new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int whichButton) {
								gameManager.getCurrentQuestion().setReport(String.valueOf(reasons.indexOf(reason.getSelectedItem().toString())));
								requestNextQuestion();
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
					checkAnswer(finalWord);
				}
			} 
		});
	}

	public void checkAnswer(String finalWord) {
		final Question current = gameManager.getCurrentQuestion();
		current.increaseTries();
		if(finalWord.compareTo("") != 0 && gameManager.validWord(current.getId(), finalWord)) {
			submit.setChecked(true);
			current.setSuccess(true);
			if(rated)
				current.setPuntuacion((int) rb.getRating());
			requestNextQuestion();
		}
		else {
			String message;
			int timeToClose = Integer.MAX_VALUE;

			if(current.getTries() != GameManager.MAX_TRIES) {
				message = getResources().getString(R.string.wrongAnswer1) + " - " + String.valueOf(current.getTries()) + "/" + String.valueOf(GameManager.MAX_TRIES);
				timeToClose = 5;
			}
			else {
				message = getResources().getString(R.string.wrongAnswer2);
			}

			if(!(this.isFinishing()))
			{
				// Avisa del fallo y cuando OK o cambia de pregunta o reintenta
				TabuUtils.showImageTimedDialog(" ", message,
						new Function<DialogInterface, Void>() {
					@Override
					public Void apply(DialogInterface arg0) {
						arg0.dismiss();

						if(rated)
							current.setPuntuacion((short) rb.getRating());

						if(current.getTries() == GameManager.MAX_TRIES) {
							current.setSuccess(false);
							requestNextQuestion();
						}
						else {
							submit.setEnabled(true);
							palabra.setText("");
							/*timerCount = new TabuCountDownTimer(gameManager.getTime() * 1000, 1000);
							timerCount.start();*/
							time.setTextColor(Color.BLACK);
							time.setText(String.valueOf(gameManager.getTime()));
						}
						time.setTextSize(fontSize);
						return null;
					} 
				},
				GameActivity.this,
				R.drawable.reject,
				timeToClose);
			}
		}

	}

	public void requestNextQuestion(){
		final Question current = gameManager.next();
		if(current != null) {
			rb.setRating(0);
			rated = false;
			palabra.setText("");
			clue.setText("");

			/*timerCount = new TabuCountDownTimer(gameManager.getTime() * 1000, 1000);
			timerCount.start();
			time.setText(String.valueOf(gameManager.getTime()));
			time.setTextColor(Color.BLACK);
			time.setTextSize(fontSize);*/

			rememberBox.setVisibility(View.GONE);

			// PREPALABRA - PALABRA - POSTPALABRA STUFF
			prepalabra.setText(current.getPrepalabra());
			postpalabra.setText(current.getPostpalabra());

			// Get left margin in dp
			int margins = TabuUtils.pxToDp(20);

			//lastLine + word to guess... fits in textView?
			int start = prepalabra.getLayout().getLineStart(prepalabra.getLineCount()-1);
			int end = prepalabra.getLayout().getLineEnd(prepalabra.getLineCount()-1);
			String lastLine = prepalabra.getText().toString().substring(start,end);
			String lastLineFilled = lastLine + "    " + current.getName(); // 4 extra characters because of the editText interface

			//float lineHeight = prepalabra.getLineHeight() * prepalabra.getLineSpacingMultiplier() + prepalabra.getLineSpacingExtra();
			float lineHeight = prepalabra.getLineHeight();
			if(TabuUtils.isTooLarge(prepalabra, lastLineFilled)){
				//New line
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
				showRememberArticle();
			}

			prepalabra.setTextIsSelectable(true);
			postpalabra.setTextIsSelectable(true);
			submit.setEnabled(true);
			clue.setEnabled(true);
			clue.setChecked(false);
			submit.setChecked(false);

			// Rate definition ballonhint!
			showRateTip();
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

	private void showRememberArticle() {
		ViewTreeObserver vto = rememberBox.getViewTreeObserver();
		vto.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
			@SuppressLint("NewApi")
			@Override
			public void onGlobalLayout() {
				final Question current = gameManager.getCurrentQuestion();
				int margins = TabuUtils.pxToDp(20);

				// Creates a remember textview and place it at the right-top corner of rememberbox
				remember = (TextView) findViewById(R.id.remember);
				rememberInside = (TextView) findViewById(R.id.rememberInside);

				// Display remember! message on top-right corner of rememberBox
				// width and height of remember box
				int width = rememberBox.getWidth();
				int height = rememberBox.getHeight();
				
				//remember dimensions	
				int max_height1 = TabuUtils.dpToPx((int) (height*0.18));
				int max_width1 = TabuUtils.dpToPx((int) (width*0.177));

				remember.setTextSize(TabuUtils.getFontSizeFromBounds(remember.getText().toString(), max_width1, max_height1));
				
				//Display remember contain
				final int max_height2 = height - max_height1;
				final int max_width2 = (int) (width - margins);
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
				
				ViewTreeObserver obs = rememberBox.getViewTreeObserver();

				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
					obs.removeOnGlobalLayoutListener(this);
				} else {
					obs.removeGlobalOnLayoutListener(this);
				}
			}
		});
	}

	private void showRateTip() {
		// Rate definition ballonhint!
		ViewTreeObserver vto = rb.getViewTreeObserver();
		vto.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
			@SuppressLint("NewApi")
			@Override
			public void onGlobalLayout() {
				int width = rb.getWidth();
				int height = rb.getHeight();

				bh = new BalloonHint(GameActivity.this, rb, MeasureSpec.AT_MOST);
				int location[] = new int[] {15,height};

				bh.setBalloonConfig(getString(R.string.rateDef), 
						TabuUtils.getFontSizeFromBounds(getString(R.string.rateDef), width, height), 
						false, Color.WHITE, width, height);
				bh.setBackgroundDrawable(getResources().getDrawable(R.drawable.bocadillo));
				bh.delayedShow(10, location);
				bh.delayedDismiss(7000);

				ViewTreeObserver obs = rb.getViewTreeObserver();

				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
					obs.removeOnGlobalLayoutListener(this);
				} else {
					obs.removeGlobalOnLayoutListener(this);
				}
			}
		});
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
		ProgressDialog dialog;

		@Override
		protected void onPreExecute() {
			dialog = ProgressDialog.show(GameActivity.this, " ", 
					getResources().getString(R.string.sending), true);
		}

		@Override
		protected JSONObject doInBackground(Object... questionList) {

			SharedPreferences loginPreferences;
			loginPreferences = getSharedPreferences("loginPrefs", MODE_PRIVATE);

			//If there is access to Internet
			if(ConnectionManager.getInstance(GameActivity.this).networkWorks()) {
				return ConnectionManager.getInstance().storeStadistics(
						loginPreferences.getInt("id", -1),
						(ArrayList<Question>)questionList[0]);
			}
			else
				return null;
		}

		// Informa al usuario de lo sucedido
		@Override
		protected void onPostExecute(JSONObject json) {
			/**
			 * Checks for success message.
			 **/
			if(json == null) {
				dialog.dismiss();
				submit.setEnabled(true);
				TabuUtils.showDialog(getResources().getString(R.string.error), getResources().getString(R.string.noNetwork),GameActivity.this);
			}
			else if (!json.isNull(TabuUtils.KEY_SUCCESS)) {
				dialog.cancel();
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
				dialog.cancel();
				TabuUtils.showDialog(getResources().getString(R.string.error), getResources().getString(R.string.errorQuestions),GameActivity.this);
				submit.setEnabled(true);
				clue.setEnabled(true);
				clue.setChecked(false);
				submit.setChecked(false);
			}
		}
	}

	private class TabuCountDownTimer extends CountDownTimer {
		public TabuCountDownTimer(long millisInFuture, long countDownInterval) {
			super(millisInFuture, countDownInterval);
		}

		@Override
		public void onFinish() {
			time.setText("00");
			String finalWord = palabra.getText().toString();
			checkAnswer(finalWord);
		}
		@Override
		public void onTick(long millisUntilFinished) {
			time.setText(String.format("%02d", millisUntilFinished / 1000));
			if(Integer.valueOf(time.getText().toString()) == 5) {
				time.setTextColor(Color.RED);
				time.setTextSize((float) (fontSize*1.25));
			}
		}   
	}
}
