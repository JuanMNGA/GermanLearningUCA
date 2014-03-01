package es.uca.tabu;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

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
	
	private static String numQuestions_tag = "questions";
	private static String questions_tag = "getquestions";
	private static String categories_tag = "categories";
	private static String checkWord_tag = "checkWord";
	private static String storeStadistics_tag = "store_stadistics";
	
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

		JSONObject json = jsonParser.getJSONFromUrl(server+"login_register.php", params);

		return json;
	}

	public JSONObject loginUser(String email, String password) {
		
		List<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
		params.add(new BasicNameValuePair("tag", login_tag));
		params.add(new BasicNameValuePair("password", password));
		params.add(new BasicNameValuePair("email", email));
		
		JSONObject json = jsonParser.getJSONFromUrl(server+"login_register.php", params);

		return json;
	}

	public JSONObject getQuestions(Integer numQuestions, Integer level, ArrayList<Integer> categories) {
		
		JSONArray jsArray = new JSONArray(categories);
		JSONObject jsCategories = new JSONObject();
		try {
			jsCategories.put("level", level);
			jsCategories.put("numOfQuestions", numQuestions);
			jsCategories.put("categories", jsArray);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			System.out.println("ERROR EN QETQUESTIONS:");
			e.printStackTrace();
		}
		
		List<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
		params.add(new BasicNameValuePair("tag", questions_tag));
		params.add(new BasicNameValuePair("categories", jsCategories.toString()));
		params.add(new BasicNameValuePair("numOfQuestions", Integer.toString(numQuestions)));
		
		JSONObject json = jsonParser.getJSONFromUrl(server+"playManager.php", params);
		
		return json;
	}
	
	public JSONObject getMaxQuestions(Integer level, ArrayList<Integer> categories) {
		JSONArray jsArray = new JSONArray(categories);
		JSONObject jsCategories = new JSONObject();
		
		try {
			jsCategories.put("level", level);
			jsCategories.put("categories", jsArray);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			System.out.println("ERROR EN getMaxQuestions:");
			e.printStackTrace();
		}
		
		List<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
		params.add(new BasicNameValuePair("tag", numQuestions_tag));
		params.add(new BasicNameValuePair("object", jsCategories.toString()));
		
		JSONObject json = jsonParser.getJSONFromUrl(server+"playManager.php", params);

		return json;
	}
	
	public JSONObject checkWord(Integer id, String word) {
		List<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
		params.add(new BasicNameValuePair("tag", checkWord_tag));
		params.add(new BasicNameValuePair("id", Integer.toString(id)));
		params.add(new BasicNameValuePair("palabra", word));
		
		JSONObject json = jsonParser.getJSONFromUrl(server+"playManager.php", params);

		return json;
	}
	
	public JSONObject getAllCategories() {
		List<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
		params.add(new BasicNameValuePair("tag", categories_tag));
		
		JSONObject json = jsonParser.getJSONFromUrl(server+"playManager.php", params);

		return json;
	}
	
	public JSONObject storeStadistics(Integer user_id, ArrayList<Question> questionList) {
		// Gran cuca usando librería GSON de google para parsear el arraylist de questions directamente
		Gson gson = new Gson();
		JsonElement element = gson.toJsonTree(questionList, new TypeToken<ArrayList<Question>>() {}.getType());
		if(!element.isJsonArray()) {
			System.out.println("ERROR parseando en storeStadistics");
			return null;
		}
		
		JsonArray jsArray = element.getAsJsonArray();
		JSONObject jsQuestions = new JSONObject();
		
		try {
			jsQuestions.put("questions", jsArray.toString());
			jsQuestions.put("user", user_id);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			System.out.println("ERROR EN storeStadistics:");
			e.printStackTrace();
		}
		
		List<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
		params.add(new BasicNameValuePair("tag", storeStadistics_tag));
		params.add(new BasicNameValuePair("object", jsQuestions.toString()));
		
		JSONObject json = jsonParser.getJSONFromUrl(server+"playManager.php", params);

		return json;
	}
	
	private ConnectionManager() {
		httpclient = new DefaultHttpClient();
		server = new String("http://192.168.1.37/tabu/");
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
