package es.uca.tabu;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import es.uca.tabu.utils.Environment;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.ScaleAnimation;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.GridView;
import android.widget.NumberPicker;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NavUtils;

public class PlayMenuActivity extends FragmentActivity implements NumberPicker.OnValueChangeListener {

	private static String KEY_QUESTIONS = "questions";
	private static String KEY_CATEGORIES = "info";

	NumberPicker np;
	NumberPicker lp;
	GridView gridview;
	View settings;
	View selectAll;
	View questionsLayout;
	Button selectAllBtn;
	Button startGameBtn;
	TextView title;
	
	Boolean toApply=true;

	Animation fadeIn;
	ImageCategoriesAdapter adapter;
	
	public class Category {
		public int id;
		public String name;
		private Map<Integer, Integer> questionsPerLevel = new HashMap<Integer, Integer>();
		
		public Category(int id, String name) {
			this.id = id;
			this.name = name;
		}
		
		public void addLevelInfo(Integer level, Integer numQuestions) {
			questionsPerLevel.put(level, numQuestions);
		}
		
		public int getNumOfQuestionsForLevel(Integer level) {
			Object numQuestions = questionsPerLevel.get(level);
			return (Integer) ((numQuestions != null) ? numQuestions : 0);
		}
		
		public int getTotal() {
			int total=0;
			for(int i : questionsPerLevel.values())
				total+=i;
			return total;
		}
	}
	
	List<Category> categoriesModel = new ArrayList<Category>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		TabuUtils.hideActionBar(this);
		setContentView(R.layout.activity_play_menu);
		//setupActionBar();

		selectAllBtn = (Button) findViewById(R.id.selectAllBtn);
		startGameBtn = (Button) findViewById(R.id.startBtn);
		title = (TextView) findViewById(R.id.title);

		settings = (View) findViewById(R.id.settings);
		np = (NumberPicker) findViewById(R.id.numberPicker);
		lp = (NumberPicker) findViewById(R.id.levelPicker);
		lp.setOnValueChangedListener(this);
		selectAll = (View) findViewById(R.id.selectAll);
		questionsLayout = (View) findViewById(R.id.questionsLayout);

		fadeIn = new AlphaAnimation(0, 1);
		fadeIn.setInterpolator(new DecelerateInterpolator());
		fadeIn.setDuration(1000);

		final Animation fadeOut = new AlphaAnimation(1, 0);
		fadeOut.setInterpolator(new AccelerateInterpolator());
		fadeOut.setStartOffset(100);
		fadeOut.setDuration(1000);

		final Animation scale = new ScaleAnimation(1, 1, 1, 0.5f);
		scale.setStartOffset(100);
		scale.setDuration(1000);

		gridview = (GridView) findViewById(R.id.gridview);

		//Set title font size
		title.setTextSize(TabuUtils.getFontSizeFromBounds(title.getText().toString(), 
				TabuUtils.dpToPx(Environment.getInstance().getScreenWidth()-150), 
				TabuUtils.dpToPx(Environment.getInstance().getScreenHeight()/7)));
		
		//Set as (un)checked all categories
		selectAllBtn.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				applyToAllInGridView(toApply);
				toApply=!toApply;
				updateQuestionsWheel(lp.getValue(), getCheckedCategories());
			} 
		});
	
		settings.setVisibility(View.INVISIBLE);

		//StartGameBtn height
		BitmapDrawable bd = (BitmapDrawable) getResources().getDrawable(R.drawable.start_game);
		int startBtnHeight = bd.getBitmap().getHeight();
		int lettersInfoHeight = ((TextView) findViewById(R.id.textView2)).getHeight();
		RelativeLayout.LayoutParams layoutParams = (android.widget.RelativeLayout.LayoutParams) np.getLayoutParams();
		layoutParams.height = startBtnHeight;
		np.setLayoutParams(layoutParams);

		layoutParams = (android.widget.RelativeLayout.LayoutParams) lp.getLayoutParams();
		layoutParams.height = startBtnHeight;
		lp.setLayoutParams(layoutParams);

		//np.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, startGameBtn.getHeight()));
		// Show settings layout
		np.setMinValue(1);
		np.setMaxValue(2);
		//Levels
		//lp.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, startGameBtn.getHeight()));
		lp.setMinValue(1);
		lp.setMaxValue(5);
		lp.setDisplayedValues(new String[] { "1", "2", "3", "4", PlayMenuActivity.this.getResources().getString(R.string.allLevels)});
		lp.setValue(5);

		//settings.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (height<<2), 2.5f));


		new CategoriesQuery().execute();
		new QuestionsQuery().execute(lp.getValue(), getCheckedCategories());

		startGameBtn.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				if(atLeastOneCategorySelected()) {
					if(np.getValue() > 0) {
						startGameBtn.setEnabled(false);
						Intent startGame = new Intent(getApplicationContext(), GameActivity.class);
						startGame.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
						startGame.putExtra("EXTRA_CHECKED_CATEGORIES", getCheckedCategories());
						startGame.putExtra("EXTRA_NUM_OF_QUESTIONS", np.getValue());
						startGame.putExtra("EXTRA_LEVEL", lp.getValue());
						startActivity(startGame);
						finish();
					}
					else {
						TabuUtils.showDialog(getResources().getString(R.string.error), getResources().getString(R.string.atLeastOneQuestion),PlayMenuActivity.this);
					}
				}
				else {
					TabuUtils.showDialog(getResources().getString(R.string.error), getResources().getString(R.string.atLeastOneCategory),PlayMenuActivity.this);
				}
			} 
		});
		
	}

		/*
		// When clicking PvsC button fade out the rest and place it in the top
		playerVsComputer.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				playerVsComputer.setEnabled(false);

				playerVsComputer.startAnimation(scale);
				playerVsPlayer.startAnimation(fadeOut);
				groupQueue.startAnimation(fadeOut);

				fadeOut.setAnimationListener(new AnimationListener() {
					public void onAnimationEnd(Animation animation) {
						//To avoid blinking
						playerVsComputer.clearAnimation();

						// Prepare dynamic settings layout dimensions
						int width = ViewGroup.LayoutParams.MATCH_PARENT;
						int height = (playerVsComputer.getHeight() >> 1);

						// Hide unused buttons
						playerVsPlayer.setVisibility(View.GONE);
						groupQueue.setVisibility(View.GONE);

						settings.setVisibility(View.INVISIBLE);

						// Keep playerVsComputer button dimensions
						playerVsComputer.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height));


						//StartGameBtn height
						BitmapDrawable bd = (BitmapDrawable) getResources().getDrawable(R.drawable.start_game);
						int startBtnHeight = bd.getBitmap().getHeight();
						int lettersInfoHeight = ((TextView) findViewById(R.id.textView2)).getHeight();
						RelativeLayout.LayoutParams layoutParams = (android.widget.RelativeLayout.LayoutParams) np.getLayoutParams();
						layoutParams.height = startBtnHeight;
						np.setLayoutParams(layoutParams);

						layoutParams = (android.widget.RelativeLayout.LayoutParams) lp.getLayoutParams();
						layoutParams.height = startBtnHeight;
						lp.setLayoutParams(layoutParams);


						//np.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, startGameBtn.getHeight()));
						// Show settings layout
						np.setMinValue(1);
						np.setMaxValue(2);
						//Levels
						//lp.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, startGameBtn.getHeight()));
						lp.setMinValue(1);
						lp.setMaxValue(5);
						lp.setDisplayedValues(new String[] { "1", "2", "3", "4", PlayMenuActivity.this.getResources().getString(R.string.allLevels)});
						lp.setValue(5);

						settings.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (height<<2), 2.5f));


						new CategoriesQuery().execute();
						new QuestionsQuery().execute(lp.getValue(), getCheckedCategories());

						startGameBtn.setOnClickListener(new View.OnClickListener() {
							public void onClick(View v) {
								if(atLeastOneCategorySelected()) {
									if(np.getValue() > 0) {
										startGameBtn.setEnabled(false);
										Intent startGame = new Intent(getApplicationContext(), GameActivity.class);
										startGame.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
										startGame.putExtra("EXTRA_CHECKED_CATEGORIES", getCheckedCategories());
										startGame.putExtra("EXTRA_NUM_OF_QUESTIONS", np.getValue());
										startGame.putExtra("EXTRA_LEVEL", lp.getValue());
										startActivity(startGame);
										finish();
									}
									else {
										TabuUtils.showDialog(getResources().getString(R.string.error), getResources().getString(R.string.atLeastOneQuestion),PlayMenuActivity.this);
									}
								}
								else {
									TabuUtils.showDialog(getResources().getString(R.string.error), getResources().getString(R.string.atLeastOneCategory),PlayMenuActivity.this);
								}
							} 
						});
					}

					@Override
					public void onAnimationRepeat(Animation animation) {
						// TODO Auto-generated method stub
					}

					@Override
					public void onAnimationStart(Animation animation) {
						// TODO Auto-generated method stub
					}

				});
			}
		});


		playerVsPlayer.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				playerVsPlayer.setEnabled(false);

				playerVsComputer.startAnimation(fadeOut);
				groupQueue.startAnimation(fadeOut);
				playerVsPlayer.startAnimation(scale);

				fadeOut.setAnimationListener(new AnimationListener() {
					public void onAnimationEnd(Animation animation) {
						//Get delta distance
						int[] Ypc = new int[2];
						int[] Ypp = new int[2];
						playerVsComputer.getLocationInWindow(Ypc);
						playerVsPlayer.getLocationInWindow(Ypp);
						int deltaY = Ypc[1] - Ypp[1];

						//Keep hide unused buttons
						playerVsComputer.setVisibility(View.INVISIBLE);
						groupQueue.setVisibility(View.INVISIBLE);

						// half button height
						final int height = playerVsPlayer.getHeight() >> 1;

						//Set final position for moved button
						LinearLayout.LayoutParams params = (LayoutParams) playerVsPlayer.getLayoutParams();
						params.leftMargin = Ypp[0];
						params.topMargin  = Ypc[1];
						playerVsPlayer.setLayoutParams(params);

						//-12 is hard-coded because I couldn't achieve it
						final Animation goUp = new TranslateAnimation(0, 0, 0, deltaY-12);
						goUp.setDuration(1000);

						// Keep playerVsPlayer button dimensions
						playerVsPlayer.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height));

						playerVsPlayer.startAnimation(goUp);

						goUp.setAnimationListener(new AnimationListener() {
							@Override
							public void onAnimationEnd(Animation animation) {
								//To avoid blinking
								playerVsPlayer.clearAnimation();

								// Make disappear other buttons from layout
								playerVsComputer.setVisibility(View.GONE);
								groupQueue.setVisibility(View.GONE);

								// Keep playerVsPlayer button dimensions
								playerVsPlayer.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height));

								np.setMinValue(1);
								np.setMaxValue(2);

								settings.setVisibility(View.INVISIBLE);
								settings.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (height<<2), 2.5f));

								new QuestionsQuery().execute();
								new CategoriesQuery().execute();

							}

							@Override
							public void onAnimationRepeat(Animation animation) {
								// TODO Auto-generated method stub

							}

							@Override
							public void onAnimationStart(Animation animation) {
								// TODO Auto-generated method stub

							}
						});
					}

					@Override
					public void onAnimationRepeat(Animation animation) {
						// TODO Auto-generated method stub
					}

					@Override
					public void onAnimationStart(Animation animation) {
						// TODO Auto-generated method stub
					}

				});
			}
		});

		groupQueue.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				groupQueue.setEnabled(false);
				playerVsComputer.startAnimation(fadeOut);
				playerVsPlayer.startAnimation(fadeOut);

				fadeOut.setAnimationListener(new AnimationListener() {
					public void onAnimationEnd(Animation animation) {
						//Keep hide unused buttons
						playerVsComputer.setVisibility(View.INVISIBLE);
						playerVsPlayer.setVisibility(View.INVISIBLE);

						// Keep button height
						final int height = groupQueue.getHeight();

						//Get delta distance
						int[] Ypc = new int[2];
						int[] Ypp = new int[2];
						playerVsComputer.getLocationInWindow(Ypc);
						groupQueue.getLocationInWindow(Ypp);
						int deltaY = Ypc[1] - Ypp[1];

						//Set final position for moved button
						LinearLayout.LayoutParams params = (LayoutParams) groupQueue.getLayoutParams();
						params.leftMargin = Ypp[0];
						params.topMargin  = Ypc[0];
						groupQueue.setLayoutParams(params);

						// Translate animation
						final TranslateAnimation translateAnimation = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 2, Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 2);  
						translateAnimation.setDuration(3000);  
						translateAnimation.setRepeatCount(3);

						final Animation goUp = new TranslateAnimation(0, 0, 0, deltaY);
						goUp.setDuration(1000);

						groupQueue.startAnimation(goUp);

						goUp.setAnimationListener(new AnimationListener() {
							@Override
							public void onAnimationEnd(Animation animation) {
								//To avoid blinking
								groupQueue.clearAnimation();

								// Make disappear other buttons from layout
								playerVsComputer.setVisibility(View.GONE);
								playerVsPlayer.setVisibility(View.GONE);

								// Keep playerVsPlayer button dimensions
								groupQueue.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height));
							}

							@Override
							public void onAnimationRepeat(Animation animation) {
								// TODO Auto-generated method stub

							}

							@Override
							public void onAnimationStart(Animation animation) {
								// TODO Auto-generated method stub

							}
						});
					}

					@Override
					public void onAnimationRepeat(Animation animation) {
						// TODO Auto-generated method stub
					}

					@Override
					public void onAnimationStart(Animation animation) {
						// TODO Auto-generated method stub
					}

				});
			}
		});

	}

	*/
	/**
	 * Set up the {@link android.app.ActionBar}.
	 */
	private void setupActionBar() {
		getActionBar().setDisplayHomeAsUpEnabled(true);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.play_menu, menu);
		return true;
	}


	@Override
	public void onResume() {
		super.onResume();
		TabuUtils.updateLanguage(this);
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

	// Obtiene el n�mero m�ximo de preguntas que hay para ese nivel y esas categor�as
	private class QuestionsQuery extends AsyncTask<Object, Object, JSONObject> {

		ProgressDialog dialog;

		@Override
		protected void onPreExecute() {
			dialog = ProgressDialog.show(PlayMenuActivity.this, " ", 
					getResources().getString(R.string.updating), true);
		}

		@Override
		protected JSONObject doInBackground(Object... level) {
			SharedPreferences loginPreferences;
			loginPreferences = getSharedPreferences("loginPrefs", MODE_PRIVATE);
			
			//If there is access to Internet
			if(ConnectionManager.getInstance(PlayMenuActivity.this).networkWorks()) {
				return ConnectionManager.getInstance().getMaxQuestions((Integer) level[0], (ArrayList<Integer>) level[1], loginPreferences.getString("language", ""));
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
					TabuUtils.showDialog(getResources().getString(R.string.error), getResources().getString(R.string.noNetwork),PlayMenuActivity.this);
				}
				if (!json.isNull(TabuUtils.KEY_SUCCESS)) {
					String res = json.getString(KEY_QUESTIONS);
					np.setMaxValue(Integer.parseInt(res));
					// Default value
					if(np.getMaxValue() > 0)
						np.setValue(1);
					dialog.cancel();
				}
				else {
					dialog.cancel();
					TabuUtils.showDialog(getResources().getString(R.string.error), getResources().getString(R.string.serverIssues),PlayMenuActivity.this);
				}
			} catch (JSONException e) {
				e.printStackTrace();
				dialog.cancel();
			}
		}
	}


	private class CategoriesQuery extends AsyncTask<Void, Void, JSONObject> {

		Toast toast = new Toast(PlayMenuActivity.this);
		ProgressDialog dialog;

		@Override
		protected void onPreExecute() {
			dialog = ProgressDialog.show(PlayMenuActivity.this, " ", 
					getResources().getString(R.string.updating), true);
		}

		@Override
		protected JSONObject doInBackground(Void... nothing) {
			
			SharedPreferences loginPreferences;
			loginPreferences = getSharedPreferences("loginPrefs", MODE_PRIVATE);
			
			//If there is access to Internet
			if(ConnectionManager.getInstance(PlayMenuActivity.this).networkWorks()) {
				return ConnectionManager.getInstance().getAllCategories(loginPreferences.getString("language", ""));
			}
			else
				return null;
		}

		@Override
		protected void onPostExecute(JSONObject json) {
			try {
				if(json == null) {
					dialog.dismiss();
					TabuUtils.showDialog(getResources().getString(R.string.error), getResources().getString(R.string.noNetwork),PlayMenuActivity.this);
				}
				if (!json.isNull(TabuUtils.KEY_SUCCESS)) {
					
					JSONObject categoriesInfo = json.getJSONObject(KEY_CATEGORIES);
					Iterator<?> keys = categoriesInfo.keys();
					
					//Populate categoriesModel
					while(keys.hasNext()) {
						String key = (String) keys.next();
						JSONObject categoryInfo = categoriesInfo.getJSONObject(key);
						System.out.println(categoryInfo.toString());
						Category c = new Category(Integer.valueOf(key), categoryInfo.getString("nombre"));
						for(int j=1; j<=4; j++) {
							try {
								c.addLevelInfo(j, categoryInfo.getInt(String.valueOf(j)));
							}
							catch(JSONException e) {
								System.out.println("There are no questions for level " + j + " in category " + key);
							}
						}
						categoriesModel.add(c);
					}
					
					adapter = new ImageCategoriesAdapter(PlayMenuActivity.this, categoriesModel);
					
					dialog.cancel();

					//adapter = new ImageCategoriesAdapter(PlayMenuActivity.this, parsedCategories, parsedIds);
					ViewGroup.LayoutParams glp = gridview.getLayoutParams();
					ViewGroup.LayoutParams settingsLp = settings.getLayoutParams();
					ViewGroup.LayoutParams selectAllLp = selectAllBtn.getLayoutParams();

					/*DisplayMetrics displaymetrics = new DisplayMetrics();
					getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
					int height = displaymetrics.heightPixels;*/
					int height = Environment.getInstance().getScreenHeight();

					int location[] = new int[2];
					selectAllBtn.getLocationOnScreen(location);
					glp.height = height - selectAllLp.height - location[1];

					gridview.setLayoutParams(glp);
					gridview.setAdapter(adapter);
					gridview.setOnItemClickListener(new OnItemClickListener() {
						public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
							if(position==0) { // Random categories selection
								applyToAllInGridView(false);
								// [3, MaxCategories]
								int numOfRandomCategories = (int) (3 + (Math.random() * (gridview.getCount()-1)));
								Integer[] NoOfCat = new Integer[numOfRandomCategories];
								for(int i=0; i< numOfRandomCategories; i++) {
									NoOfCat[i] = (int) (1 + (Math.random() * (gridview.getCount()-1)));
									MarkableImageView imageView = 
											(MarkableImageView) ((ImageCategoriesAdapter) gridview.getAdapter()).getView(NoOfCat[i], null, null);
									imageView.setChecked(true);
								}

							}
							else { // Mark selected category
								MarkableImageView miv = (MarkableImageView) v;
								miv.setChecked(!miv.isChecked());
								toast.cancel();
								toast = Toast.makeText(PlayMenuActivity.this, categoriesModel.get(position-1).name, Toast.LENGTH_SHORT);
								toast.show();
							}
							
							updateQuestionsWheel(lp.getValue(), getCheckedCategories());
						}
					});

					settings.startAnimation(fadeIn);
					fadeIn.setAnimationListener(new AnimationListener() {

						@Override
						public void onAnimationEnd(
								Animation animation) {
							// TODO Auto-generated method stub
							settings.setVisibility(View.VISIBLE);
						}

						@Override
						public void onAnimationRepeat(
								Animation animation) {
							// TODO Auto-generated method stub

						}

						@Override
						public void onAnimationStart(
								Animation animation) {
							// TODO Auto-generated method stub

						}
					});
					dialog.cancel();
				}
				else {
					dialog.cancel();
					TabuUtils.showDialog(getResources().getString(R.string.error), getResources().getString(R.string.serverIssues),PlayMenuActivity.this);
				}
			} catch (JSONException e) {
				e.printStackTrace();
				dialog.cancel();
			}
		}
	}

	private void applyToAllInGridView(Boolean checked) {
		ImageCategoriesAdapter ica = (ImageCategoriesAdapter) gridview.getAdapter();
		for(int i=1; i<gridview.getCount(); i++) {
			MarkableImageView imageView = (MarkableImageView) ica.getView(i,null,null);
			imageView.setChecked(checked);
		}
		// Display All or None according to checked value
		if(checked)
			selectAllBtn.setText(getResources().getString(R.string.none));
		else
			selectAllBtn.setText(getResources().getString(R.string.selectAll));
	}

	private ArrayList<Integer> getCheckedCategories() {
		ArrayList<Integer> checkedCategories = new ArrayList<Integer>();
		for(int i=1; i<gridview.getCount(); i++) {
			MarkableImageView imageView = (MarkableImageView) gridview.getAdapter().getView(i,null,null);
			if(imageView.isChecked())
				checkedCategories.add(imageView.getId());
		}
		return checkedCategories;
	}

	private void updateQuestionsWheel(Integer level, List<Integer> checkedCategories) {
		int total=0;
		
		for(Integer i : checkedCategories) {
			for(Category cat : categoriesModel) {
				if(i == cat.id) {
					if(level == 5) { //All
						total += cat.getTotal();
					}
					else {
						total += cat.getNumOfQuestionsForLevel(level);
					}
					break;
				}
			}
		}
		np.setMaxValue(total);
		if(np.getMaxValue() > 0)
			np.setValue(1);
	}
	
	private boolean atLeastOneCategorySelected() {
		for(int i=1; i<gridview.getCount(); i++) {
			MarkableImageView imageView = (MarkableImageView) gridview.getAdapter().getView(i,null,null);
			if(imageView.isChecked())
				return true;
		}
		return false;
	}


	@Override
	public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
		// TODO Auto-generated method stub
		updateQuestionsWheel(lp.getValue(), getCheckedCategories());

	}
}
