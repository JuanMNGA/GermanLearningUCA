package es.uca.tabu;

import java.util.ArrayList;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;

public class ImageCategoriesAdapter extends BaseAdapter {
	private Context context;

	private ArrayList<MarkableImageView> mImageList;

	public ImageCategoriesAdapter(Context c, ArrayList<String> parsedCategories, ArrayList<Integer> parsedIds) {
		context = c;
		Integer idImage;
		mImageList = (ArrayList<MarkableImageView>) new ArrayList<MarkableImageView>();
		MarkableImageView imageView;
		
		// Random Icon
		idImage = TabuUtils.getDrawable(c, adaptResource("random"));
		if(idImage != 0) {
			imageView = new MarkableImageView(context, -1, false);
			imageView.setLayoutParams(new GridView.LayoutParams(125, 125));
			imageView.setScaleType(MarkableImageView.ScaleType.CENTER_CROP);
			imageView.setImageResource(idImage);
			mImageList.add(imageView);
		}

		for(int i=1; i<parsedCategories.size(); i++) {
			idImage = TabuUtils.getDrawable(c, adaptResource(parsedCategories.get(i)));
			if(idImage != 0) {
				imageView = new MarkableImageView(context, parsedIds.get(i), false);
				imageView.setLayoutParams(new GridView.LayoutParams(125, 125));
				imageView.setScaleType(MarkableImageView.ScaleType.CENTER_CROP);
				imageView.setImageResource(idImage);
				mImageList.add(imageView);
				
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
		return mImageList.size();
	}
	
	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return mImageList.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return mImageList.get(position).getId();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		return mImageList.get(position);
	}
}
