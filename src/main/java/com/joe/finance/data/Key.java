package com.joe.finance.data;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

public class Key {
	
	private String key;
	
	public Key(String key) {
		this.key = key;
	}
	
	public Key(String symbol, DateTime dateTime) {
		assert (symbol != null && dateTime != null);
		DateTimeFormatter f = DateTimeFormat.forPattern("yyyy-MM-dd");
		key = symbol + "-" + f.print(dateTime);
	}
	
	public String getKey() {
		return key;
	}
	
	@Override 
	public int hashCode() {
		return key.hashCode();
	}
	
	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		Key key = (Key) o;
		boolean result = key.getKey().equals(key.getKey());
		return result;
	}
	
}
