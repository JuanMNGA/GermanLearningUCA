package es.uca.tabu;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.*;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.SectionIndexer;

public class DictionaryActivity extends Activity {
	private ArrayList<String> mItems;
	private IndexableListView mListView;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dictionary);

		Bundle extras = getIntent().getExtras();
		if(extras != null) {
			mItems = extras.getStringArrayList("EXTRA_WORDS");
	        Collections.sort(mItems);

	        ContentAdapter adapter = new ContentAdapter(this,
	                android.R.layout.simple_list_item_1, mItems);
	        
	        mListView = (IndexableListView) findViewById(R.id.listview);
	        mListView.setAdapter(adapter);
	        mListView.setFastScrollEnabled(true);
	        mListView.setOnItemClickListener(new IndexableListView.OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
						long arg3) {
					// TODO Auto-generated method stub
					//System.out.println("Elemento en posición: " + arg2);

				}
			});
		}
		else
		{
			System.out.println("NO WORDS");
		}
    }
    
    private String firstToUpper(String input) {
    	return input.substring(0, 1).toUpperCase() + input.substring(1);
    }
    
    private class ContentAdapter extends ArrayAdapter<String> implements SectionIndexer {
    	
    	private String mSections = "#AaBbCcDdEeFfGgHhIiJjKkLlMmNnOoPpQqRrSsTtUuVvWwXxYyZzÜüÄä";
    	
		public ContentAdapter(Context context, int textViewResourceId,
				List<String> objects) {
			super(context, textViewResourceId, objects);
		}

		@Override
		public int getPositionForSection(int section) {
			// If there is no item for current section, previous section will be selected
			for (int i = section; i >= 0; i--) {
				for (int j = 0; j < getCount(); j++) {
					if (i == 0) {
						// For numeric section
						for (int k = 0; k <= 9; k++) {
							if (StringMatcher.match(firstToUpper(String.valueOf(getItem(j).charAt(0))), String.valueOf(k)))
								return j;
						}
					} else {
						if (StringMatcher.match(firstToUpper(String.valueOf(getItem(j).charAt(0))), String.valueOf(mSections.charAt(i))))
							return j;
					}
				}
			}
			return 0;
		}

		@Override
		public int getSectionForPosition(int position) {
			return 0;
		}

		@Override
		public Object[] getSections() {
			String[] sections = new String[mSections.length()];
			for (int i = 0; i < mSections.length(); i++)
				sections[i] = String.valueOf(mSections.charAt(i));
			return sections;
		}
    }
    

}
