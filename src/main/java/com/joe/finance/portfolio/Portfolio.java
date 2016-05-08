package com.joe.finance.portfolio;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.joe.finance.Strategy.Report;
import com.joe.finance.config.xml.PortfolioConfig;
import com.joe.finance.data.QuoteCache;
import com.joe.finance.order.Order;

public class Portfolio {

	public static final double MIN_STOCK_HOLDING_PERIOD = 0;
	static final Logger logger = Logger.getLogger(Portfolio.class);
	
	private String name;
	private double initialValue;
	private double cash;
	// Map keyed based on ticker symbol
	private Map<String, Asset> position;
	// Set of stocks to track for algorithm;
	private Set<String> watch;
	private List<Order> orders;
	private DateTimeFormatter valueKeyFormatter;
	private Map<String, Double> valueMap;
	// Key is stock symbol
	private Map<String, DateTime> lastSaleLookup;

	public class Asset {
		public Asset(String symbol, int numShares, double buyPrice) {
			this.symbol = symbol;
			this.numShares = numShares;
			this.buyPrice = buyPrice;
		}

		public String symbol;
		public int numShares;
		public double buyPrice;
	}

	public Portfolio(PortfolioConfig config) {
		this(config.name, config.portfolioStartValue);
		this.watch = new HashSet<String>(config.symbols);
		this.valueMap = new TreeMap<>();
		valueKeyFormatter = DateTimeFormat.forPattern("yyyy-MM-dd");
		lastSaleLookup = new HashMap<>();
	}
	
	public Portfolio(String name, double cash) {
		this.name = name;
		this.initialValue = cash;
		this.cash = cash;
		this.position = new HashMap<>();
		this.watch = new HashSet<>();
		this.orders = new ArrayList<>();
	}

	public boolean canBuyBackShares(String symbol, DateTime now) {
		DateTime lastSale = lastSaleLookup.get(symbol);
		if (lastSale == null) {
			return true;
		}
		int delta = 
				Days.daysBetween(
						lastSale.withTimeAtStartOfDay(), now.withTimeAtStartOfDay()).getDays();
		if (delta <= MIN_STOCK_HOLDING_PERIOD ) {
			return false;
		}
		return true;
	}
	
	public void sellShares(DateTime time, String symbol, int numShares, double sellPrice,
			boolean stopLoss) {
		Asset asset = position.get(symbol);
		if (numShares > asset.numShares) {
			throw new RuntimeException("Selling more shares than available.  Check logic.");
		}
		asset.numShares -= numShares;
		if (asset.numShares == 0) {
			position.remove(symbol);
		}
		double amount = numShares * sellPrice;
		cash += amount;
		Order order = null;
		if (stopLoss) {
			order = Order.stopLossOrder(this, time, symbol, numShares, sellPrice, amount);
		} else {
			order = Order.sellOrder(this, time, symbol, numShares, sellPrice, amount);
		}
		orders.add(order);
		lastSaleLookup.put(symbol, time);
	}
	
	public void initWatch(List<String> symbols) {
		for (String symbol : symbols) {
			watch.add(symbol);
		}
	}
	
	public void initWatch(String symbol) {
		watch.add(symbol);
	}
	
	public void buyShares(DateTime nowDateTime, QuoteCache cache,
			String symbol, int numShares) {
		if (numShares == 0) {
			return;
		}
		double buyPrice = cache.getPrice(nowDateTime, symbol);
		buyShares(nowDateTime, symbol, numShares, buyPrice);
	}

	public void buyShares(DateTime time, String symbol, int numShares, double buyPrice) {
		if (numShares == 0) {
			return;
		}
		Asset asset = position.get(symbol);
		if (asset == null) {
			Asset newPosition = new Asset(symbol, numShares, buyPrice);
			position.put(symbol, newPosition);
		} else {
			asset.numShares += numShares;
		}
		double amount = numShares * buyPrice;
		if (amount > cash) {
			throw new RuntimeException("Insufficent cash to buy shares.  Check logic.");
		}
		cash -= amount;
		Order order = Order.buyOrder(this, time, symbol, numShares, buyPrice, amount);
		orders.add(order);
	}

	// compute current cummulative return
	public Report computeReturn(Report report, DateTime nowDateTime, QuoteCache cache) {
		Double portfolioValue = computePortfolioValue(nowDateTime, cache);
		report.startValue = initialValue;
		report.endValue = portfolioValue;
		report.cummulativeReturn =  (portfolioValue - initialValue) / initialValue;
		return report;
	}

	public Double computePortfolioValue(DateTime nowDateTime, QuoteCache cache) {
		String key = valueKeyFormatter.print(nowDateTime);
		Double value = valueMap.get(key);
		if (value != null) {
			return value;
		}
		Double portfolioValue = this.cash;
		Set<String> holdings = position.keySet();
		for (String symbol : holdings) {
		double closePrice = cache.getPrice(nowDateTime, symbol);
			Asset asset = position.get(symbol);
			portfolioValue = portfolioValue + (asset.numShares * closePrice);
		} 
		valueMap.put(key, portfolioValue);
		return portfolioValue;
	}
	
	public double getCash() {
		return cash;
	}

	public HashMap<String, Asset> getPosition() {
		return (HashMap<String, Asset>) position;
	}
	
	public Asset getAsset(String symbol) {
		return position.get(symbol);
	}
	
	public Set<String> getWatch() {
		return watch;
	}
	
	public List<Order> getTrades() {
		return orders;
	}
	
	public double getInitialValue() {
		return initialValue;
	}
	
	public void logTrades(){
		Order.logTrades(orders);
	}
	
	public String getName() {
		return name;
	}
	
}
