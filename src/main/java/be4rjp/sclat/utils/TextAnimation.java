package be4rjp.sclat.utils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class TextAnimation {

	private static final Set<Character> SMALLS = new HashSet<>(
			Arrays.asList('.', '|', 'i', '!', '！', '/', '1', ' ', 'l'));

	private final String text;
	private final int length;

	private int index = 0;

	public TextAnimation(String text, int length) {
		this.text = text + text + text + text;
		this.length = length;
	}

	public String next() {

		String line = text.substring(index, index + length);

		int plus = 0;
		int hankaku = 0;

		char[] chars = line.toCharArray();
		for (char aChar : chars) {
			if (SMALLS.contains(aChar)) {
				plus++;
			} else if (String.valueOf(aChar).getBytes().length < 2) {
				hankaku++;
			}
		}

		line = text.substring(index, index + length + plus + hankaku / 2);

		index++;
		if (index == text.length() / 4)
			index = 0;

		return line;
	}
}
