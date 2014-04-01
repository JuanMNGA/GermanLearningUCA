package es.uca.tabu;

import java.text.Normalizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import junit.framework.Assert;

import com.google.common.base.Function;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
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
			if(!Character.isLetter(c)) {
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
		AlertDialog dialog = new AlertDialog.Builder(c)
		.setTitle(title)
		.setMessage(message)
		.setPositiveButton(c.getResources().getString(R.string.ok),
				new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				dialog.cancel();
			}})
			.show();
	}

	public static void showImageDialog(String title, String message, final Function<DialogInterface, Void> func, Context c,  int image) {
		AlertDialog dialog = new AlertDialog.Builder(c)
		.setTitle(title)
		.setMessage(message)
		.setIcon(c.getResources().getDrawable(image))
		.setPositiveButton(c.getResources().getString(R.string.ok),
				new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				func.apply(dialog);
			}})
			.show();
	}

	/* Call argument function when button clicked */
	public static void showDialog(String title, String message, final Function<DialogInterface, Void> func, Context c) {
		AlertDialog dialog = new AlertDialog.Builder(c)
		.setTitle(title)
		.setMessage(message)
		.setPositiveButton(c.getResources().getString(R.string.ok),
				new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				func.apply(dialog);
			}})
			.show();
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
	
	public static String accentGerman(String str) {
		String result;
		if(Character.isUpperCase(str.charAt(0))) {
			str.toLowerCase();
			result = str.replaceAll("oe", "ö").replaceAll("ae", "ä").replaceAll("ue", "ü").replaceAll("sz", "ß");
			Character.toUpperCase(result.charAt(0));
		}
		else {
			result = str.replaceAll("oe", "ö").replaceAll("ae", "ä").replaceAll("ue", "ü").replaceAll("sz", "ß");
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
		return container.contains(" ") &&
				ending.compareTo(container.substring(container.lastIndexOf(" ")+1)) == 0;
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
		return incr_text_size;
	}
	
	public static int pxToDp(Context c, int px) {
		DisplayMetrics displaymetrics = c.getResources().getDisplayMetrics();
		return (int)((px * displaymetrics.density) + 0.5);
	}
	
	public static String getArticleColor(String article) {
		switch(article) {
		case "der" :
			return "#FF0000"; //RED
		case "das" :
			return "#0000FF"; //BLUE
		case "die" :
		case "die PL." :
			return "#31B404"; //GREEN
		}
		return "#000000";
	}
}
