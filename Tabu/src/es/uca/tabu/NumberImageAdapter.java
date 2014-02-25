package es.uca.tabu;

import android.content.Context;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;

public class NumberImageAdapter extends BaseAdapter {
	private Context context;

	public NumberImageAdapter(Context c) {
		context = c;
	}


	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return GameManager.getInstance(context).getNumOfQuestions();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		MarkableImageView imageView;
		if (convertView == null) {  // if it's not recycled, initialize some attributes
			imageView = new MarkableImageView(context, position, true);
			imageView.setLayoutParams(new GridView.LayoutParams(125, 125));
			imageView.setScaleType(MarkableImageView.ScaleType.CENTER_CROP);
			//imageView.setPadding(8, 8, 8, 8);
		} else {
			imageView = (MarkableImageView) convertView;
		}
		imageView.setImageResource(TabuUtils.getDrawable(context, "numbers"));
		
		for(Question q : GameManager.getInstance(context).getQuestions()) {
			System.out.println("PREGUNTA: " + q.getId());
		}

		if(GameManager.getInstance(context).getQuestions().get(position).isSuccess())
			imageView.setChecked(true);
		
		return imageView;
	}
}
