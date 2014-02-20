package es.uca.tabu;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class ReviewQuestionActivity extends Activity {

	
	Button backBtn, dictionaryBtn;
	EditText word;
	TextView definition;
	TextView article;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_review_question);
		

		Bundle extras = getIntent().getExtras();
		Question q = null;
		if(extras != null) {
			q = (Question) extras.getSerializable("EXTRA_QUESTION");
			
			backBtn = (Button) findViewById(R.id.backToMenu);
			backBtn.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					Intent result = new Intent(getApplicationContext(), ResultActivity.class);
					result.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					startActivity(result);
					finish();
				} 
			});
			
			word = (EditText) findViewById(R.id.word);
			word.setKeyListener(null);
			definition = (TextView) findViewById(R.id.definition);
			article = (TextView) findViewById(R.id.article);
			
			word.setText(q.getName());
			if(q.isSuccess()) {
				word.setTextColor(Color.parseColor("#006400"));
			}
			else {
				word.setTextColor(Color.RED);
			}
			definition.setText(q.getDefinition());
			article.setText(q.getArticle());
			
			dictionaryBtn = (Button) findViewById(R.id.dictionary);
			dictionaryBtn.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					int startSelection = definition.getSelectionStart();
					int endSelection = definition.getSelectionEnd();
					
					if(startSelection != endSelection) {
						String selectedText = definition.getText().toString().substring(startSelection, endSelection);
						if(!selectedText.contains(" ")) {
							Toast.makeText(ReviewQuestionActivity.this, selectedText + " added", Toast.LENGTH_SHORT)
							.show();
						}
						else {
							Toast.makeText(ReviewQuestionActivity.this, "You have selected more than one word", Toast.LENGTH_SHORT)
							.show();
						}
					}
					else {
						Toast.makeText(ReviewQuestionActivity.this,"No text selected", Toast.LENGTH_SHORT)
						.show();
					}
				} 
			});
		}
		else
		{
			System.out.println("NO EXTRAS AT REVIEW");
		}
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.review_question, menu);
		return true;
	}

}
