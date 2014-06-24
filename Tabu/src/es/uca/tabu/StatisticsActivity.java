package es.uca.tabu;

import es.uca.tabu.utils.MyHorizontalScrollView;
import es.uca.tabu.utils.MyHorizontalScrollView.SizeCallback;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.support.v7.app.ActionBarActivity;

public class StatisticsActivity extends ActionBarActivity {

	static ImageView btnSlide;
	static ClickListenerForScrolling clfs;
	View menu;
	ListView list;

	@SuppressLint("NewApi")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		TabuUtils.hideActionBar(this);

		LayoutInflater inflater = LayoutInflater.from(this);
		setContentView(R.layout.horz_scroll_menu);

		MyHorizontalScrollView scrollView = (MyHorizontalScrollView) findViewById(R.id.menuScrollView);
		menu = (View) findViewById(R.id.menulist);
		final View app = inflater.inflate(R.layout.individual_activity_statistics, null);
		app.setOnClickListener(clfs);

		/*ListView listView = (ListView) app.findViewById(R.id.list);
		ViewUtils.initListView(this, listView, "Item ", 30,
				android.R.layout.simple_list_item_1);
		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				app.performClick();
			}
		});*/

		/*ViewUtils.initListView(this, menu, "Activity ", 3,
				android.R.layout.simple_list_item_1);

		menu.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int pos,
					long arg3) {

				clfs.performClick(pos + 1);
				// activity indices start at 1

			}
		});*/

		// Header button to show left menu
		ViewGroup tabBar = (ViewGroup) app.findViewById(R.id.tabBar);

		// Navigation items
		final String[] nombres = {"Individual statistics"/*, "Course statistics"*/};
		Integer[] imagenes = {R.drawable.image1/*, R.drawable.image1*/};

		CustomMenuList adapter = new CustomMenuList(StatisticsActivity.this, nombres, imagenes);
		list=(ListView)menu.findViewById(R.id.list);
		list.setAdapter(adapter);
		list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				clfs.performClick(position + 1);
				//Toast.makeText(StatisticsActivity.this, "You Clicked at " +nombres[+ position], Toast.LENGTH_SHORT).show();
			}
		});


		btnSlide = (ImageView) app.findViewById(R.id.BtnSlide);
		clfs = new ClickListenerForScrolling(scrollView, menu);
		app.setOnClickListener(clfs);

		// Create a transparent view that pushes the other views in the HSV to
		// the right.
		// This transparent view allows the menu to be shown when the HSV is
		// scrolled.
		View transparent = new TextView(this);
		transparent.setBackgroundColor(Color.TRANSPARENT);

		final View[] children = new View[] { transparent, app };

		// Scroll to menu (view[0]) when layout finished.
		int scrollToViewIdx = 0;
		((ViewGroup) scrollView.getChildAt(0)).removeAllViews();
		scrollView.initViews(children, scrollToViewIdx,
				new SizeCallbackForMenu(btnSlide));
	}

	@Override
	public void onBackPressed() {
		Intent mainmenu = new Intent(getApplicationContext(), MainMenuActivity.class);
		mainmenu.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(mainmenu);
		/**
		 * Close Login Screen
		 **/
		finish();
	}
	


	@Override
	protected void onResume() {
		super.onResume();
		overridePendingTransition(0, 0);
		TabuUtils.updateLanguage(this);

	}

	/**
	 * Helper for examples with a HSV that should be scrolled by a menu View's
	 * width.
	 */
	public static class ClickListenerForScrolling implements OnClickListener {
		private MyHorizontalScrollView scrollView;
		private View menu;
		private Bitmap appCache;
		private ImageView app;
		private int show = 0; // determines which activity to show
		private int statusBarHeight = 0;

		/**
		 * Menu must NOT be out/shown to start with.
		 */
		// SHOWN BY DEFAULT
		boolean menuOut = true;

		public ClickListenerForScrolling(MyHorizontalScrollView scrollView,
				View menu) {
			super();
			this.scrollView = scrollView;
			this.menu = menu;
			this.app = new ImageView(menu.getContext());
			this.app.setOnClickListener(this);
			this.statusBarHeight = (int) Math.ceil(25 * menu.getContext()
					.getResources().getDisplayMetrics().density);
		}

		public int getShow() {
			return show;
		}

		public void setShow(int i) {
			show = i;
		}

		public void setCache(Bitmap bmp) {
			if (appCache != null) {
				appCache.recycle();
				appCache = null;
			}
			appCache = bmp;
			app.setImageBitmap(appCache);
			app.setPadding(0, -statusBarHeight, 0, 0);

			// Create a transparent view that pushes the other views in the HSV
			// to the right.
			// This transparent view allows the menu to be shown when the HSV is
			// scrolled.
			View transparent = new TextView(menu.getContext());
			transparent.setBackgroundColor(Color.TRANSPARENT);

			View[] children = new View[] { transparent, app };

			// Scroll to menu (view[0]) when layout finished.
			int scrollToViewIdx = 0;
			((ViewGroup) scrollView.getChildAt(0)).removeAllViews();
			scrollView.initViews(children, scrollToViewIdx,
					new SizeCallbackForMenu(btnSlide));
		}

		public void performClick(int i, View v) {
			show = i;
			onClick(v);
		}

		public void performClick(int i) {
			performClick(i, null);
		}

		public void onClick() {
			onClick(null);
		}

		public void inmediateChangeTo(int i) {
			Context context = menu.getContext();
			
			Class<?> cls = IndividualStatistics.class;
			if (i== PlayStatistics.ACTIVITY_NO)
				cls = PlayStatistics.class;
			else if(i == DefStatistics.ACTIVITY_NO) {
				cls = DefStatistics.class;
				System.out.println("DEF SHOWN");
			}
			context.startActivity(new Intent(context, cls)
			.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));
		}
		
		@Override
		public void onClick(View v) {
			Context context = menu.getContext();

			int menuWidth = menu.getMeasuredWidth();

			// Ensure menu is visible
			menu.setVisibility(View.VISIBLE);

			if (!menuOut) {
				context.startActivity(new Intent(context,
						StatisticsActivity.class)
				.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));

				// Scroll to 0 to reveal menu
				int left = 0;
				scrollView.smoothScrollTo(left, 0);

			} else {
				// Scroll to menuWidth so menu isn't on screen.
				int left = menuWidth;
				scrollView.smoothScrollTo(left, 0);

				Class<?> cls = IndividualStatistics.class;
				if (show == PlayStatistics.ACTIVITY_NO)
					cls = PlayStatistics.class;
				else if(show == DefStatistics.ACTIVITY_NO)
					cls = DefStatistics.class;

				context.startActivity(new Intent(context, cls)
				.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));

			}
			menuOut = !menuOut;
		}
	}
	/**
	 * Helper that remembers the width of the 'slide' button, so that the 'slide' button remains in view, even when the menu is
	 * showing.
	 */
	public static class SizeCallbackForMenu implements SizeCallback {
		int btnWidth;
		View btnSlide;

		public SizeCallbackForMenu(View btnSlide) {
			super();
			this.btnSlide = btnSlide;
		}

		@Override
		public void onGlobalLayout() {
			btnWidth = btnSlide.getMeasuredWidth();
			//System.out.println("btnWidth=" + btnWidth);
		}

		@Override
		public void getViewSize(int idx, int w, int h, int[] dims) {
			dims[0] = w;
			dims[1] = h;
			final int menuIdx = 0;
			if (idx == menuIdx) {
				dims[0] = w - btnWidth;
			}
		}
	}

}
