package es.uca.tabu;

import java.util.ArrayList;
import java.util.List;

import es.uca.tabu.PlayMenuActivity.Category;
import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;

public class ImageCategoriesAdapter extends BaseAdapter {
	private Context context;

	private ArrayList<MarkableImageView> mImageList;

	public ImageCategoriesAdapter(Context c, List<Category> categoriesModel) {
		context = c;
		mImageList = (ArrayList<MarkableImageView>) new ArrayList<MarkableImageView>();
		Integer idImage;
		MarkableImageView imageView;
		
		// Random Icon
		idImage = TabuUtils.getDrawable(c, adaptResource("random"));
		BitmapDrawable bd = (BitmapDrawable) c.getResources().getDrawable(idImage);
		int w = bd.getBitmap().getWidth();
		int h = bd.getBitmap().getHeight();
		if(idImage != 0) {
			imageView = new MarkableImageView(context, -1, false);
			//imageView.setLayoutParams(new GridView.LayoutParams(125, 125));
			imageView.setLayoutParams(new GridView.LayoutParams(w, h));
			imageView.setScaleType(MarkableImageView.ScaleType.CENTER_CROP);
			imageView.setImageResource(idImage);
			mImageList.add(imageView);
		}
		
		for(Category cat : categoriesModel) {
			idImage = TabuUtils.getDrawable(c, TabuUtils.translateCategory(context, adaptResource(cat.name)));
			if(idImage != 0) {
				bd = (BitmapDrawable) c.getResources().getDrawable(idImage);
				w = bd.getBitmap().getWidth();
				h = bd.getBitmap().getHeight();
				imageView = new MarkableImageView(context, cat.id, false);
				//imageView.setLayoutParams(new GridView.LayoutParams(125, 125));
				imageView.setLayoutParams(new GridView.LayoutParams(w, h));
				imageView.setScaleType(MarkableImageView.ScaleType.CENTER_CROP);
				imageView.setImageResource(idImage);
				mImageList.add(imageView);
				
				System.out.println("Imagen( " + idImage + " ) " + adaptResource(cat.name) + ", added");
			} else
				System.out.println("Imagen( " + idImage + " ) " + adaptResource(cat.name) + ", no existe");
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
