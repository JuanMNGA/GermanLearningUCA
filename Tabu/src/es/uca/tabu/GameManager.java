package es.uca.tabu;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

public class GameManager {
	
	private static String KEY_DEFINITION = "definiciones";
	private static String KEY_ARTICLE = "articulos";
	private static String KEY_ID = "ids";
	private static String KEY_NAMES = "nombres";
	
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
		return questions.contains(new Question(id, word, "", "", false)) || questions.contains(new Question(id, TabuUtils.accentGerman(word), "", "", false));
	}
	
	private GameManager() {}
	
	
	private class getCategorizedQuestions extends AsyncTask<Object, Boolean, JSONObject> {

		// Devuelve true si consigue meter el usuario en la base de datos
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
			
			System.out.println("ON POST EXECUTE");
			/**
			 * Checks for success message.
			 **/
			try {
				if (!json.isNull(TabuUtils.KEY_SUCCESS)) {
					JSONArray articles = null;
					if(!json.isNull(KEY_ARTICLE))
						articles = json.getJSONArray(KEY_ARTICLE);
					JSONArray ids = json.getJSONArray(KEY_ID);
					JSONArray definitions = json.getJSONArray(KEY_DEFINITION);
					JSONArray names = json.getJSONArray(KEY_NAMES);
					
					String article = null;
					for(int i=0; i<ids.length(); i++) {
						
						// Article might be null
						if(!json.isNull(KEY_ARTICLE))
							article = articles.getString(i);
						
						questions.add(new Question(
								ids.getInt(i),
								names.getString(i),
								article,
								definitions.getString(i),
								false));
					}
					for(int i=0; i<questions.size(); i++) {
						System.out.println("IDS: " + questions.get(i).getId());
						System.out.println("DEF: " + questions.get(i).getDefinition());
					}
					
					GameActivity ga = (GameActivity) c;
					ga.requestNextQuestion();
					
				}
				else {
					TabuUtils.showDialog(c.getResources().getString(R.string.error), c.getResources().getString(R.string.errorQuestions),c);
				}
			} catch (JSONException e) {
				System.out.println("Error en QuestionCategorized postExecute");
				e.printStackTrace();
			}
		}
	}
}
