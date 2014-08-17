package es.uca.tabu;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Locale;

import org.json.JSONException;
import org.json.JSONObject;

import es.uca.tabu.utils.Environment;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

public class LanguageSelectionActivity extends Activity {

	ImageView uk, germany, russia;

	private SharedPreferences loginPreferences;
	private SharedPreferences.Editor loginPrefsEditor;

	private UpdateApp updateApp;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		TabuUtils.hideActionBar(this);
		setContentView(R.layout.select_language);

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
		russia = (ImageView) findViewById(R.id.russia);

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

		russia.setOnClickListener(new View.OnClickListener() {
			//@Override
			public void onClick(View v) {
				conf.locale = new Locale("ru");
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

	private class checkAppVersion extends AsyncTask<String, Void, JSONObject> {
		ProgressDialog dialog;

		@Override
		protected void onPreExecute() {
			dialog = ProgressDialog.show(LanguageSelectionActivity.this, " ", 
					getResources().getString(R.string.updating), true);
		}

		@Override
		protected JSONObject doInBackground(String... user) {
			//If there is access to Internet
			if(ConnectionManager.getInstance(LanguageSelectionActivity.this).networkWorks()) {
				return ConnectionManager.getInstance().getLastVersion();
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
					// EXIT
					//TabuUtils.showDialog(getResources().getString(R.string.error), getResources().getString(R.string.noNetwork),LoginActivity.this);
				}
				else if (!json.isNull(TabuUtils.KEY_SUCCESS)) {
					
					String versionName = LanguageSelectionActivity.this.getPackageManager()
							.getPackageInfo(getPackageName(), 0).versionName;
					
					String res = json.getString(TabuUtils.KEY_SUCCESS);
					if(TabuUtils.versionCompare(versionName, res) < 0) { // App is not updated... UPDATE NOW!
						
						updateApp = new UpdateApp();
						updateApp.setContext(LanguageSelectionActivity.this);
						dialog.dismiss();
						updateApp.execute("http://94.247.31.212/tabu/updateGuessIt.apk");
						
					}else{
						dialog.dismiss();
					}
					dialog.dismiss();
				}
				else {
					dialog.dismiss();
					TabuUtils.showDialog(getResources().getString(R.string.error), getResources().getString(R.string.serverIssues),LanguageSelectionActivity.this);
				}
			} catch (JSONException e) {
				e.printStackTrace();
				dialog.dismiss();
			} catch (NameNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}


	public class UpdateApp extends AsyncTask<String,Void,Void>{
		private Context context;
		public void setContext(Context contextf){
			context = contextf;
		}

		@Override
		protected Void doInBackground(String... arg0) {
			try {

				String filename = "GuessItupdate.apk";

				URL url = new URL(arg0[0]);
				HttpURLConnection c = (HttpURLConnection) url.openConnection();
				c.setRequestMethod("GET");
				c.setDoOutput(true);
				c.connect();

				String PATH = "/mnt/sdcard/Download/";
				File file = new File(PATH);
				file.mkdirs();
				File outputFile = new File(file, filename);
				if(outputFile.exists()){
					outputFile.delete();
				}
				FileOutputStream fos = new FileOutputStream(outputFile);

				InputStream is = c.getInputStream();

				byte[] buffer = new byte[1024];
				int len1 = 0;
				while ((len1 = is.read(buffer)) != -1) {
					fos.write(buffer, 0, len1);
				}
				fos.close();
				is.close();

				Intent intent = new Intent(Intent.ACTION_VIEW);
				intent.setDataAndType(Uri.fromFile(new File("/mnt/sdcard/Download/" + filename)), "application/vnd.android.package-archive");
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // without this flag android returned a intent error!
				context.startActivity(intent);


			} catch (Exception e) {
				Log.e("UpdateAPP", "Update error! " + e.getMessage());
			}
			return null;
		}
	}

}
