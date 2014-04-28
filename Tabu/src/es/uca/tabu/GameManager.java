package es.uca.tabu;

import java.lang.reflect.Type;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.speech.tts.TextToSpeech;
import android.widget.Toast;

public class GameManager implements 
android.speech.tts.TextToSpeech.OnInitListener {

	private static String KEY_PREPALABRA = "prepalabras";
	private static String KEY_POSTPALABRA = "postpalabras";
	private static String KEY_ARTICLE = "articulos";
	private static String KEY_ID = "ids";
	private static String KEY_NAMES = "nombres";
	private static String KEY_CLUES = "pistas";

	public static short MAX_TRIES = 3;

	private static GameManager instance = null;

	private int numberOfQuestions;
	private ArrayList<Integer> categories;
	private int level;

	private int time = 0;

	private ArrayList<Question> questions;
	private Question currentQuestion = null;

	private static Context c = null;
	
	private TextToSpeech tts = null;

	public static GameManager getInstance() {
		if(instance == null)
			instance =  new GameManager();
		return instance;
	}

	public static GameManager getInstance(Context c2) {
		if(instance == null) {
			instance = new GameManager(c2);
		}
		c = c2;
		
		return instance;
	}
	
	public void destroy() {
        // Don't forget to shutdown tts!
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
	}
	
	public void clean() {
		if(questions != null) {
			questions.clear();
			questions = null;
		}
		if(categories != null) {
			categories.clear();
			categories = null;
		}
		
		currentQuestion = null;
	}

	public int getTime() {
		return time;
	}

	public Question getCurrentQuestion() {
		return currentQuestion;
	}

	public void setNumOfQuestions(int nq) {
		numberOfQuestions = nq;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public int getNumOfQuestions() {
		return numberOfQuestions;
	}

	public int getNumOfPassedQuestions() {
		int number=0;
		for(Question q : questions) {
			if(q.isSuccess())
				number++;
		}
		return number;
	}

	public int getNumOfUsedClues() {
		int number=0;
		for(Question q : questions) {
			if(q.isClue())
				number++;
		}
		return number;
	}

	public ArrayList<Question> getQuestions() {
		return questions;
	}

	public boolean isQuestionPassed(int i) {
		if(i >= 0 && i< questions.size()) {
			return questions.get(i).isSuccess();
		}
		return false;
	}

	public void setCategories(ArrayList<Integer> c) {
		categories = c;
	}

	public void initialize() {
		questions = new ArrayList<Question>(numberOfQuestions);
		new getCategorizedQuestions().execute(numberOfQuestions, level, categories);
	}

	public Question next() {
		if(questions.size() != 0) {
			if(currentQuestion == null)
				currentQuestion = questions.get(0);
			else if(questions.indexOf(currentQuestion) < questions.size()-1) {
				currentQuestion = questions.get(questions.indexOf(currentQuestion)+1);
			}
			else
				return null;

			return currentQuestion;
		}
		else {
			TabuUtils.showDialog(c.getResources().getString(R.string.error), c.getResources().getString(R.string.noQuestions),c);
			return null;
		}
	}

	public Boolean validWord(Integer id, String word) {
		return questions.contains(new Question(id, word, "", "", "", "", false)) || questions.contains(new Question(id, TabuUtils.accentGerman(word), "", "", "", "", false));
	}

	public void addWordToBloc(Integer id, String word) {
		new addWord().execute(id,word);
	}

	public void deleteQuestion(Question q) {
		questions.remove(q);
		numberOfQuestions--;
	}

	public void saveCurrentGame() {
		//Creating a shared preference
		Type arrayListOfQuestions = new TypeToken<ArrayList<Question>>(){}.getType();
		SharedPreferences  prefs = c.getSharedPreferences("last_game", c.MODE_PRIVATE);
		Editor prefsEditor = prefs.edit();
		Gson gson = new Gson();
		String json_questions = gson.toJson(questions, arrayListOfQuestions);
		prefsEditor.putString("questions", json_questions);
		prefsEditor.commit();
	}
	
	public boolean initializeFromSharedPreferences() {
		SharedPreferences  prefs = c.getSharedPreferences("last_game", c.MODE_PRIVATE);
		Gson gson = new Gson();
	    String json = prefs.getString("questions", "");
	    if(json.compareTo("") != 0) {
	    	Type arrayListOfQuestions = new TypeToken<ArrayList<Question>>(){}.getType();
	    	questions = gson.fromJson(json, arrayListOfQuestions);
	    	numberOfQuestions = questions.size();
	    	return true;
	    }
	    return false;
	}

	public TextToSpeech getTTS() {
		return tts;
	}
	
	private GameManager() {	}
	private GameManager(Context context) {
		tts = new TextToSpeech(context.getApplicationContext(), this);
	}


	private class getCategorizedQuestions extends AsyncTask<Object, Boolean, JSONObject> {

		ProgressDialog dialog;

		@Override
		protected void onPreExecute() {
			dialog = ProgressDialog.show(c, " ", 
					c.getResources().getString(R.string.updating), true);
		}

		@Override
		protected JSONObject doInBackground(Object... questioninfo) {

			//If there is access to Internet
			if(ConnectionManager.getInstance(c).networkWorks()) {
				return ConnectionManager.getInstance().getQuestions(
						(Integer)questioninfo[0],
						(Integer)questioninfo[1],
						(ArrayList<Integer>) questioninfo[2]);
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
					TabuUtils.showDialog(c.getResources().getString(R.string.error), c.getResources().getString(R.string.noNetwork),c);
				}
				if (!json.isNull(TabuUtils.KEY_SUCCESS)) {
					JSONArray articles = null;
					if(!json.isNull(KEY_ARTICLE))
						articles = json.getJSONArray(KEY_ARTICLE);
					JSONArray ids = json.getJSONArray(KEY_ID);
					JSONArray prepalabras = json.getJSONArray(KEY_PREPALABRA);
					JSONArray postpalabras = json.getJSONArray(KEY_POSTPALABRA);
					JSONArray names = json.getJSONArray(KEY_NAMES);
					JSONArray clues = json.getJSONArray(KEY_CLUES);

					time = json.getInt("tiempo");

					String article = null;
					for(int i=0; i<ids.length(); i++) {

						// Article might be null
						if(!json.isNull(KEY_ARTICLE))
							article = articles.getString(i);

						questions.add(new Question(
								ids.getInt(i),
								names.getString(i),
								article,
								prepalabras.getString(i),
								postpalabras.getString(i),
								clues.getString(i),
								false));
					}

					GameActivity ga = (GameActivity) c;
					ga.requestNextQuestion();
					dialog.cancel();
				}
				else {
					dialog.cancel();
					TabuUtils.showDialog(c.getResources().getString(R.string.error), c.getResources().getString(R.string.errorQuestions),c);
				}
			} catch (JSONException e) {
				dialog.cancel();
				System.out.println("Error en QuestionCategorized postExecute");
				e.printStackTrace();
			}
		}
	}

	private class addWord extends AsyncTask<Object, Boolean, JSONObject> {

		String word;

		@Override
		protected JSONObject doInBackground(Object... word) {
			this.word = (String)word[1];

			//If there is access to Internet
			if(ConnectionManager.getInstance(c).networkWorks()) {
				return ConnectionManager.getInstance().addWordToBloc(
						(Integer)word[0],
						(String)word[1]);
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
			if(json == null) {
				TabuUtils.showDialog(c.getResources().getString(R.string.error), c.getResources().getString(R.string.noNetwork),c);
			}
			else if (!json.isNull(TabuUtils.KEY_SUCCESS)) {
				Toast.makeText(c, this.word + " " + c.getString(R.string.added), Toast.LENGTH_SHORT)
				.show();
			}
			else {
				TabuUtils.showDialog(c.getResources().getString(R.string.error), c.getResources().getString(R.string.serverIssues),c);
			}
		}
	}

	@Override
	public void onInit(int status) {
		// TODO Auto-generated method stub
        if (status == TextToSpeech.SUCCESS) {
        	Resources res = c.getResources();
    		android.content.res.Configuration conf = res.getConfiguration();
            int result = tts.setLanguage(conf.locale);
 
            if (result == TextToSpeech.LANG_MISSING_DATA
                    || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                System.out.println("TTS: " + "This Language is not supported");
            } else {
                System.out.println("TTS: " + "Speech engine initialized");
            }
 
        } else {
        	System.out.println("TTS: " + "Initilization Failed!");
        }
	}

}
