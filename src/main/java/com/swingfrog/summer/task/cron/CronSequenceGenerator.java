package com.swingfrog.summer.task.cron;

import java.util.*;

/**
 * Copied from Spring :)
 *
 * <p>
 * Date sequence generator for a
 * <a href="http://www.manpagez.com/man/5/crontab/">Crontab pattern</a>,
 * allowing clients to specify a pattern that the sequence matches.
 * <p>
 * <p>The pattern is a list of six single space-separated fields: representing
 * second, minute, hour, day, month, weekday. Month and weekday names can be
 * given as the first three letters of the English names.
 * <p>
 * <p>Example patterns:
 * <ul>
 * <li>"0 0 * * * *" = the top of every hour of every day.</li>
 * <li>"*&#47;10 * * * * *" = every ten seconds.</li>
 * <li>"0 0 8-10 * * *" = 8, 9 and 10 o'clock of every day.</li>
 * <li>"0 0 6,19 * * *" = 6:00 AM and 7:00 PM every day.</li>
 * <li>"0 0/30 8-10 * * *" = 8:00, 8:30, 9:00, 9:30, 10:00 and 10:30 every day.</li>
 * <li>"0 0 9-17 * * MON-FRI" = on the hour nine-to-five weekdays</li>
 * <li>"0 0 0 25 12 ?" = every Christmas Day at midnight</li>
 * </ul>
 */
public class CronSequenceGenerator {

	private final String expression;

	private final TimeZone timeZone;

	private final BitSet months = new BitSet(12);

	private final BitSet daysOfMonth = new BitSet(31);

	private final BitSet daysOfWeek = new BitSet(7);

	private final BitSet hours = new BitSet(24);

	private final BitSet minutes = new BitSet(60);

	private final BitSet seconds = new BitSet(60);


	/**
	 * Construct a {@link CronSequenceGenerator} from the pattern provided,
	 * using the default {@link TimeZone}.
	 *
	 * @param expression a space-separated list of time fields
	 * @throws IllegalArgumentException if the pattern cannot be parsed
	 * @see TimeZone#getDefault()
	 */
	public CronSequenceGenerator(String expression) {
		this(expression, TimeZone.getDefault());
	}

	/**
	 * Construct a {@link CronSequenceGenerator} from the pattern provided,
	 * using the specified {@link TimeZone}.
	 *
	 * @param expression a space-separated list of time fields
	 * @param timeZone   the TimeZone to use for generated trigger times
	 * @throws IllegalArgumentException if the pattern cannot be parsed
	 */
	public CronSequenceGenerator(String expression, TimeZone timeZone) {
		this.expression = expression;
		this.timeZone = timeZone;
		parse(expression);
	}

	private CronSequenceGenerator(String expression, String[] fields) {
		this.expression = expression;
		this.timeZone = null;
		doParse(fields);
	}

	/**
	 * Determine whether the specified expression represents a valid cron pattern.
	 *
	 * @param expression the expression to evaluate
	 * @return {@code true} if the given expression is a valid cron expression
	 * @since 4.3
	 */
	public static boolean isValidExpression(String expression) {
		if (expression == null) {
			return false;
		}
		String[] fields = tokenizeToStringArray(expression, " ");
		if (!areValidCronFields(fields)) {
			return false;
		}
		try {
			new CronSequenceGenerator(expression, fields);
			return true;
		} catch (IllegalArgumentException ex) {
			return false;
		}
	}

	private static boolean areValidCronFields(String[] fields) {
		return (fields != null && fields.length == 6);
	}

	/**
	 * Return the cron pattern that this sequence generator has been built for.
	 */
	String getExpression() {
		return this.expression;
	}

	/**
	 * Get the next {@link Date} in the sequence matching the Cron pattern and
	 * after the value provided. The return value will have a whole number of
	 * seconds, and will be after the input value.
	 *
	 * @param date a seed value
	 * @return the next value matching the pattern
	 */
	public Date next(Date date) {
		/*
		The plan:

		1 Start with whole second (rounding up if necessary)

		2 If seconds match move on, otherwise find the next match:
		2.1 If next match is in the next minute then roll forwards

		3 If minute matches move on, otherwise find the next match
		3.1 If next match is in the next hour then roll forwards
		3.2 Reset the seconds and go to 2

		4 If hour matches move on, otherwise find the next match
		4.1 If next match is in the next day then roll forwards,
		4.2 Reset the minutes and seconds and go to 2
		*/

		Calendar calendar = new GregorianCalendar();
		calendar.setTimeZone(this.timeZone);
		calendar.setTime(date);

		// First, just reset the milliseconds and try to calculate from there...
		calendar.set(Calendar.MILLISECOND, 0);
		long originalTimestamp = calendar.getTimeInMillis();
		doNext(calendar, calendar.get(Calendar.YEAR));

		if (calendar.getTimeInMillis() == originalTimestamp) {
			// We arrived at the original timestamp - round up to the next whole second and try again...
			calendar.add(Calendar.SECOND, 1);
			doNext(calendar, calendar.get(Calendar.YEAR));
		}

		return calendar.getTime();
	}

	private void doNext(Calendar calendar, int dot) {
		List<Integer> resets = new ArrayList<>();

		int second = calendar.get(Calendar.SECOND);
		List<Integer> emptyList = Collections.emptyList();
		int updateSecond = findNext(this.seconds, second, calendar, Calendar.SECOND, Calendar.MINUTE, emptyList);
		if (second == updateSecond) {
			resets.add(Calendar.SECOND);
		}

		int minute = calendar.get(Calendar.MINUTE);
		int updateMinute = findNext(this.minutes, minute, calendar, Calendar.MINUTE, Calendar.HOUR_OF_DAY, resets);
		if (minute == updateMinute) {
			resets.add(Calendar.MINUTE);
		} else {
			doNext(calendar, dot);
		}

		int hour = calendar.get(Calendar.HOUR_OF_DAY);
		int updateHour = findNext(this.hours, hour, calendar, Calendar.HOUR_OF_DAY, Calendar.DAY_OF_WEEK, resets);
		if (hour == updateHour) {
			resets.add(Calendar.HOUR_OF_DAY);
		} else {
			doNext(calendar, dot);
		}

		int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
		int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
		int updateDayOfMonth = findNextDay(calendar, this.daysOfMonth, dayOfMonth, daysOfWeek, dayOfWeek, resets);
		if (dayOfMonth == updateDayOfMonth) {
			resets.add(Calendar.DAY_OF_MONTH);
		} else {
			doNext(calendar, dot);
		}

		int month = calendar.get(Calendar.MONTH);
		int updateMonth = findNext(this.months, month, calendar, Calendar.MONTH, Calendar.YEAR, resets);
		if (month != updateMonth) {
			if (calendar.get(Calendar.YEAR) - dot > 4) {
				throw new IllegalArgumentException("Invalid cron expression \"" + this.expression +
						"\" led to runaway search for next trigger");
			}
			doNext(calendar, dot);
		}

	}

	private int findNextDay(Calendar calendar, BitSet daysOfMonth, int dayOfMonth, BitSet daysOfWeek, int dayOfWeek,
							List<Integer> resets) {

		int count = 0;
		int max = 366;
		// the DAY_OF_WEEK values in java.util.Calendar start with 1 (Sunday),
		// but in the cron pattern, they start with 0, so we subtract 1 here
		while ((!daysOfMonth.get(dayOfMonth) || !daysOfWeek.get(dayOfWeek - 1)) && count++ < max) {
			calendar.add(Calendar.DAY_OF_MONTH, 1);
			dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
			dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
			reset(calendar, resets);
		}
		if (count >= max) {
			throw new IllegalArgumentException("Overflow in day for expression \"" + this.expression + "\"");
		}
		return dayOfMonth;
	}


	// Parsing logic invoked by the constructor

	/**
	 * Search the bits provided for the next set bit after the value provided,
	 * and reset the calendar.
	 *
	 * @param bits        a {@link BitSet} representing the allowed values of the field
	 * @param value       the current value of the field
	 * @param calendar    the calendar to increment as we move through the bits
	 * @param field       the field to increment in the calendar (@see
	 *                    {@link Calendar} for the static constants defining valid fields)
	 * @param lowerOrders the Calendar field ids that should be reset (i.e. the
	 *                    ones of lower significance than the field of interest)
	 * @return the value of the calendar field that is next in the sequence
	 */
	private int findNext(BitSet bits, int value, Calendar calendar, int field, int nextField, List<Integer> lowerOrders) {
		int nextValue = bits.nextSetBit(value);
		// roll over if needed
		if (nextValue == -1) {
			calendar.add(nextField, 1);
			reset(calendar, Collections.singletonList(field));
			nextValue = bits.nextSetBit(0);
		}
		if (nextValue != value) {
			calendar.set(field, nextValue);
			reset(calendar, lowerOrders);
		}
		return nextValue;
	}

	/**
	 * Reset the calendar setting all the fields provided to zero.
	 */
	private void reset(Calendar calendar, List<Integer> fields) {
		for (int field : fields) {
			calendar.set(field, field == Calendar.DAY_OF_MONTH ? 1 : 0);
		}
	}

	/**
	 * Parse the given pattern expression.
	 */
	private void parse(String expression) throws IllegalArgumentException {
		String[] fields = tokenizeToStringArray(expression, " ");
		if (!areValidCronFields(fields)) {
			throw new IllegalArgumentException(String.format(
					"Cron expression must consist of 6 fields (found %d in \"%s\")", fields.length, expression));
		}
		doParse(fields);
	}

	private void doParse(String[] fields) {
		setNumberHits(this.seconds, fields[0], 0, 60);
		setNumberHits(this.minutes, fields[1], 0, 60);
		setNumberHits(this.hours, fields[2], 0, 24);
		setDaysOfMonth(this.daysOfMonth, fields[3]);
		setMonths(this.months, fields[4]);
		setDays(this.daysOfWeek, replaceOrdinals(fields[5], "SUN,MON,TUE,WED,THU,FRI,SAT"), 8);

		if (this.daysOfWeek.get(7)) {
			// Sunday can be represented as 0 or 7
			this.daysOfWeek.set(0);
			this.daysOfWeek.clear(7);
		}
	}

	/**
	 * Replace the values in the comma-separated list (case insensitive)
	 * with their index in the list.
	 *
	 * @return a new String with the values from the list replaced
	 */
	private String replaceOrdinals(String value, String commaSeparatedList) {
		String[] list = commaDelimitedListToStringArray(commaSeparatedList);
		for (int i = 0; i < list.length; i++) {
			String item = list[i].toUpperCase();
			value = replace(value.toUpperCase(), item, "" + i);
		}
		return value;
	}

	private void setDaysOfMonth(BitSet bits, String field) {
		int max = 31;
		// Days of month start with 1 (in Cron and Calendar) so add one
		setDays(bits, field, max + 1);
		// ... and remove it from the front
		bits.clear(0);
	}

	private void setDays(BitSet bits, String field, int max) {
		if (field.contains("?")) {
			field = "*";
		}
		setNumberHits(bits, field, 0, max);
	}

	private void setMonths(BitSet bits, String value) {
		int max = 12;
		value = replaceOrdinals(value, "FOO,JAN,FEB,MAR,APR,MAY,JUN,JUL,AUG,SEP,OCT,NOV,DEC");
		BitSet months = new BitSet(13);
		// Months start with 1 in Cron and 0 in Calendar, so push the values first into a longer bit set
		setNumberHits(months, value, 1, max + 1);
		// ... and then rotate it to the front of the months
		for (int i = 1; i <= max; i++) {
			if (months.get(i)) {
				bits.set(i - 1);
			}
		}
	}

	private void setNumberHits(BitSet bits, String value, int min, int max) {
		String[] fields = delimitedListToStringArray(value, ",");
		for (String field : fields) {
			if (!field.contains("/")) {
				// Not an incrementer so it must be a range (possibly empty)
				int[] range = getRange(field, min, max);
				bits.set(range[0], range[1] + 1);
			} else {
				String[] split = delimitedListToStringArray(field, "/");
				if (split.length > 2) {
					throw new IllegalArgumentException("Incrementer has more than two fields: '" +
							field + "' in expression \"" + this.expression + "\"");
				}
				int[] range = getRange(split[0], min, max);
				if (!split[0].contains("-")) {
					range[1] = max - 1;
				}
				int delta = Integer.parseInt(split[1]);
				if (delta <= 0) {
					throw new IllegalArgumentException("Incrementer delta must be 1 or higher: '" +
							field + "' in expression \"" + this.expression + "\"");
				}
				for (int i = range[0]; i <= range[1]; i += delta) {
					bits.set(i);
				}
			}
		}
	}

	private int[] getRange(String field, int min, int max) {
		int[] result = new int[2];
		if (field.contains("*")) {
			result[0] = min;
			result[1] = max - 1;
			return result;
		}
		if (!field.contains("-")) {
			result[0] = result[1] = Integer.parseInt(field);
		} else {
			String[] split = delimitedListToStringArray(field, "-");
			if (split.length > 2) {
				throw new IllegalArgumentException("Range has more than two fields: '" +
						field + "' in expression \"" + this.expression + "\"");
			}
			result[0] = Integer.parseInt(split[0]);
			result[1] = Integer.parseInt(split[1]);
		}
		if (result[0] >= max || result[1] >= max) {
			throw new IllegalArgumentException("Range exceeds maximum (" + max + "): '" +
					field + "' in expression \"" + this.expression + "\"");
		}
		if (result[0] < min || result[1] < min) {
			throw new IllegalArgumentException("Range less than minimum (" + min + "): '" +
					field + "' in expression \"" + this.expression + "\"");
		}
		if (result[0] > result[1]) {
			throw new IllegalArgumentException("Invalid inverted range: '" + field +
					"' in expression \"" + this.expression + "\"");
		}
		return result;
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof CronSequenceGenerator)) {
			return false;
		}
		CronSequenceGenerator otherCron = (CronSequenceGenerator) other;
		return (this.months.equals(otherCron.months) && this.daysOfMonth.equals(otherCron.daysOfMonth) &&
				this.daysOfWeek.equals(otherCron.daysOfWeek) && this.hours.equals(otherCron.hours) &&
				this.minutes.equals(otherCron.minutes) && this.seconds.equals(otherCron.seconds));
	}

	@Override
	public int hashCode() {
		return (17 * this.months.hashCode() + 29 * this.daysOfMonth.hashCode() + 37 * this.daysOfWeek.hashCode() +
				41 * this.hours.hashCode() + 53 * this.minutes.hashCode() + 61 * this.seconds.hashCode());
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + ": " + this.expression;
	}



	/**
	 * Check that the given {@code String} is neither {@code null} nor of length 0.
	 * <p>Note: this method returns {@code true} for a {@code String} that
	 * purely consists of whitespace.
	 * @param str the {@code String} to check (may be {@code null})
	 * @return {@code true} if the {@code String} is not {@code null} and has length
	 */
	private static boolean hasLength(String str) {
		return (str != null && !str.isEmpty());
	}

	/**
	 * Replace all occurrences of a substring within a string with another string.
	 * @param inString {@code String} to examine
	 * @param oldPattern {@code String} to replace
	 * @param newPattern {@code String} to insert
	 * @return a {@code String} with the replacements
	 */
	private static String replace(String inString, String oldPattern, String newPattern) {
		if (!hasLength(inString) || !hasLength(oldPattern) || newPattern == null) {
			return inString;
		}
		int index = inString.indexOf(oldPattern);
		if (index == -1) {
			// no occurrence -> can return input as-is
			return inString;
		}

		int capacity = inString.length();
		if (newPattern.length() > oldPattern.length()) {
			capacity += 16;
		}
		StringBuilder sb = new StringBuilder(capacity);

		int pos = 0;  // our position in the old string
		int patLen = oldPattern.length();
		while (index >= 0) {
			sb.append(inString.substring(pos, index));
			sb.append(newPattern);
			pos = index + patLen;
			index = inString.indexOf(oldPattern, pos);
		}

		// append any characters to the right of a match
		sb.append(inString.substring(pos));
		return sb.toString();
	}

	/**
	 * Delete any character in a given {@code String}.
	 * @param inString the original {@code String}
	 * @param charsToDelete a set of characters to delete.
	 * E.g. "az\n" will delete 'a's, 'z's and new lines.
	 * @return the resulting {@code String}
	 */
	private static String deleteAny(String inString, String charsToDelete) {
		if (!hasLength(inString) || !hasLength(charsToDelete)) {
			return inString;
		}

		StringBuilder sb = new StringBuilder(inString.length());
		for (int i = 0; i < inString.length(); i++) {
			char c = inString.charAt(i);
			if (charsToDelete.indexOf(c) == -1) {
				sb.append(c);
			}
		}
		return sb.toString();
	}

	/**
	 * Copy the given {@code Collection} into a {@code String} array.
	 * <p>The {@code Collection} must contain {@code String} elements only.
	 * @param collection the {@code Collection} to copy
	 * @return the {@code String} array
	 */
	private static String[] toStringArray(Collection<String> collection) {
		return collection.toArray(new String[collection.size()]);
	}

	/**
	 * Tokenize the given {@code String} into a {@code String} array via a
	 * {@link StringTokenizer}.
	 * <p>Trims tokens and omits empty tokens.
	 * <p>The given {@code delimiters} string can consist of any number of
	 * delimiter characters. Each of those characters can be used to separate
	 * tokens. A delimiter is always a single character; for multi-character
	 * delimiters, consider using {@link #delimitedListToStringArray}.
	 * @param str the {@code String} to tokenize
	 * @param delimiters the delimiter characters, assembled as a {@code String}
	 * (each of the characters is individually considered as a delimiter)
	 * @return an array of the tokens
	 * @see StringTokenizer
	 * @see String#trim()
	 * @see #delimitedListToStringArray
	 */
	private static String[] tokenizeToStringArray(String str, String delimiters) {
		return tokenizeToStringArray(str, delimiters, true, true);
	}

	/**
	 * Tokenize the given {@code String} into a {@code String} array via a
	 * {@link StringTokenizer}.
	 * <p>The given {@code delimiters} string can consist of any number of
	 * delimiter characters. Each of those characters can be used to separate
	 * tokens. A delimiter is always a single character; for multi-character
	 * delimiters, consider using {@link #delimitedListToStringArray}.
	 * @param str the {@code String} to tokenize
	 * @param delimiters the delimiter characters, assembled as a {@code String}
	 * (each of the characters is individually considered as a delimiter)
	 * @param trimTokens trim the tokens via {@link String#trim()}
	 * @param ignoreEmptyTokens omit empty tokens from the result array
	 * (only applies to tokens that are empty after trimming; StringTokenizer
	 * will not consider subsequent delimiters as token in the first place).
	 * @return an array of the tokens
	 * @see StringTokenizer
	 * @see String#trim()
	 * @see #delimitedListToStringArray
	 */
	private static String[] tokenizeToStringArray(
			String str, String delimiters, boolean trimTokens, boolean ignoreEmptyTokens) {

		if (str == null) {
			return new String[0];
		}

		StringTokenizer st = new StringTokenizer(str, delimiters);
		List<String> tokens = new ArrayList<>();
		while (st.hasMoreTokens()) {
			String token = st.nextToken();
			if (trimTokens) {
				token = token.trim();
			}
			if (!ignoreEmptyTokens || token.length() > 0) {
				tokens.add(token);
			}
		}
		return toStringArray(tokens);
	}

	/**
	 * Take a {@code String} that is a delimited list and convert it into a
	 * {@code String} array.
	 * <p>A single {@code delimiter} may consist of more than one character,
	 * but it will still be considered as a single delimiter string, rather
	 * than as bunch of potential delimiter characters, in contrast to
	 * {@link #tokenizeToStringArray}.
	 * @param str the input {@code String}
	 * @param delimiter the delimiter between elements (this is a single delimiter,
	 * rather than a bunch individual delimiter characters)
	 * @return an array of the tokens in the list
	 * @see #tokenizeToStringArray
	 */
	private static String[] delimitedListToStringArray(String str, String delimiter) {
		return delimitedListToStringArray(str, delimiter, null);
	}

	/**
	 * Take a {@code String} that is a delimited list and convert it into
	 * a {@code String} array.
	 * <p>A single {@code delimiter} may consist of more than one character,
	 * but it will still be considered as a single delimiter string, rather
	 * than as bunch of potential delimiter characters, in contrast to
	 * {@link #tokenizeToStringArray}.
	 * @param str the input {@code String}
	 * @param delimiter the delimiter between elements (this is a single delimiter,
	 * rather than a bunch individual delimiter characters)
	 * @param charsToDelete a set of characters to delete; useful for deleting unwanted
	 * line breaks: e.g. "\r\n\f" will delete all new lines and line feeds in a {@code String}
	 * @return an array of the tokens in the list
	 * @see #tokenizeToStringArray
	 */
	private static String[] delimitedListToStringArray(String str, String delimiter, String charsToDelete) {

		if (str == null) {
			return new String[0];
		}
		if (delimiter == null) {
			return new String[] {str};
		}

		List<String> result = new ArrayList<>();
		if ("".equals(delimiter)) {
			for (int i = 0; i < str.length(); i++) {
				result.add(deleteAny(str.substring(i, i + 1), charsToDelete));
			}
		}
		else {
			int pos = 0;
			int delPos;
			while ((delPos = str.indexOf(delimiter, pos)) != -1) {
				result.add(deleteAny(str.substring(pos, delPos), charsToDelete));
				pos = delPos + delimiter.length();
			}
			if (str.length() > 0 && pos <= str.length()) {
				// Add rest of String, but not in case of empty input.
				result.add(deleteAny(str.substring(pos), charsToDelete));
			}
		}
		return toStringArray(result);
	}

	/**
	 * Convert a comma delimited list (e.g., a row from a CSV file) into an
	 * array of strings.
	 * @param str the input {@code String}
	 * @return an array of strings, or the empty array in case of empty input
	 */
	private static String[] commaDelimitedListToStringArray(String str) {
		return delimitedListToStringArray(str, ",");
	}

}

