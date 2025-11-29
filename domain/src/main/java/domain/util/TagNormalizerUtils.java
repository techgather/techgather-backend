package domain.util;

import java.util.regex.Pattern;

public class TagNormalizerUtils {

	private static final TagNormalizerUtils INSTANCE = new TagNormalizerUtils();
	private static final Pattern LOWERCASE_PATTERN = Pattern.compile("[a-z]+");

	private TagNormalizerUtils() {
	}

	public static TagNormalizerUtils getInstance() {
		return INSTANCE;
	}

	public String normalize(String tagName) {
		String normalized = tagName.trim().toLowerCase();
		if (isAllLowerCaseLetters(normalized)) {
			return normalized.toUpperCase();
		}
		return normalized;
	}

	private boolean isAllLowerCaseLetters(String str) {
		return LOWERCASE_PATTERN.matcher(str).matches();
	}
}

