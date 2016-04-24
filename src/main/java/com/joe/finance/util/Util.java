package com.joe.finance.util;

import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;

public class Util {
	
	public static DateTime computeValidWeekdayForward(DateTime d) {
		DateTime validDateTime = d;
		while (validDateTime.getDayOfWeek() == DateTimeConstants.SATURDAY
				|| validDateTime.getDayOfWeek() == DateTimeConstants.SUNDAY) {
			validDateTime = validDateTime.plusDays(1);
		}
		return validDateTime;
	}
	
	public static DateTime computeValidWeekdayBackward(DateTime d) {
		DateTime validDateTime = d;
		while (validDateTime.getDayOfWeek() == DateTimeConstants.SATURDAY
				|| validDateTime.getDayOfWeek() == DateTimeConstants.SUNDAY) {
			validDateTime = validDateTime.minusDays(1);
		}
		return validDateTime;
	}
	
}
