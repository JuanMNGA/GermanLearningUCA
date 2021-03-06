package es.uca.tabu;

import java.util.ArrayList;

import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.text.Html;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.View.OnTouchListener;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class ReviewQuestionActivity extends Activity implements RatingBar.OnRatingBarChangeListener {

	Button backBtn, dictionaryBtn, audioBtn;
	TextView definition;

	private TextView remember;
	private LinearLayout rememberBox;
	private TextView rememberInside; 
	private Button reportBtn;

	RatingBar rb;
	boolean rated; // To make rating optional
	float lastRating=0;

	Question q;

	AlertDialog dialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//Hide Action bar
		TabuUtils.hideActionBar(this);
		setContentView(R.layout.activity_review_question);

		rated = false;

		Bundle extras = getIntent().getExtras();
		q = null;
		if(extras != null) {
			q = (Question) extras.getSerializable("EXTRA_QUESTION");

			backBtn = (Button) findViewById(R.id.backToMenu);
			backBtn.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {			
					if(rated) {
						new SendReport().execute(q.getId(), (int)rb.getRating(), q.getReport());
					}
					else {
						backToResults();
					}
				} 
			});

			definition = (TextView) findViewById(R.id.definition);
			rememberBox = (LinearLayout) findViewById(R.id.rememberBox);
			reportBtn = (Button) findViewById(R.id.reportBtn);

			String color;
			if(q.isSuccess()) {
				color = "#006400";
			}
			else {
				color = "#FF0000";
			}
			String coloredWord = "<font color=#000000>" + q.getPrepalabra() + " </font> ";
			coloredWord += "<font color=" + color + ">" + q.getName() + " </font> ";
			coloredWord += "<font color=#000000>" + q.getPostpalabra() + " </font> ";
			definition.setText(Html.fromHtml(coloredWord));

			audioBtn = (Button) findViewById(R.id.audio);
			audioBtn.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {		
					//if(GameManager.getInstance(ReviewQuestionActivity.this).getTTS() != null)
					GameManager.getInstance(ReviewQuestionActivity.this).getTTS().speak(definition.getText().toString(), TextToSpeech.QUEUE_FLUSH, null);
				} 
			});

			// Rating bar
			rb = (RatingBar) findViewById(R.id.ratingBar);
			rated = false;

			if(q.getPuntuacion() != null)
				rb.setRating(q.getPuntuacion());

			rb.setOnRatingBarChangeListener(this);
			rb.setOnTouchListener(new OnTouchListener() {
				@Override
				public boolean onTouch(View v, MotionEvent event) {
					if (event.getAction() == MotionEvent.ACTION_UP) {
						float touchPositionX = event.getX();
						float width = rb.getWidth();
						float starsf = (touchPositionX / width) * rb.getNumStars();
						int stars = (int)starsf + 1;
						
						rb.setRating(stars);
						
						rated = true;
					}
					return true;
				}
			});

			reportBtn.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					// Report dialog
					AlertDialog.Builder editalert = new AlertDialog.Builder(ReviewQuestionActivity.this);
					editalert.setTitle(getResources().getString(R.string.report));
					editalert.setMessage(getResources().getString(R.string.reportReason));
					final Spinner reason = new Spinner(ReviewQuestionActivity.this);
					LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
							LinearLayout.LayoutParams.MATCH_PARENT,
							LinearLayout.LayoutParams.MATCH_PARENT);
					reason.setLayoutParams(lp);
					editalert.setView(reason);
					final ArrayList<String> reasons = new ArrayList<String>();
					TabuUtils.fillReportReasons(ReviewQuestionActivity.this, reasons);
					ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(ReviewQuestionActivity.this,
							android.R.layout.simple_spinner_item, reasons);
					dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
					reason.setAdapter(dataAdapter);

					editalert.setPositiveButton("Report", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int whichButton) {
							q.setReport(String.valueOf(reasons.indexOf(reason.getSelectedItem().toString())));
						}
					});
					editalert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int whichButton) {
						}
					});


					editalert.show();
				}
			});
			
			// Check if there is an article
			if((q.getArticle() != null && !q.getArticle().isEmpty()) && !q.getArticle().contains("null")) {
				// Get left margin in dp
				rememberBox.setVisibility(View.VISIBLE);
				showRememberArticle();

				/*
				System.out.println("Tiene art�culo");
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
				rememberInside.setText(q.getArticle());
				rememberInside.setTextSize(TabuUtils.getFontSizeFromBounds(rememberInside.getText().toString(), max_width2, max_height2));
				rememberInside.setEllipsize(null);
				rememberInside.setTextColor(Color.parseColor(TabuUtils.getArticleColor(q.getArticle())));

				//Apply color and size
				String text;
				text = q.getArticle() + " " + q.getName();
				String formattedText = "<font color=" + TabuUtils.getArticleColor(q.getArticle()) + ">" + q.getArticle() + " </font> <font color=#000000>" + q.getName() + "</font>";
				rememberInside.setText(Html.fromHtml(formattedText));
				rememberInside.setTextSize(TabuUtils.getFontSizeFromBounds(text, max_width2, max_height2));*/
			}
			//Notepad button
			dictionaryBtn = (Button) findViewById(R.id.dictionary);
			dictionaryBtn.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					int startSelection = definition.getSelectionStart();
					int endSelection = definition.getSelectionEnd();

					SharedPreferences loginPreferences;
					loginPreferences = getSharedPreferences("loginPrefs", MODE_PRIVATE);
					GameManager gm = GameManager.getInstance(ReviewQuestionActivity.this);
					
					if(startSelection != endSelection) {
						String selectedText = definition.getText().toString().substring(startSelection, endSelection);
						if(!selectedText.contains(" ")) {
							gm.addWordToBloc(loginPreferences.getInt("id", -1), selectedText);
							/*Toast.makeText(ReviewQuestionActivity.this, selectedText + " " + getString(R.string.added), Toast.LENGTH_SHORT)
							.show();*/
						}
						else {
							Toast.makeText(ReviewQuestionActivity.this, getString(R.string.oneword), Toast.LENGTH_SHORT)
							.show();
						}
					}
					else {
						Toast.makeText(ReviewQuestionActivity.this,getString(R.string.noText), Toast.LENGTH_SHORT)
						.show();
					}
				} 
			});


			//dialog = ProgressDialog.show(ReviewQuestionActivity.this, " ", 
			//		getResources().getString(R.string.updating), true);

		}
		else
		{
			System.out.println("NO EXTRAS AT REVIEW");
		}

	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}


	@Override
	public void onBackPressed() {
		Intent mainmenu = new Intent(getApplicationContext(), ResultActivity.class);
		mainmenu.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(mainmenu);
		/**
		 * Close Login Screen
		 **/
		finish();
	}


	@Override
	public void onResume() {
		super.onResume();
		TabuUtils.updateLanguage(this);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.review_question, menu);
		return true;
	}

	@Override
	public void onRatingChanged(RatingBar ratingBar, float rating,
			boolean fromUser) {
		// TODO Auto-generated method stub
		rated = true;
	}

	private void backToResults() {
		Intent result = new Intent(getApplicationContext(), ResultActivity.class);
		result.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(result);
		finish();
	}

	private class SendReport extends AsyncTask<Object, Boolean, JSONObject> {
		ProgressDialog dialog;

		@Override
		protected void onPreExecute() {
			dialog = ProgressDialog.show(ReviewQuestionActivity.this, " ", 
					getResources().getString(R.string.sending), true);
		}

		@Override
		protected JSONObject doInBackground(Object... info) {

			SharedPreferences loginPreferences;
			loginPreferences = getSharedPreferences("loginPrefs", MODE_PRIVATE);

			//If there is access to Internet
			if(ConnectionManager.getInstance(ReviewQuestionActivity.this).networkWorks()) {
				return ConnectionManager.getInstance().sendReport(
						loginPreferences.getInt("id", -1),
						((Integer) info[0]),
						((Integer) info[1]),
						((String)info[2]));
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
				TabuUtils.showDialog(getResources().getString(R.string.error), getResources().getString(R.string.noNetwork),ReviewQuestionActivity.this);
			}
			else if (!json.isNull(TabuUtils.KEY_SUCCESS)) {
				dialog.dismiss();
				backToResults();
			}
			else {
				dialog.dismiss();
				TabuUtils.showDialog(getResources().getString(R.string.error), getResources().getString(R.string.errorReason),ReviewQuestionActivity.this);
			}
		}
	}

	private void showRememberArticle() {
		ViewTreeObserver vto = rememberBox.getViewTreeObserver();
		vto.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
			@SuppressLint("NewApi")
			@Override
			public void onGlobalLayout() {
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
				//final int max_height2 = TabuUtils.dpToPx((int)height - max_height1*4);
				final int max_height2 = 40;
				final int max_width2 = TabuUtils.dpToPx((int) (width - margins));
				rememberInside.setText(q.getArticle());
				rememberInside.setTextSize(TabuUtils.getFontSizeFromBounds(rememberInside.getText().toString(), max_width2, max_height2));
				rememberInside.setEllipsize(null);
				rememberInside.setTextColor(Color.parseColor(TabuUtils.getArticleColor(q.getArticle())));

				//Apply color and size
				String text;
				text = q.getArticle() + " " + q.getName();
				String formattedText = "<font color=" + TabuUtils.getArticleColor(q.getArticle()) + ">" + q.getArticle() + " </font> <font color=#000000>" + q.getName() + "</font>";
				rememberInside.setText(Html.fromHtml(formattedText));
				rememberInside.setTextSize(TabuUtils.getFontSizeFromBounds(text, max_width2, max_height2));

				ViewTreeObserver obs = rememberBox.getViewTreeObserver();

				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
					obs.removeOnGlobalLayoutListener(this);
				} else {
					obs.removeGlobalOnLayoutListener(this);
				}
			}
		});
	}

}
