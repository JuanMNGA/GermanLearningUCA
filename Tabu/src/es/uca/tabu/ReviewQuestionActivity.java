package es.uca.tabu;

import java.util.ArrayList;

import org.json.JSONObject;

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
import android.text.Html;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.View.OnTouchListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class ReviewQuestionActivity extends Activity implements RatingBar.OnRatingBarChangeListener {

	Button backBtn, dictionaryBtn;
	TextView definition;

	private TextView remember;
	private LinearLayout rememberBox;
	private TextView rememberInside; 

	RatingBar rb;
	boolean rated; // To make rating optional
	float lastRating=0;

	Question q;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//Hide Action bar
		getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
		getActionBar().hide();
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
						new SendReport().execute(q.getId(), rb.getRating(), q.getReport());
					}
					else {
						backToResults();
					}
				} 
			});

			definition = (TextView) findViewById(R.id.definition);
			rememberBox = (LinearLayout) findViewById(R.id.rememberBox);

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
						float starsf = (touchPositionX / width) * 5.0f;
						int stars = (int)starsf + 1;
						
						if(stars == lastRating) {
							rb.setRating(0.0f);
							
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
							
							
							/*final EditText input = new EditText(ReviewQuestionActivity.this);
							LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
									LinearLayout.LayoutParams.MATCH_PARENT,
									LinearLayout.LayoutParams.MATCH_PARENT);
							input.setLayoutParams(lp);
							editalert.setView(input);*/

							editalert.setPositiveButton(getResources().getString(R.string.report), new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int whichButton) {
									q.setReport(String.valueOf(reasons.indexOf(reason.getSelectedItem().toString())));
								}
							});
							editalert.setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
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

			// Check if there is an article
			if((q.getArticle() != null && !q.getArticle().isEmpty())) {
				// Get left margin in dp
				int margins = TabuUtils.pxToDp(this, 20);
				rememberBox.setVisibility(View.VISIBLE);
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
				rememberInside.setTextSize(TabuUtils.getFontSizeFromBounds(text, max_width2, max_height2));
			}
			//Notepad button
			dictionaryBtn = (Button) findViewById(R.id.dictionary);
			dictionaryBtn.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					int startSelection = definition.getSelectionStart();
					int endSelection = definition.getSelectionEnd();

					if(startSelection != endSelection) {
						String selectedText = definition.getText().toString().substring(startSelection, endSelection);
						if(!selectedText.contains(" ")) {
							Toast.makeText(ReviewQuestionActivity.this, selectedText + " " + getString(R.string.added), Toast.LENGTH_SHORT)
							.show();
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
		}
		else
		{
			System.out.println("NO EXTRAS AT REVIEW");
		}

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

}
