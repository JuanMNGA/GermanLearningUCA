package es.uca.tabu;

import org.json.JSONException;
import org.json.JSONObject;

import com.google.common.base.Function;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class RegisterActivity extends Activity {

    /**
     *  JSON Response node names.
     **/
    private static String KEY_SUCCESS = "success";
    private static String KEY_NAME = "name";
    private static String KEY_EMAIL = "email";
    private static String KEY_ERROR = "error";
	
	ProgressDialog dialog;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Set View to register.xml
		TabuUtils.hideActionBar(this);
		setContentView(R.layout.register);

		TextView loginScreen = (TextView) findViewById(R.id.link_to_login);

		// Listening to Login Screen link
		loginScreen.setOnClickListener(new View.OnClickListener() {

			public void onClick(View arg0) {
				// Closing registration screen
				// Switching to Login Screen/closing register screen
				finish();
			}
		});

		Button submit = (Button) findViewById(R.id.btnRegister);

		// Listening to register new account link
		submit.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				String name  = ((EditText) findViewById(R.id.reg_fullname)).getText().toString();
				String pass  = ((EditText) findViewById(R.id.reg_password)).getText().toString();
				String email = ((EditText) findViewById(R.id.reg_email)).getText().toString();

				// Validate data
				if(!TabuUtils.validateName(name)) {
					TabuUtils.showDialog(getResources().getString(R.string.error), getResources().getString(R.string.invalidName),RegisterActivity.this);
				}
				else if(!TabuUtils.validateEmail(email)) {
					TabuUtils.showDialog(getResources().getString(R.string.error), getResources().getString(R.string.invalidEmail),RegisterActivity.this);
				}
				else if(!TabuUtils.validatePassword(pass)) {
					TabuUtils.showDialog(getResources().getString(R.string.error), getResources().getString(R.string.invalidPass),RegisterActivity.this);
				}
				else {
						// Insert the user
						new addUserTask().execute(
								name,
								pass,
								email,
								new String("estudiante"));
				}
			}
		});
	}

	private class addUserTask extends AsyncTask<String, Object, JSONObject> {
		ProgressDialog dialog;

		@Override
		protected void onPreExecute() {
			dialog = ProgressDialog.show(RegisterActivity.this, " ", 
					getResources().getString(R.string.sending), true);
		}
		
		// Devuelve true si consigue meter el usuario en la base de datos
		@Override
		protected JSONObject doInBackground(String... user) {
			
			if(ConnectionManager.getInstance(RegisterActivity.this).networkWorks()) {
				return ConnectionManager.getInstance().addNewUser(
						user[0],
						user[1],
						user[2],
						user[3]);
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
			try {
				if(json == null) {
					dialog.dismiss();
					TabuUtils.showDialog(getResources().getString(R.string.error), getResources().getString(R.string.noNetwork),RegisterActivity.this);
				} else if (!json.isNull(KEY_SUCCESS)) {
					String res = json.getString(KEY_SUCCESS);
					String red = json.getString(KEY_ERROR);
					if(Integer.parseInt(res) == 1){
						dialog.dismiss();

					    SharedPreferences 		loginPreferences = getSharedPreferences("loginPrefs", MODE_PRIVATE);
					    SharedPreferences.Editor loginPrefsEditor = loginPreferences.edit();
					    
						JSONObject json_user = json.getJSONObject("user");
						loginPrefsEditor.putInt("id", json_user.getInt("id"));
						loginPrefsEditor.commit();
						
						TabuUtils.showDialog(" ", getResources().getString(R.string.userReg),
								new Function<DialogInterface, Void>() { //Function to switch to MainMenuActivity when dialog button clicked
									@Override
									public Void apply(DialogInterface arg0) {
										arg0.cancel();

										Intent registered = new Intent(getApplicationContext(), MainMenuActivity.class);
										registered.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
										startActivity(registered);
										finish();
										return null;
									} 
								},
								RegisterActivity.this);
						//
						/**
						 * Close all views before launching Registered screen
						 **/
					}
					else if (Integer.parseInt(red) ==2){
						dialog.dismiss();
						TabuUtils.showDialog(getResources().getString(R.string.error), getResources().getString(R.string.userNotReg),RegisterActivity.this);
					}
				}
				else{
					dialog.dismiss();
					TabuUtils.showDialog(getResources().getString(R.string.error), getResources().getString(R.string.serverIssues),RegisterActivity.this);
				}
			} catch (JSONException e) {
				dialog.dismiss();
				System.out.println("Error en Register postExecute");
				e.printStackTrace();
			}
		}
	}
}