package com.joe.finance.portfolio;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;

import com.google.common.base.Optional;
import com.joe.finance.Strategy.Report;
import com.joe.finance.config.xml.PortfolioConfig;
import com.joe.finance.data.QuoteCache;
import com.joe.finance.order.Order;

public class Portfolio {

	public static final double MAX_SECURITY_HOLDING = 0.2;
	static final Logger logger = Logger.getLogger(Portfolio.class);
	
	private String name;
	private double initialValue;
	private double cash;
	// Map keyed based on ticker symbol
	private Map<String, Asset> position;
	// Set of stocks to track for algorithm;
	private Set<String> watch;
	private List<Order> orders;

	public class Asset {
		public Asset(String symbol, int numShares) {
			this.symbol = symbol;
			this.numShares = numShares;
		}

		public String symbol;
		public int numShares;
	}

	public Portfolio(PortfolioConfig config) {
		this(config.name, config.portfolioStartValue);
		this.watch = new HashSet<String>(config.symbols);
	}
	
	public Portfolio(String name, double cash) {
		this.name = name;
		this.initialValue = cash;
		this.cash = cash;
		this.position = new HashMap<>();
		this.watch = new HashSet<>();
		this.orders = new ArrayList<>();
	}

	public void sellAllShares(DateTime nowDateTime, QuoteCache cache) {
		if (position.isEmpty()) {
			return;
		}
		Set<String> holdings = position.keySet();
		for (String symbol : holdings) {
			Optional<Double> closePriceOptional = cache.getPrice(nowDateTime, symbol);
			if (closePriceOptional.isPresent()) {
				Asset asset = position.remove(symbol);
				cash += asset.numShares * closePriceOptional.get();
			}
		}
	}

	public void sellShares(DateTime time, String symbol, int numShares, double sellPrice) {
		Asset asset = position.get(symbol);
		assert (asset != null);
		asset.numShares -= numShares;
		assert (asset.numShares >= 0);
		if (numShares == 0) {
			position.remove(symbol);
		}
		double amount = numShares * sellPrice;
		cash += amount;
		Order order = Order.sellOrder(name, time, symbol, numShares, sellPrice, amount);
		orders.add(order);
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
		Optional<Double> buyPrice = cache.getPrice(nowDateTime, symbol);
		if (buyPrice.isPresent()) {
			buyShares(nowDateTime, symbol, numShares, buyPrice.get());
		}
	}

	public void buyShares(DateTime time, String symbol, int numShares, double buyPrice) {
		if (numShares == 0) {
			return;
		}
		Asset asset = position.get(symbol);
		if (asset == null) {
			Asset newPosition = new Asset(symbol, numShares);
			position.put(symbol, newPosition);
		} else {
			asset.numShares += numShares;
		}
		double amount = numShares * buyPrice;
		cash -= amount;
		Order order = Order.buyOrder(name, time, symbol, numShares, buyPrice, amount);
		orders.add(order);
	}

	// compute current cummulative return
	public Report computeReturn(Report report, DateTime nowDateTime, QuoteCache cache) {
		if (position.isEmpty()) {
			report.cummulativeReturn = 0.0;
			return report;
		}
		Double portfolioValue = computePortfolioValue(nowDateTime, cache);
		report.startValue = initialValue;
		report.endValue = portfolioValue;
		report.cummulativeReturn =  (portfolioValue - initialValue) / initialValue;
		return report;
	}

	public Double computePortfolioValue(DateTime nowDateTime, QuoteCache cache) {
		Double portfolioValue = this.cash;
		Set<String> holdings = position.keySet();
		for (String symbol : holdings) {
			Optional<Double> closePriceOptional = cache.getPrice(nowDateTime, symbol);
			if (closePriceOptional.isPresent()) {
				Asset asset = position.get(symbol);
				portfolioValue = portfolioValue + (asset.numShares * closePriceOptional.get());
			} else {
				throw new RuntimeException("Quote unavailable for " + nowDateTime);
			}
		}
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
