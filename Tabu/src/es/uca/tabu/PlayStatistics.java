package es.uca.tabu;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.TextView;

public class PlayStatistics extends ActionBarActivity {

	static int ACTIVITY_NO = 3;

	TextView success, total, bestCategory, worstCategory, level, levels;

	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		TabuUtils.hideActionBar(this);
		
		LayoutInflater inflater = LayoutInflater.from(this);
		View app = inflater.inflate(R.layout.activity_play_statistics, null);
		setContentView(app);
		setTitle("Activity " + ACTIVITY_NO);
		
		// Instance StatisticsManager
		StatisticsManager sm = StatisticsManager.getInstance(this);
		
		// Retrieve views
		success = (TextView) findViewById(R.id.ratio);
		total = (TextView) findViewById(R.id.jugados);
		bestCategory = (TextView) findViewById(R.id.mejorCategoria);
		worstCategory = (TextView) findViewById(R.id.peorCategoria);
		level = (TextView) findViewById(R.id.nivelJugado);
		levels = (TextView) findViewById(R.id.nivelesJugados);
		
		// Initialize statistics
		float ratio = (float) sm.getIRatio();
		ratio*=100;
		
		String coloredNumber = "<font color="+ TabuUtils.getPercentColor(ratio) +">" + String.format("%.2f",ratio) + "%</font> ";
		success.setText(Html.fromHtml(coloredNumber));
		total.setText(String.valueOf(sm.getIPlayed()));
		bestCategory.setText(sm.getIBestCat());
		worstCategory.setText(sm.getIWorstCat());
		level.setText(String.valueOf(sm.getIMostPlayedLevel()));
		levels.setText(sm.getIPlayedLevels());
		
		// Header button to show left menu
		ViewGroup tabBar = (ViewGroup) app.findViewById(R.id.tabBar);

		tabBar.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				showMenu();
			}
		});

	}

	
	
	@Override
	public void onBackPressed() {
		Intent mainmenu = new Intent(getApplicationContext(), IndividualStatistics.class);
		mainmenu.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(mainmenu);
		/**
		 * Close Login Screen
		 **/
		finish();
	}

	private void showMenu() {
		Bitmap viewCapture = null;
		View rootView = findViewById(android.R.id.content).getRootView();
		rootView.setDrawingCacheEnabled(true);
		viewCapture = Bitmap.createBitmap(rootView.getDrawingCache());
		rootView.setDrawingCacheEnabled(false);

		if (viewCapture != null)
			StatisticsActivity.clfs.setCache(viewCapture);

		// mark an index for the activity
		StatisticsActivity.clfs.performClick(ACTIVITY_NO);
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		overridePendingTransition(0, 0);
		TabuUtils.updateLanguage(this);
	}
}
