package es.uca.tabu;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;

public class StatisticsActivity extends Activity {

	GridView gridView; 
	ArrayList<Item> gridArray = new ArrayList<Item>(); 
	MenuAdapter customGridAdapter;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_statistics);

		//set grid view item 
		Bitmap lastGameIcon = BitmapFactory.decodeResource(this.getResources(), R.drawable.lastgame);

		gridArray.add(new Item(lastGameIcon,getString(R.string.lastGame)));

		gridView = (GridView) findViewById(R.id.gridView1);

		customGridAdapter = new MenuAdapter(this, R.layout.row_grid, gridArray); 
		gridView.setAdapter(customGridAdapter);
		gridView.setOnItemClickListener(new OnItemClickListener() { 
			@Override public void onItemClick(AdapterView<?> arg0, View v, int position, long arg3) {
				GameManager gamemanager = GameManager.getInstance(StatisticsActivity.this);
				if(gamemanager.initializeFromSharedPreferences()) {
					Intent conclusion = new Intent(getApplicationContext(), ResultActivity.class);
					conclusion.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					startActivity(conclusion);
					finish();
				}
				else 
					TabuUtils.showDialog(getString(R.string.error), getString(R.string.nolastgame), StatisticsActivity.this);
			} });


	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.statistics, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

}
