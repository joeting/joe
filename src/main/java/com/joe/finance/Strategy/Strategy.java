package com.joe.finance.Strategy;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.joda.time.DateTime;

import com.google.common.collect.Range;
import com.joe.finance.data.QuoteCache;
import com.joe.finance.data.QuoteDao;
import com.joe.finance.optimizer.Dimension;
import com.joe.finance.order.Order;
import com.joe.finance.portfolio.Portfolio;
import com.joe.finance.portfolio.Portfolio.Asset;
import com.joe.finance.util.MarketDateTime;

public abstract class Strategy implements IStrategy {
	
	protected QuoteCache cache;
	protected MarketDateTime startTime;
	protected MarketDateTime endTime;
	protected Portfolio portfolio;
	protected Report report;
	protected boolean debug = false;
	
	public static Set<Dimension> dims;
	public static Dimension minBuyRatio;
	public static Dimension maxBuyRatio;
	public static Dimension maxSellRatio;
	public static Dimension stopLoss;
	
	static {
		minBuyRatio = new Dimension("minBuyRatio", Range.closed(0.1, 0.1), 2);
		maxBuyRatio = new Dimension("maxBuyRatio", Range.closed(0.3, 0.3), 2);
		maxSellRatio = new Dimension("maxSellRatio", Range.closed(0.2, 1.0), 2);
		stopLoss = new Dimension("stopLoss", Range.closed(0.2, 0.2), 2);
		dims = new HashSet<>();
		dims.add(minBuyRatio);
		dims.add(maxBuyRatio);
		dims.add(maxSellRatio);
		dims.add(stopLoss);
	}
	protected Map<Dimension, Double> dimValueMap;
	
	public Strategy(Portfolio portfolio, MarketDateTime startTime, MarketDateTime endTime) {
		this.portfolio = portfolio;
		this.cache = QuoteDao.quoteDao().getCache();
		this.startTime = startTime;
		this.endTime = endTime;
		report = new Report();
		report.portfolio = portfolio;
		dimValueMap = new HashMap<>();
		// Defaults
		dimValueMap.put(minBuyRatio, 0.1);
		dimValueMap.put(maxBuyRatio, 0.1);
		dimValueMap.put(maxSellRatio, 1.0);
		dimValueMap.put(stopLoss, 0.1);
	}
	
	@Override
	public Report getReport(MarketDateTime iterationTime) {
		portfolio.computeReturn(report, iterationTime.time(), cache);
		return report;
	}
	
	@Override
	public List<Order> getTrades() {
		return portfolio.getTrades();
	}
	
	@Override
	public IStrategy setDimValue(Dimension dim, double value) {
		dimValueMap.put(dim, value);
		return this;
	}
	
	@Override
	public IStrategy setDebug() {
		debug = true;
		return this;
	}
	
	protected boolean triggerStopLoss(DateTime time, Asset asset, double price) {
		if (asset != null) {
			double delta =  price - asset.startPrice;
			if (delta < 0
					&& ((Math.abs(delta) /  asset.startPrice) > dimValueMap.get(stopLoss))){
				portfolio.sellShares(time, asset.symbol, asset.numShares, price, true);
				return true;
			}
		}
		return false;
	}
	
	protected int computeSharesToBuy(Asset asset, DateTime time, double stockPrice) {
		int currentHolding = 0;
		if (asset != null) {
			currentHolding = asset.numShares;
		}
		double currentPortfolioValue = portfolio.computePortfolioValue(time, cache);
		int sharesToBuy = (int)(portfolio.getCash() / stockPrice);
		int minSharesToBuy = (int)(currentPortfolioValue 
				* dimValueMap.get(minBuyRatio) / stockPrice);
		if (sharesToBuy < minSharesToBuy) {
			return 0;
		}
		int maxSharesToBuy = (int) (currentPortfolioValue 
				* dimValueMap.get(maxBuyRatio) / stockPrice);
		maxSharesToBuy =  maxSharesToBuy - currentHolding;
		maxSharesToBuy = Math.min(sharesToBuy, maxSharesToBuy);
		if (maxSharesToBuy < minSharesToBuy) {
			return 0;
		} else {
			return maxSharesToBuy;
		}
	}
	
	protected int computeSharesToSell(Asset asset, DateTime time, double stockPrice) {
		int maxSharesToSell = (int) (portfolio.computePortfolioValue(time, cache)
						* dimValueMap.get(maxSellRatio) / stockPrice);
		return Math.min(asset.numShares, maxSharesToSell);
	}
	
}
