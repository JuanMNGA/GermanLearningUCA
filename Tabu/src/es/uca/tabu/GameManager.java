package es.uca.tabu;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.widget.Toast;

public class GameManager {
	
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
	
	private ArrayList<Question> questions;
	private Question currentQuestion = null;
		
	private static Context c = null;

	public static GameManager getInstance() {
		if(instance == null)
			instance =  new GameManager();
		return instance;
	}
	
	public static GameManager getInstance(Context c2) {
		if(instance == null) {
			instance = new GameManager();
		}
		c = c2;
		return instance;
	}
	
	public void clean() {
		c = null;
		currentQuestion = null;
		questions = null;
		categories = null;
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
		//If there is access to Internet
		if(ConnectionManager.getInstance(c).networkWorks()) {
			new getCategorizedQuestions().execute(numberOfQuestions, level, categories);
		}
		else {
			TabuUtils.showDialog(c.getResources().getString(R.string.error), c.getResources().getString(R.string.noNetwork),c);
		}
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
	
	private GameManager() {}
	
	
	private class getCategorizedQuestions extends AsyncTask<Object, Boolean, JSONObject> {

		ProgressDialog dialog;

		@Override
		protected void onPreExecute() {
			dialog = ProgressDialog.show(c, " ", 
					c.getResources().getString(R.string.updating), true);
		}
		
		@Override
		protected JSONObject doInBackground(Object... questioninfo) {
			return ConnectionManager.getInstance().getQuestions(
					(Integer)questioninfo[0],
					(Integer)questioninfo[1],
					(ArrayList<Integer>) questioninfo[2]);
		}

		// Informa al usuario de lo sucedido
		@Override
		protected void onPostExecute(JSONObject json) {
			
			/**
			 * Checks for success message.
			 **/
			try {
				if (!json.isNull(TabuUtils.KEY_SUCCESS)) {
					JSONArray articles = null;
					if(!json.isNull(KEY_ARTICLE))
						articles = json.getJSONArray(KEY_ARTICLE);
					JSONArray ids = json.getJSONArray(KEY_ID);
					JSONArray prepalabras = json.getJSONArray(KEY_PREPALABRA);
					JSONArray postpalabras = json.getJSONArray(KEY_POSTPALABRA);
					JSONArray names = json.getJSONArray(KEY_NAMES);
					JSONArray clues = json.getJSONArray(KEY_CLUES);
					
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
					/*for(int i=0; i<questions.size(); i++) {
						System.out.println("IDS: " + questions.get(i).getId());
						System.out.println("DEF: " + questions.get(i).getDefinition());
					}*/
					
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
		
		// Devuelve true si consigue meter el usuario en la base de datos
		@Override
		protected JSONObject doInBackground(Object... word) {
			this.word = (String)word[1];

			return ConnectionManager.getInstance().addWordToBloc(
					(Integer)word[0],
					(String)word[1]);
		}

		// Informa al usuario de lo sucedido
		@Override
		protected void onPostExecute(JSONObject json) {
			/**
			 * Checks for success message.
			 **/
			if (!json.isNull(TabuUtils.KEY_SUCCESS)) {
				Toast.makeText(c, this.word + " " + c.getString(R.string.added), Toast.LENGTH_SHORT)
				.show();
			}
			else {
				TabuUtils.showDialog(c.getResources().getString(R.string.error), c.getResources().getString(R.string.serverIssues),c);
			}
		}
	}
	
}
