package es.uca.tabu;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import junit.framework.Assert;

import com.google.common.base.Function;

import es.uca.tabu.utils.Environment;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.util.DisplayMetrics;
import android.view.Window;
import android.widget.TextView;

public class TabuUtils {

	public static String KEY_SUCCESS = "success";
	public static String KEY_CATEGORIES = "categories";
	public static String KEY_IDS = "ids";

	// Validate name
	// - Length: min 3, max 99
	// - Allowed characters
	public static boolean validateName(String name) {
		return validateLength(name, 3, 99) && isAlpha(name);
	}

	public static boolean validateEmail(String email) {
		Pattern pattern;
		Matcher matcher;

		final String EMAIL_PATTERN = 
				"^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
						+ "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";

		pattern = Pattern.compile(EMAIL_PATTERN);
		matcher = pattern.matcher(email);
		return matcher.matches();
	}

	public static boolean validatePassword(String password) {
		return validateLength(password, 5, 40) && isAlphanumeric(password);
	}

	private static boolean isAlphanumeric(String str) {
		char[] chars = str.toCharArray();

		for (char c : chars) {
			if(!(Character.isLetter(c) || Character.isDigit(c))) {
				return false;
			}
		}

		return true;
	}

	private static boolean isAlpha(String str) {
		char[] chars = str.toCharArray();

		for (char c : chars) {
			if(!Character.isLetter(c) && Character.toString(c).compareTo(" ") != 0) {
				return false;
			}
		}

		return true;
	}

	private static boolean validateLength(String str, int minLength, int maxLength) {
		if(str != null)
			return str.length() >= minLength && str.length() <= maxLength;
			else 
				return false;
	}

	public static void showDialog(String title, String message, Context c) {
		AlertDialog.Builder dialogB = new AlertDialog.Builder(c)
		.setTitle(title)
		.setMessage(message)
		.setPositiveButton(c.getResources().getString(R.string.ok),
				new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				dialog.cancel();
			}});

		AlertDialog dialog = dialogB.create();
		dialog.setCanceledOnTouchOutside(false);
		dialog.show();
	}

	public static void showImageDialog(String title, String message, final Function<DialogInterface, Void> func, Context c,  int image) {
		AlertDialog.Builder dialogB = new AlertDialog.Builder(c)
		.setTitle(title)
		.setMessage(message)
		.setIcon(c.getResources().getDrawable(image))
		.setPositiveButton(c.getResources().getString(R.string.ok),
				new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				func.apply(dialog);
			}});

		AlertDialog dialog = dialogB.create();
		dialog.setCanceledOnTouchOutside(false);
		dialog.show();
	}

	public static void showImageTimedDialog(String title, String message, final Function<DialogInterface, Void> func, Context c,  int image, int time) {
		AlertDialog.Builder dialogB = new AlertDialog.Builder(c)
		.setTitle(title)
		.setMessage(message)
		.setIcon(c.getResources().getDrawable(image))
		.setPositiveButton(c.getResources().getString(R.string.ok),
				new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				func.apply(dialog);
			}});

		final AlertDialog dialog = dialogB.create();
		dialog.setCanceledOnTouchOutside(false);
		dialog.show();

		// Hide after some seconds
		final Handler handler  = new Handler();
		final Runnable runnable = new Runnable() {
			@Override
			public void run() {
				if (dialog.isShowing()) {
					func.apply(dialog);
				}
			}
		};

		dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
			@Override
			public void onDismiss(DialogInterface dialog) {
				handler.removeCallbacks(runnable);
			}
		});

		// Close dialog in time sec
		handler.postDelayed(runnable, 1000 * time);
	}

	/* Call argument function when button clicked */
	public static void showDialog(String title, String message, final Function<DialogInterface, Void> func, Context c) {
		/*AlertDialog dialog = new AlertDialog.Builder(c)
		.setTitle(title)
		.setMessage(message)
		.setPositiveButton(c.getResources().getString(R.string.ok),
				new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				func.apply(dialog);
			}})
			.show();*/


		AlertDialog.Builder dialogB = new AlertDialog.Builder(c)
		.setTitle(title)
		.setMessage(message)
		.setPositiveButton(c.getResources().getString(R.string.ok),
				new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				func.apply(dialog);
			}});

		AlertDialog dialog = dialogB.create();
		dialog.setCanceledOnTouchOutside(false);
		dialog.show();
	}

	/* Call argument function when button clicked */
	public static void showConfirmDialog(String title, String message, final Function<DialogInterface, Void> func, Context c) {
		AlertDialog dialog = new AlertDialog.Builder(c)
		.setTitle(title)
		.setMessage(message)
		.setPositiveButton(c.getResources().getString(R.string.ok),
				new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				func.apply(dialog);
			}})
			.setNegativeButton(c.getResources().getString(R.string.cancel), null)
			.show();
	}

	/* Call argument function when button clicked */
	public static void showConfirmDialog(String title, String message, int positive, int negative, final Function<DialogInterface, Void> func, Context c) {
		AlertDialog dialog = new AlertDialog.Builder(c)
		.setTitle(title)
		.setMessage(message)
		.setPositiveButton(positive,
				new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				func.apply(dialog);
			}})
			.setNegativeButton(negative, null)
			.show();
	}

	public static int getDrawable(Context context, String name)
	{
		try {
			Assert.assertNotNull(context);
			Assert.assertNotNull(name);

			return context.getResources().getIdentifier(deAccent(name),
					"drawable", context.getPackageName());
		} catch (AssertionError e) {
			return -1;
		}
	}

	public static Drawable getDrawable(Context context, String name, String dummy)
	{
		try {
			Assert.assertNotNull(context);
			Assert.assertNotNull(name);

			return context.getResources().getDrawable(getDrawable(context, name));
		} catch (AssertionError e) {
			return null;
		}
	}

	private static String deAccent(String str) {
		String nfdNormalizedString = Normalizer.normalize(str, Normalizer.Form.NFD); 
		Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
		return pattern.matcher(nfdNormalizedString).replaceAll("");
	}

	public static String changeBeta(String str) {
		String result;
		if(Character.isUpperCase(str.charAt(0))) {
			str.toLowerCase();
			result = str.replaceAll("ss", "ß");
			Character.toUpperCase(result.charAt(0));
		}
		else {
			result = str.replaceAll("ss", "ß");
		}
		return result;
	}
	
	public static String inverseChangeBeta(String str) {
		String result;
		if(Character.isUpperCase(str.charAt(0))) {
			str.toLowerCase();
			result = str.replaceAll("ß", "ss");
			Character.toUpperCase(result.charAt(0));
		}
		else {
			result = str.replaceAll("ß", "ss");
		}
		return result;
	}

	public static String deAccentGermanVowel(String str) {
		String result;
		if(Character.isUpperCase(str.charAt(0))) {
			str.toLowerCase();
			result = str.replaceAll("ö", "oe").replaceAll("ä", "ae").replaceAll("ü", "ue");
			Character.toUpperCase(result.charAt(0));
		}
		else {
			result = str.replaceAll("ö", "oe").replaceAll("ä", "ae").replaceAll("ü", "ue");
		}
		return result;
	}
	
	public static String accentGermanVowel(String str) {
		String result;
		if(Character.isUpperCase(str.charAt(0))) {
			str.toLowerCase();
			result = str.replaceAll("oe", "ö").replaceAll("ae", "ä").replaceAll("ue", "ü");
			Character.toUpperCase(result.charAt(0));
		}
		else {
			result = str.replaceAll("oe", "ö").replaceAll("ae", "ä").replaceAll("ue", "ü");
		}
		return result;
	}

	public static boolean isTooLarge (TextView text, String newText) {
		float textWidth = text.getPaint().measureText(newText);
		return (textWidth >= text.getMeasuredWidth ());
	}

	public static boolean beginsBy(String beginning, String container) {
		return container.indexOf(beginning) == 0;
	}

	public static boolean endsBy(String ending, String container) {
		/*if(ending.contains(" "))
			return container.endsWith(ending);
			
		return container.contains(" ") &&
				ending.compareTo(container.substring(container.lastIndexOf(" ")+1)) == 0;*/
		//System.out.println("CONTAINER: '"+container+"'");
		//System.out.println("ENDING: '"+ending+"'");
		return container.endsWith(ending);
	}

	public static int getFontSizeFromBounds(String text, int maxWidth, int maxHeight) {
		Paint paint = new Paint();
		Rect bounds = new Rect();

		int current_w = 0;
		int current_h = 0;

		int incr_text_size = 1;
		boolean found_desired_size = true;

		while (found_desired_size){
			paint.setTextSize(incr_text_size); // Test current text size

			paint.getTextBounds(text, 0, text.length(), bounds); // get min rect from the text

			current_h =  bounds.height();
			current_w =  bounds.width();

			if (maxHeight <= current_h || maxWidth <= current_w){
				found_desired_size = false;
			}
			else
				incr_text_size++;
		}
		return --incr_text_size;
	}

	public static int pxToDp(int px) {
		return (int)((px * Environment.getInstance().getDensity()) + 0.5);
	}

	public static int dpToPx(int dp) {
		return (int)((dp - 0.5) / Environment.getInstance().getDensity());
	}

	public static String getArticleColor(String article) {
		if(article.compareTo("der") == 0)
			return "#FF0000"; //RED
		else if(article.compareTo("das") == 0)
			return "#0000FF"; //BLUE
		else if(article.compareTo("die") == 0)
			return "#31B404"; //GREEN
		else
			return "#000000";
	}

	public static void fillReportReasons(Context c, ArrayList<String> al) {
		al.add(c.getString(R.string.improperContent));
		al.add(c.getString(R.string.offensiveContent));
		al.add(c.getString(R.string.orthographicError));
		al.add(c.getString(R.string.sintaxError));
		al.add(c.getString(R.string.grammarError));
		al.add(c.getString(R.string.difficultDefinition));
		al.add(c.getString(R.string.badAudio));
	}

	@SuppressLint("NewApi")
	public static void hideActionBar(Context c) {
		//Hide Action bar
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			((Activity) c).getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
			((Activity) c).getActionBar().hide();
		}
		else {
			((Activity) c).getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
			android.support.v7.app.ActionBar actionBar = ((ActionBarActivity) c).getSupportActionBar();
			actionBar.hide();
		}
	}
	
	public static String getPercentColor(float number) {
		if(number<40f)
			return "#FF0000"; //RED
		else if(number<65f)
			return "#FFE600"; //YELLOW
		else
			return "#9CCB19"; //GREEN
	}
	
	public static String translateCategory(Context c, String category) {
		Locale current = c.getResources().getConfiguration().locale;
		if(current.getCountry().compareTo("en_gb") != 0) {
			Map<String, String> englishCategories = new HashMap<String, String>();
			englishCategories.put("food_drink", "essen_trinken");
			englishCategories.put("weather_seasons", "wetter_jahreszeiten");
			englishCategories.put("weeks_months", "wochentage_monate");
			englishCategories.put("free_time", "freizeit");
			englishCategories.put("appearance", "aussehen");
			englishCategories.put("personality", "charaktereigenschaften");
			englishCategories.put("jobs", "berufe");
			englishCategories.put("opinions", "eine_meinung_aussern");
			englishCategories.put("home", "wohnung");
			englishCategories.put("clothes_fashion", "kleidung_mode");
			englishCategories.put("countries_languages_nationalities", "lander_sprachen_nationalitat");
			englishCategories.put("studies", "studium_universitat");
			englishCategories.put("family_friends", "familie_freunde");
			englishCategories.put("meeting_a_person", "begrussen_sich_vorstellen");
			englishCategories.put("feelings", "gefuhle_befinden");
			englishCategories.put("city", "stadt");
			englishCategories.put("daily_life", "tagesablauf");
			
			if(englishCategories.containsKey(category))
				return englishCategories.get(category);
		}
		else if(current.getCountry().compareTo("ru") != 0) {
			Map<String, String> russianCategories = new HashMap<String, String>();
			russianCategories.put("еда_и_напитки", "essen_trinken");
			russianCategories.put("погода_и_времена_года", "wetter_jahreszeiten");
			russianCategories.put("свободное_время", "freizeit");
			russianCategories.put("внешность", "aussehen");
			russianCategories.put("характер", "charaktereigenschaften");
			russianCategories.put("профессии", "berufe");
			russianCategories.put("Мнения", "eine_meinung_aussern");
			russianCategories.put("дом", "wohnung");
			russianCategories.put("Одежда__мода", "kleidung_mode");
			russianCategories.put("страны_языки_национальност", "lander_sprachen_nationalitat");
			russianCategories.put("обучение", "studium_universitat");
			russianCategories.put("семья_друзья", "familie_freunde");
			russianCategories.put("при_встрече", "begrussen_sich_vorstellen");
			russianCategories.put("чувства", "gefuhle_befinden");
			russianCategories.put("город", "stadt");
			
			if(russianCategories.containsKey(category))
				return russianCategories.get(category);
		}
		return category;

	}
	
	public static void updateLanguage(Context c) {
		SharedPreferences loginPreferences;
		
		final Resources res = c.getResources();
		// Change locale settings in the app.
		final DisplayMetrics dm = res.getDisplayMetrics();
		final android.content.res.Configuration conf = res.getConfiguration();
		
		loginPreferences = c.getSharedPreferences("loginPrefs", c.MODE_PRIVATE);

		conf.locale = new Locale(loginPreferences.getString("language", ""));
		res.updateConfiguration(conf, dm);
	}
}
