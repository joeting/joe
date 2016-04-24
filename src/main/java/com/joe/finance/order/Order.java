package com.joe.finance.order;

import java.util.List;

import org.joda.time.DateTime;

public class Order {
	
	public static String OPEN_ORDER = "Open";
	public static String CLOSED_ORDER = "Closed";
	
	public static String SELL = "Sell";
	public static String BUY = "Buy";
	
	public DateTime time;
	public String portfolio;
	public String type;
	public String symbol;
	public int shares;
	public Double price;
	public Double transactionAmount;
	public String status;
	
	public static Order buyOrder(String portfolio, DateTime time, String symbol, int numShares, double sellPrice, 
			double amount) {
		Order order = new Order();
		order.portfolio = portfolio;
		order.time = time;
		order.symbol = symbol;
		order.shares = numShares;
		order.price = sellPrice;
		order.transactionAmount = amount;
		order.type = BUY;
		order.status = CLOSED_ORDER;
		return order;
	}
	
	public static Order sellOrder(String portfolio, DateTime time, String symbol, int numShares, 
			double sellPrice, double amount) {
		Order order = new Order();
		order.portfolio = portfolio;
		order.time = time;
		order.symbol = symbol;
		order.shares = numShares;
		order.price = sellPrice;
		order.transactionAmount = amount;
		order.type = SELL;
		order.status = CLOSED_ORDER;
		return order;
	}
	
	public static void logTrades(List<Order> orders) {
		if (orders == null) {
			return;
		}
		for (Order order : orders) {
			order.logTrade();
		}
	}
	
	public void logTrade() {
		String out = String.format(
				"%s : %s : %s %d shares of %s at %f", portfolio, time, type, shares, symbol,
				price);
		System.out.println(out);
	}

}
