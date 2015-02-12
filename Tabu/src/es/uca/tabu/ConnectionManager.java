package es.uca.tabu;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
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
	private static String reset_pass_tag = "resetpass";
	private static String chgpass_tag = "chgpass";
	private static String getWordToDef_tag = "provideWordForDef";
	private static String numQuestions_tag = "questions";
	private static String questions_tag = "getquestions";
	private static String sendDefinition_tag = "addDefinition";
	private static String categories_tag = "categories";
	private static String checkWord_tag = "checkWord";
	private static String storeStadistics_tag = "store_stadistics";
	private static String getNotes_tag = "getNotes";
	private static String addWord_tag = "addWord";
	private static String sendReport_tag = "sendReport";
	private static String getStatistics_tag = "get_statistics";
	private static String getVersion_tag = "version";
	
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

	/* Auxiliar function, Must be called from async task */
	public Boolean networkWorks() {
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
	/*public Boolean networkWorks() {
		System.out.println("ConnectionManager: networkWorks()");
		try {
			System.out.println("ConnectionManager: networkWorks()2");
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
	}*/
	
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

	public JSONObject resetPassword(String email) {
		List<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
		params.add(new BasicNameValuePair("tag", reset_pass_tag));
		params.add(new BasicNameValuePair("email", email));
		
		JSONObject json = jsonParser.getJSONFromUrl(server+"login_register.php", params);

		return json;
	}
	
	public JSONObject getQuestions(Integer user_id, Integer numQuestions, Integer level, ArrayList<Integer> categories) {
		
		JSONArray jsArray = new JSONArray(categories);
		JSONObject jsCategories = new JSONObject();
		try {
			jsCategories.put("level", level);
			jsCategories.put("numOfQuestions", numQuestions);
			jsCategories.put("categories", jsArray);
			jsCategories.put("user_id", user_id);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			System.out.println("ERROR EN QETQUESTIONS:");
			e.printStackTrace();
		}
		
		List<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
		params.add(new BasicNameValuePair("tag", questions_tag));
		params.add(new BasicNameValuePair("categories", jsCategories.toString()));
		params.add(new BasicNameValuePair("numOfQuestions", Integer.toString(numQuestions)));
		params.add(new BasicNameValuePair("user_id", Integer.toString(user_id)));
		
		JSONObject json = jsonParser.getJSONFromUrl(server+"playManager.php", params);
		
		return json;
	}
	
	public JSONObject getMaxQuestions(Integer level, ArrayList<Integer> categories, String lang) {
		JSONArray jsArray = new JSONArray(categories);
		JSONObject jsCategories = new JSONObject();
		
		try {
			jsCategories.put("level", level);
			jsCategories.put("idioma", lang);
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
	
	public JSONObject getAllCategories(String lang) {
		List<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
		params.add(new BasicNameValuePair("tag", categories_tag));
		params.add(new BasicNameValuePair("idioma", lang));
		
		JSONObject json = jsonParser.getJSONFromUrl(server+"playManager.php", params);

		return json;
	}
	
	public JSONObject storeStadistics(Integer user_id, ArrayList<Question> questionList) {
		// Gran cuca usando librer�a GSON de google para parsear el arraylist de questions directamente
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
	
	public JSONObject sendReport(Integer user_id, Integer question_id, Integer rate, String reason) {
		List<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
		params.add(new BasicNameValuePair("tag", sendReport_tag));
		params.add(new BasicNameValuePair("user_id", user_id.toString()));
		params.add(new BasicNameValuePair("question_id", question_id.toString()));
		params.add(new BasicNameValuePair("rate", rate.toString()));
		params.add(new BasicNameValuePair("reason", reason));
		JSONObject json = jsonParser.getJSONFromUrl(server+"playManager.php", params);
		return json;
	}
	
	public JSONObject getNotes(Integer user_id, String lang) {
		List<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
		params.add(new BasicNameValuePair("tag", getNotes_tag));
		params.add(new BasicNameValuePair("user_id", user_id.toString()));
		params.add(new BasicNameValuePair("idioma", lang));
		JSONObject json = jsonParser.getJSONFromUrl(server+"playManager.php", params);
		
		return json;
	}
	
	public JSONObject addWordToBloc(Integer user_id, String word, String lang) {
		List<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
		params.add(new BasicNameValuePair("tag", addWord_tag));
		params.add(new BasicNameValuePair("user_id", user_id.toString()));
		params.add(new BasicNameValuePair("word", word));
		params.add(new BasicNameValuePair("idioma", lang));
		
		JSONObject json = jsonParser.getJSONFromUrl(server+"playManager.php", params);

		return json;
	}
	
	public JSONObject sendDefinition(Integer user_id, String article, String word, String prepalabra, String postpalabra, String hint, Integer level, String category, String lang) {
		List<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
		params.add(new BasicNameValuePair("tag", sendDefinition_tag));
		params.add(new BasicNameValuePair("id_user", user_id.toString()));
		params.add(new BasicNameValuePair("word", word));
		params.add(new BasicNameValuePair("article", article));
		params.add(new BasicNameValuePair("prepalabra", prepalabra));
		params.add(new BasicNameValuePair("postpalabra", postpalabra));
		params.add(new BasicNameValuePair("clue", hint));
		params.add(new BasicNameValuePair("level", level.toString()));
		params.add(new BasicNameValuePair("category", category));
		params.add(new BasicNameValuePair("idioma", lang));
		
		System.out.println(params.toString());
		
		JSONObject json = jsonParser.getJSONFromUrl(server+"playManager.php", params);

		return json;
	}
	
	public JSONObject getStatistics(Integer user_id, String lang) {
		List<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
		params.add(new BasicNameValuePair("tag", getStatistics_tag));
		params.add(new BasicNameValuePair("user_id", user_id.toString()));
		params.add(new BasicNameValuePair("idioma", lang));
		
		JSONObject json = jsonParser.getJSONFromUrl(server+"playManager.php", params);

		return json;
	}
	
	public JSONObject getWordToDef(Integer user_id, String lang) {
		List<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
		params.add(new BasicNameValuePair("tag", getWordToDef_tag));
		params.add(new BasicNameValuePair("user_id", user_id.toString()));
		params.add(new BasicNameValuePair("idioma", lang));
		
		JSONObject json = jsonParser.getJSONFromUrl(server+"playManager.php", params);

		return json;
	}
	
	public JSONObject getLastVersion() {
		List<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
		params.add(new BasicNameValuePair("tag", getVersion_tag));
		
		JSONObject json = jsonParser.getJSONFromUrl(server+"playManager.php", params);

		return json;
	}
	
	private ConnectionManager() {
		httpclient = new DefaultHttpClient();
		//server = new String("http://192.168.1.35/tabu/");
		//server = new String("http://94.247.31.212/tabu/");
		//server = new String("http://94.247.31.212/tabu/Granada/");
		//server = new String("http://94.247.31.212/tabu/UCA_Multilingue/");
		//server = new String("http://94.247.31.212/tabu/EOI_Multilingue/");
		server = new String("http://104.236.215.116/tabu/Australia/");
		jsonParser = new JSONParser();
	}
	
	/**
	 * Async Task to check whether internet connection is working.
	 **/
	public class NetCheck extends AsyncTask<Void, Void, Boolean>
	{
		
		@Override
		protected Boolean doInBackground(Void... params) {

			System.out.println("ConnectionManager: doInBackground()");
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
				//TabuUtils.showDialog(c.getString(R.string.error), c.getString(R.string.noNetwork) , c);
			}

		}
	}
}
