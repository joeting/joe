package com.joe.finance.data;

import java.util.Calendar;
import java.util.TimeZone;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;

@Entity("quote")
public class Quote {
	@Id
	private String id;
	private String symbol;
	private Long volume;
	private String adjClosePrice;
	private String openPrice;
	private String closePrice;
	private String highPrice;
	private String lowPrice;
	private String lastPrice;
	
	public Quote() {}
	
	public Quote(String symbol, Calendar calendar, String closePrice) {
		this.symbol = symbol;
		DateTimeZone jodaTz = DateTimeZone.forTimeZone(TimeZone.getTimeZone("UTC"));
		DateTime dateTime = new DateTime(calendar.getTimeInMillis(), jodaTz);
		this.closePrice = closePrice;
		this.id = new Key(symbol, dateTime).getKey();
	}

	public Key getId() {
		return new Key(id);
	}
	
	public String getSymbol() {
		return symbol;
	}

	public Long getVolume() {
		return volume;
	}

	public void setVolume(Long volume) {
		this.volume = volume;
	}

	public String getAdjClosePrice() {
		return adjClosePrice;
	}

	public void setAdjClosePrice(String adjClosePrice) {
		this.adjClosePrice = adjClosePrice;
	}

	public String getOpenPrice() {
		return openPrice;
	}

	public void setOpenPrice(String openPrice) {
		this.openPrice = openPrice;
	}

	public String getClosePrice() {
		return closePrice;
	}

	public void setClosePrice(String closePrice) {
		this.closePrice = closePrice;
	}

	public String getHighPrice() {
		return highPrice;
	}

	public void setHighPrice(String highPrice) {
		this.highPrice = highPrice;
	}

	public String getLowPrice() {
		return lowPrice;
	}

	public void setLowPrice(String lowPrice) {
		this.lowPrice = lowPrice;
	}
	
	public String getLastPrice() {
		return lastPrice;
	}
	
	public void setLastPrice(String price) {
		this.lastPrice = price;
	}

}
