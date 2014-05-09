package es.uca.tabu;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.RatingBar;
import android.widget.TextView;

public class DefStatistics extends ActionBarActivity {

	static int ACTIVITY_NO = 4;

	TextView success, numReports;
	RatingBar rb;

	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);

		TabuUtils.hideActionBar(this);
		
		LayoutInflater inflater = LayoutInflater.from(this);
		View app = inflater.inflate(R.layout.activity_def_statistics, null);
		setContentView(app);
		setTitle("Activity " + ACTIVITY_NO);
		
		// Instance StatisticsManager
		StatisticsManager sm = StatisticsManager.getInstance(this);
		
		// Retrieve views
		success = (TextView) findViewById(R.id.ratio);
		numReports = (TextView) findViewById(R.id.reports);
		rb = (RatingBar) findViewById(R.id.ratingBar);
		
		// Initialize statistics
		float ratio = (float) sm.getIDefRate();
		ratio*=100;
				
		String coloredNumber = "<font color="+ TabuUtils.getPercentColor(ratio) +">" + String.format("%.2f",ratio) + "%</font> ";
		success.setText(Html.fromHtml(coloredNumber));
		numReports.setText(String.valueOf(sm.getINumOfReports()));
		rb.setRating((float) sm.getIAVGDefRating());
		
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
	}
}
