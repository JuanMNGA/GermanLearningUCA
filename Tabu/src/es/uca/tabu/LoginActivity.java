package es.uca.tabu;

import java.util.Locale;

import org.json.JSONException;
import org.json.JSONObject;

import com.google.common.base.Function;
import es.uca.tabu.utils.Environment;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

public class LoginActivity extends Activity {
	private static String KEY_SUCCESS = "success";
	private static String KEY_NOMBRE = "nombre";
	private static String KEY_EMAIL = "email";

	private CheckBox saveLoginCheckBox;
	private SharedPreferences loginPreferences;
	private SharedPreferences.Editor loginPrefsEditor;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		/* Temporalmente en alem�n */
		Resources res = this.getResources();
		// Change locale settings in the app.
		DisplayMetrics dm = res.getDisplayMetrics();
		android.content.res.Configuration conf = res.getConfiguration();
		conf.locale = Locale.GERMAN;
		res.updateConfiguration(conf, dm);
		
		//Store screensize
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		Environment.getInstance().setScreenHeight(dm.heightPixels);
		Environment.getInstance().setScreenWidth(dm.widthPixels);
		Environment.getInstance().setDensity(dm.density);
		
		// setting default screen to login.xml
		setContentView(R.layout.login);

		// Load default options if rememberMe is checked
		saveLoginCheckBox = (CheckBox)findViewById(R.id.rememberMeCheckBox);
		loginPreferences = getSharedPreferences("loginPrefs", MODE_PRIVATE);
		loginPrefsEditor = loginPreferences.edit();

		if (loginPreferences.getBoolean("saveLogin", false)) {
			((EditText) findViewById(R.id.reg_email)).setText(loginPreferences.getString("email", ""));
			((EditText) findViewById(R.id.reg_password)).setText(loginPreferences.getString("password", ""));
			saveLoginCheckBox.setChecked(true);
			((View) findViewById(R.id.dummyLayout)).requestFocus();
		}

		TextView registerScreen = (TextView) findViewById(R.id.link_to_register);

		// Listening to register new account link
		registerScreen.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				// Switching to Register screen
				Intent i = new Intent(getApplicationContext(), RegisterActivity.class);
				startActivity(i);
			}
		});

		Button mainMenu = (Button) findViewById(R.id.btnLogin);

		// Listening to register new account link
		mainMenu.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				String pass  = ((EditText) findViewById(R.id.reg_password)).getText().toString();
				String email = ((EditText) findViewById(R.id.reg_email)).getText().toString();

				// Validate data
				if(!TabuUtils.validateEmail(email)) {
					TabuUtils.showDialog(getResources().getString(R.string.error), getResources().getString(R.string.invalidEmail),LoginActivity.this);
				}
				else if(!TabuUtils.validatePassword(pass)) {
					TabuUtils.showDialog(getResources().getString(R.string.error), getResources().getString(R.string.invalidPass),LoginActivity.this);
				}
				else {
					// Save remember me options
					if (saveLoginCheckBox.isChecked()) {
						loginPrefsEditor.putBoolean("saveLogin", true);
						loginPrefsEditor.putString("email", email);
						loginPrefsEditor.putString("password", pass);
						loginPrefsEditor.commit();
					} else {
						loginPrefsEditor.clear();
						loginPrefsEditor.commit();
					}

					// Login the user
					new loginUserTask().execute(
							email,
							pass);
				}
			}
		});
		
		Button forgot = (Button) findViewById(R.id.btnRemember);
		forgot.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				final String email = ((EditText) findViewById(R.id.reg_email)).getText().toString();

				// Validate data
				if(!TabuUtils.validateEmail(email)) {
					TabuUtils.showDialog(getResources().getString(R.string.error), getResources().getString(R.string.invalidEmail),LoginActivity.this);
				}
				else {
					TabuUtils.showConfirmDialog(" ", getResources().getString(R.string.sureReset),
							new Function<DialogInterface, Void>() {
						@Override
						public Void apply(DialogInterface arg0) {
							arg0.cancel();
							// Login the user
							new forgotMyPassTask().execute(email);
							return null;
						} 
					},
					LoginActivity.this);

				}
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.login, menu);
		return true;
	}

	private class loginUserTask extends AsyncTask<String, Void, JSONObject> {
		ProgressDialog dialog;

		@Override
		protected void onPreExecute() {
			dialog = ProgressDialog.show(LoginActivity.this, " ", 
					getResources().getString(R.string.logging), true);
		}

		@Override
		protected JSONObject doInBackground(String... user) {
			//If there is access to Internet
			if(ConnectionManager.getInstance(LoginActivity.this).networkWorks()) {
				return ConnectionManager.getInstance().loginUser(
						user[0],
						user[1]);
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
					TabuUtils.showDialog(getResources().getString(R.string.error), getResources().getString(R.string.noNetwork),LoginActivity.this);
				}
				else if (!json.isNull(KEY_SUCCESS)) {
					String res = json.getString(KEY_SUCCESS);
					if(Integer.parseInt(res) == 1){
						dialog.dismiss();

						JSONObject json_user = json.getJSONObject("user");
						loginPrefsEditor.putInt("id", json_user.getInt("id"));
						loginPrefsEditor.commit();

						Intent mainmenu = new Intent(getApplicationContext(), MainMenuActivity.class);
						mainmenu.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
						startActivity(mainmenu);
						/**
						 * Close Login Screen
						 **/
						finish();
					}else{
						dialog.dismiss();
						TabuUtils.showDialog(getResources().getString(R.string.error), getResources().getString(R.string.userNotExists),LoginActivity.this);
					}
				}
				else {
					dialog.dismiss();
					TabuUtils.showDialog(getResources().getString(R.string.error), getResources().getString(R.string.serverIssues),LoginActivity.this);
				}
			} catch (JSONException e) {
				e.printStackTrace();
				dialog.dismiss();
			}
		}
	}
	
	private class forgotMyPassTask extends AsyncTask<String, Void, JSONObject> {
		ProgressDialog dialog;

		@Override
		protected void onPreExecute() {
			dialog = ProgressDialog.show(LoginActivity.this, " ", 
					getResources().getString(R.string.sending), true);
		}

		@Override
		protected JSONObject doInBackground(String... user) {
			//If there is access to Internet
			if(ConnectionManager.getInstance(LoginActivity.this).networkWorks()) {
				return ConnectionManager.getInstance().resetPassword(
						user[0]);
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
					TabuUtils.showDialog(getResources().getString(R.string.error), getResources().getString(R.string.noNetwork),LoginActivity.this);
				}
				else if (!json.isNull(KEY_SUCCESS)) {
					String res = json.getString(KEY_SUCCESS);
					if(Integer.parseInt(res) == 1){
						dialog.dismiss();
						TabuUtils.showDialog(" ", getResources().getString(R.string.newPassword),LoginActivity.this);
					}else{
						dialog.dismiss();
						TabuUtils.showDialog(getResources().getString(R.string.error), getResources().getString(R.string.emailNotExists),LoginActivity.this);
					}
				}
				else {
					dialog.dismiss();
					TabuUtils.showDialog(getResources().getString(R.string.error), getResources().getString(R.string.serverIssues),LoginActivity.this);
				}
			} catch (JSONException e) {
				e.printStackTrace();
				dialog.dismiss();
			}
		}
	}

}
