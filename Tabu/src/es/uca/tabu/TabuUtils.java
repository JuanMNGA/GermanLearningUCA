package es.uca.tabu;

import java.text.Normalizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import junit.framework.Assert;

import com.google.common.base.Function;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;

public class TabuUtils {

	public static String KEY_SUCCESS = "success";

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
			result = str.replaceAll("oe", "�").replaceAll("ae", "�").replaceAll("ue", "�").replaceAll("sz", "�");
			Character.toUpperCase(result.charAt(0));
		}
		else {
			result = str.replaceAll("oe", "�").replaceAll("ae", "�").replaceAll("ue", "�").replaceAll("sz", "�");
		}
		return result;
	}
	
	
}
