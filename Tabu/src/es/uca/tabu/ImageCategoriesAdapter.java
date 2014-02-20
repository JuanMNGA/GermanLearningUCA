package es.uca.tabu;

import java.util.ArrayList;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;

public class ImageCategoriesAdapter extends BaseAdapter {
	private Context context;

	// (id, drawable)
	private ArrayList<Pair<Integer, Integer>> gallery;

	public ImageCategoriesAdapter(Context c, ArrayList<String> parsedCategories, ArrayList<Integer> parsedIds) {
		context = c;
		Integer idImage;
		gallery = (ArrayList<Pair<Integer, Integer>>) new ArrayList<Pair<Integer, Integer>>();
		
		// Random Icon
		idImage = TabuUtils.getDrawable(c, adaptResource("random"));
		if(idImage != 0) {
			gallery.add(new Pair(-1,idImage));
		}

		for(int i=1; i<parsedCategories.size(); i++) {
			idImage = TabuUtils.getDrawable(c, adaptResource(parsedCategories.get(i)));
			if(idImage != 0) {
				gallery.add(new Pair(parsedIds.get(i), idImage));
				System.out.println("Imagen( " + idImage + " ) " + adaptResource(parsedCategories.get(i)) + ", añadida");
			} else
				System.out.println("Imagen( " + idImage + " ) " + adaptResource(parsedCategories.get(i)) + ", no existe");
		}
	}

	private String adaptResource(String str) {
		return str.replaceAll(" ", "_").replaceAll("-", "_").toLowerCase(context.getResources().getConfiguration().locale);
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return gallery.size();
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
			imageView = new MarkableImageView(context, gallery.get(position).getFirst(), false);
			imageView.setLayoutParams(new GridView.LayoutParams(125, 125));
			imageView.setScaleType(MarkableImageView.ScaleType.CENTER_CROP);
			//imageView.setPadding(8, 8, 8, 8);
		} else {
			imageView = (MarkableImageView) convertView;
		}
		imageView.setImageResource(gallery.get(position).getSecond());
		return imageView;
	}
}
