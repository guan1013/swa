package de.shop.util;

import java.util.Date;

public final class DateFormatter {

	public static final int DELAY = 10000;

	public static Date korrigiereDatum(Date date) {
		date.setTime(date.getTime() - DELAY);

		return date;
	}

	private DateFormatter() {

	}
}
