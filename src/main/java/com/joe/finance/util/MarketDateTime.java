package com.joe.finance.util;

import org.joda.time.DateTime;

/**
 * Represents a valid market trading day.  (ie. weekday).
 */
public class MarketDateTime {
	
	private DateTime time;
	
	public static MarketDateTime now() {
		return new MarketDateTime(DateTime.now());
	}
	
	public static MarketDateTime nowMinusNDays(int n) {
		return new MarketDateTime(DateTime.now().minusDays(n));
	}
	
	public static MarketDateTime nowMinusNYears(int n) {
		return new MarketDateTime(DateTime.now().minusYears(n));
	}
	
	public MarketDateTime(DateTime input) {
		time = Util.computeValidWeekdayBackward(input);
	}
	
	public MarketDateTime(DateTime input, boolean backward) {
		if (backward) {
			time = Util.computeValidWeekdayBackward(input);
		} else {
			time = Util.computeValidWeekdayForward(input);
		}
	}
	
	public DateTime time() {
		return time;
	}
	
	public MarketDateTime plusDays(int n) {
		return new MarketDateTime(time.plusDays(n), false);
	}
	
	public MarketDateTime minusDays(int n) {
		return new MarketDateTime(time.minusDays(n));
	}
	
	@Override
	public String toString() {
		return time.toString();
	}

}
