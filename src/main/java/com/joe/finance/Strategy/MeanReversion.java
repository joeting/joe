package com.joe.finance.Strategy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.joda.time.DateTime;

import com.google.common.base.Optional;
import com.google.common.collect.Range;
import com.joe.finance.data.Key;
import com.joe.finance.data.QuoteCache;
import com.joe.finance.data.QuoteDao;
import com.joe.finance.indicator.QuantitiveStats;
import com.joe.finance.optimizer.Dimension;
import com.joe.finance.order.Order;
import com.joe.finance.portfolio.Portfolio;
import com.joe.finance.portfolio.Portfolio.Asset;
import com.joe.finance.util.MarketDateTime;

public class MeanReversion implements IStrategy {
	
	public static final double UPPER_SIGMA = 1;
	public static final double LOWER_SIGMA = 1;
	
	public static Set<Dimension> dims;
	public static Dimension ndays;
	public static Dimension upperSigma;
	public static Dimension lowerSigma;
	public static Dimension minBuyRatio;
	public static Dimension maxBuyRatio;
	public static Dimension maxSellRatio;
	
	static {
		dims = new HashSet<>();
		ndays = new Dimension("ndays", Range.closed(30.0, 120.0), 0);
		upperSigma = new Dimension("upperSigma", Range.closed(0.5, 2.0), 1);
		lowerSigma = new Dimension("lowerSigma", Range.closed(0.5, 2.0), 1);
		minBuyRatio = new Dimension("minBuyRatio", Range.closed(0.1, 0.1), 2);
		maxBuyRatio = new Dimension("maxBuyRatio", Range.closed(0.1, 0.3), 2);
		maxSellRatio = new Dimension("maxSellRatio", Range.closed(0.2, 1.0), 2);
		dims.add(ndays);
		dims.add(upperSigma);
		dims.add(lowerSigma);
		dims.add(minBuyRatio);
		dims.add(maxBuyRatio);
		dims.add(maxSellRatio);
	}
	
	private MarketDateTime startTime;
	private MarketDateTime endTime;
	private Portfolio portfolio;
	private QuantitiveStats stats;
	private QuoteCache cache;
	private Report report;
	
	private Map<Dimension, Double> dimValueMap;
	
	public static class TestBuilder {
		public static List<IStrategy> buildTests(MarketDateTime startTime,
				MarketDateTime endTime) {
			List<IStrategy> list = new ArrayList<>();
			for (Integer n = 10; n <= 120; n++) {
				int portFolioStartValue = 1000000;
				Portfolio folio = new Portfolio("Mean Reversion " + n,  portFolioStartValue);
				folio.initWatch("NFLX");
				list.add(new MeanReversion(folio, 
						startTime,
						endTime));
			}
			return list;
		}
	}
	
	public MeanReversion(Portfolio portfolio, MarketDateTime startTime, MarketDateTime endTime) {
		this.portfolio = portfolio;
		this.cache = QuoteDao.quoteDao().getCache();
		this.startTime = startTime;
		this.endTime = endTime;
		
		report = new Report();
		report.strategyName = "MeanReversion";
		report.portfolioName = portfolio.getName();
		
		dimValueMap = new HashMap<>();
		dimValueMap.put(ndays, (double) 100);
		dimValueMap.put(upperSigma, UPPER_SIGMA);
		dimValueMap.put(lowerSigma, LOWER_SIGMA);
		dimValueMap.put(minBuyRatio, 0.1);
		dimValueMap.put(maxBuyRatio, 0.1);
		dimValueMap.put(maxSellRatio, 1.0);
	}

	@Override
	public void run(MarketDateTime iterationTime, boolean isBackTest) {
		if (stats == null) {
			int days = (int) dimValueMap.get(ndays).doubleValue();
			this.stats = new QuantitiveStats(days, portfolio, cache, startTime.minusDays(days).time(), 
					endTime.time());
		}
		for (String symbol : portfolio.getWatch()) {
			Key key = new Key(symbol, iterationTime.time());
			run(iterationTime.time(), symbol, key, isBackTest);
		}
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
	public Set<Dimension> getDimensions() {
		return dims;
	}

	private void run(DateTime time, String symbol, Key key, boolean isBackTest) {
		Optional<Double> priceOptional = cache.getPrice(key);
		Optional<Double> upperBollingerOptional = stats.getUpperBollinger(key);
		Optional<Double> lowerBollingerOptional = stats.getLowerBollinger(key);
		Optional<Double> movingAvgOptional = stats.getMovingAverage(key);
		if (!priceOptional.isPresent()
				|| !upperBollingerOptional.isPresent()
				|| !lowerBollingerOptional.isPresent()
				|| !movingAvgOptional.isPresent()) {
			return;
		}
		Asset asset = portfolio.getAsset(symbol);
		if (priceOptional.get() > dimValueMap.get(upperSigma) 
				* upperBollingerOptional.get()) {
			if (asset != null && asset.numShares > 0) {
				int sharesToSell = computeSharesToSell(asset, time, priceOptional.get());
				portfolio.sellShares(time, symbol, sharesToSell, priceOptional.get());
			}
		} else if (priceOptional.get() < dimValueMap.get(lowerSigma)
				* lowerBollingerOptional.get()) {
			if (asset == null || portfolio.getCash() > 0) {
				int sharesToBuy = computeSharesToBuy(asset, time, priceOptional.get());
				portfolio.buyShares(time, symbol, sharesToBuy, priceOptional.get());
			}
		}
	}
	
	private int computeSharesToBuy(Asset asset, DateTime time, double stockPrice) {
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
		if (maxSharesToBuy < minSharesToBuy) {
			return 0;
		} else {
			return maxSharesToBuy;
		}
	}
	
	private int computeSharesToSell(Asset asset, DateTime time, double stockPrice) {
		int maxSharesToSell = (int) (portfolio.computePortfolioValue(time, cache)
						* dimValueMap.get(maxSellRatio) / stockPrice);
		return Math.min(asset.numShares, maxSharesToSell);
	}
}

