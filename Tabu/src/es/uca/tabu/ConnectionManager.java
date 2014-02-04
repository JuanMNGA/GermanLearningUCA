package es.uca.tabu;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;

public class ConnectionManager {

	private JSONParser jsonParser;

	private static ConnectionManager instance = null;
	private static String server = null;
	private static HttpClient httpclient = null;

	private static String login_tag = "login";
	private static String register_tag = "register";
	private static String forpass_tag = "forpass";
	private static String chgpass_tag = "chgpass";
	
	private static Context c = null;

	public static ConnectionManager getInstance() {
		if(instance == null)
			instance =  new ConnectionManager();
		return instance;
	}
	
	public static ConnectionManager getInstance(Context c2) {
		if(instance == null) {
			instance = new ConnectionManager();
		}
		c = c2;
		return instance;
	}

	public Boolean networkWorks() {
		try {
			return new NetCheck().execute().get();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}
	
	public JSONObject addNewUser(String nombre, String password, String email, String rol) {
		List<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
		params.add(new BasicNameValuePair("tag", register_tag));
		params.add(new BasicNameValuePair("nombre", nombre));
		params.add(new BasicNameValuePair("password", password));
		params.add(new BasicNameValuePair("email", email));
		params.add(new BasicNameValuePair("rol", rol));

		JSONObject json = jsonParser.getJSONFromUrl(server+"addNewUser.php", params);

		return json;
	}

	public JSONObject loginUser(String email, String password) {
		
		List<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
		params.add(new BasicNameValuePair("tag", login_tag));
		params.add(new BasicNameValuePair("password", password));
		params.add(new BasicNameValuePair("email", email));

		JSONObject json = jsonParser.getJSONFromUrl(server+"loginUser.php", params);

		return json;
	}

	private ConnectionManager() {
		httpclient = new DefaultHttpClient();
		server = new String("http://192.168.1.34/tabu/");
		jsonParser = new JSONParser();
	}
	
	/**
	 * Async Task to check whether internet connection is working.
	 **/
	public class NetCheck extends AsyncTask<Void, Void, Boolean>
	{
		@Override
		protected Boolean doInBackground(Void... params) {
			/**
			 * Gets current device state and checks for working internet connection by trying Google.
			 **/
			ConnectivityManager cm = (ConnectivityManager) c.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo netInfo = cm.getActiveNetworkInfo();
			if (netInfo != null && netInfo.isConnected()) {
				try {
					URL url = new URL("http://www.google.com");
					HttpURLConnection urlc = (HttpURLConnection) url.openConnection();
					urlc.setConnectTimeout(3000);
					urlc.connect();
					if (urlc.getResponseCode() == 200) {
						return true;
					}
				} catch (MalformedURLException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			return false;
		}
		@Override
		protected void onPostExecute(Boolean internetOK){
			if(!internetOK){
				// INFORMA
			}

		}
	}
}
