package com.joe.finance.order;

import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.joe.finance.data.QuoteCache;
import com.joe.finance.data.QuoteDao;
import com.joe.finance.portfolio.Portfolio;

public class Order {
	
	public static String OPEN_ORDER = "Open";
	public static String CLOSED_ORDER = "Closed";
	public static String SAVED_ORDER = "Saved";
	
	public static String SELL = "Sell";
	public static String BUY = "Buy";
	public static String SELL_STOP_LOSS = "Sell -- Stop Loss";
	
	public DateTime time;
	public Portfolio portfolio;
	public String type;
	public String symbol;
	public int shares;
	public Double price;
	public Double transactionAmount;
	public String status;
	
	public static Order buyOrder(Portfolio portfolio, DateTime time, String symbol, int numShares, double sellPrice, 
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
	
	public static Order stopLossOrder(Portfolio portfolio, DateTime time, String symbol, int numShares, 
			double sellPrice, double amount) {
		Order order = new Order();
		order.portfolio = portfolio;
		order.time = time;
		order.symbol = symbol;
		order.shares = numShares;
		order.price = sellPrice;
		order.transactionAmount = amount;
		order.type = SELL_STOP_LOSS;
		order.status = CLOSED_ORDER;
		return order;
	}
	
	public static Order sellOrder(Portfolio portfolio, DateTime time, String symbol, int numShares, 
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
		QuoteCache cache = QuoteDao.quoteDao().getCache();
		for (Order order : orders) {
			order.logTrade(cache);
		}
	}
	
	public void logTrade(QuoteCache cache) {
		DateTimeFormatter formatter = DateTimeFormat.forPattern("YYYY MMMM dd");
		String out = String.format(
				"%-20s| %s %d shares of %s at %f \t | portfolio value : $%.2f", formatter.print(time), type, shares, symbol,
				price, portfolio.computePortfolioValue(time, cache));
		System.out.println(out);
	}

}
