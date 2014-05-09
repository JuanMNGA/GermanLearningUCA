package es.uca.tabu;

import java.util.Locale;

import es.uca.tabu.utils.Environment;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.ImageView;

public class LanguageSelectionActivity extends Activity {
	
	ImageView uk, germany;

	private SharedPreferences loginPreferences;
	private SharedPreferences.Editor loginPrefsEditor;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.select_language);
		
		/* Temporalmente en alem�n */
		final Resources res = this.getResources();
		// Change locale settings in the app.
		final DisplayMetrics dm = res.getDisplayMetrics();
		final android.content.res.Configuration conf = res.getConfiguration();
		
		loginPreferences = getSharedPreferences("loginPrefs", MODE_PRIVATE);
		loginPrefsEditor = loginPreferences.edit();
		
		//Store screensize
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		Environment.getInstance().setScreenHeight(dm.heightPixels);
		Environment.getInstance().setScreenWidth(dm.widthPixels);
		Environment.getInstance().setDensity(dm.density);
		
		uk = (ImageView) findViewById(R.id.uk);
		germany = (ImageView) findViewById(R.id.germany);
		
		uk.setOnClickListener(new View.OnClickListener() {
			   //@Override
			   public void onClick(View v) {
					conf.locale = Locale.UK;
					res.updateConfiguration(conf, dm);
					
					loginPrefsEditor.putString("language", conf.locale.toString());
					loginPrefsEditor.commit();

					Intent mainmenu = new Intent(getApplicationContext(), LoginActivity.class);
					mainmenu.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					startActivity(mainmenu);
					
			   }        
			});
		
		germany.setOnClickListener(new View.OnClickListener() {
			   //@Override
			   public void onClick(View v) {
					conf.locale = Locale.GERMAN;
					res.updateConfiguration(conf, dm);      
					
					loginPrefsEditor.putString("language", conf.locale.toString());
					loginPrefsEditor.commit();

					Intent mainmenu = new Intent(getApplicationContext(), LoginActivity.class);
					mainmenu.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					startActivity(mainmenu);
			   }        
			});
		
	}
}
