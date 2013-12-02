package es.uca.tabu;

import java.util.Locale;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class LoginActivity extends Activity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
        super.onCreate(savedInstanceState);
        
        // setting default screen to login.xml
        setContentView(R.layout.login);
        
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
                // Switching to Register screen
                Intent i = new Intent(getApplicationContext(), MainMenuActivity.class);
                startActivity(i);
            }
        });
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.login, menu);
		return true;
	}

}
