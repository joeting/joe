package com.joe.finance.Strategy;

import java.util.ArrayList;
import java.util.List;

import org.joda.time.DateTime;

import com.google.common.base.Optional;
import com.joe.finance.data.Key;
import com.joe.finance.data.QuoteCache;
import com.joe.finance.data.QuoteDao;
import com.joe.finance.indicator.QuantitiveStats;
import com.joe.finance.order.Order;
import com.joe.finance.portfolio.Portfolio;
import com.joe.finance.portfolio.Portfolio.Asset;
import com.joe.finance.util.MarketDateTime;

public class MeanReversion implements IStrategy {
	
	public static final double UPPER_SIGMA = 1;
	public static final double LOWER_SIGMA = 1;
	private Portfolio portfolio;
	private QuantitiveStats stats;
	private QuoteCache cache;
	private Report report;
	private double upperSigma;
	private double lowerSigma;
	private double minBuyRatio;
	private double maxBuyRatio;
	private double maxSellRatio;
	
	public static class TestBuilder {
		public static List<IStrategy> buildTests(MarketDateTime startTime,
				MarketDateTime endTime) {
			List<IStrategy> list = new ArrayList<>();
			for (Integer n = 10; n <= 120; n++) {
				int portFolioStartValue = 1000000;
				Portfolio folio = new Portfolio("Mean Reversion " + n,  portFolioStartValue);
				folio.initWatch("NFLX");
				list.add(new MeanReversion(n, 
						folio, 
						startTime,
						endTime));
			}
			return list;
		}
	}
	
	public MeanReversion(int days, Portfolio portfolio, MarketDateTime startTime, MarketDateTime endTime) {
		this.portfolio = portfolio;
		this.cache = QuoteDao.quoteDao().getCache();
		this.stats = new QuantitiveStats(days, portfolio, cache, startTime.minusDays(days).time(), 
				endTime.time());
		report = new Report();
		report.strategyName = "MeanReversion";
		report.portfolioName = portfolio.getName();
		this.upperSigma = UPPER_SIGMA;
		this.lowerSigma = LOWER_SIGMA;
		this.minBuyRatio = 0.1;
		this.maxBuyRatio = 0.1;
		this.maxSellRatio = 1;
	}

	@Override
	public void run(MarketDateTime iterationTime, boolean isBackTest) {
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
	
	public MeanReversion setMaxBuyRatio(double ratio) {
		this.maxBuyRatio = ratio;
		return this;
	}
	
	public MeanReversion setMaxSellRatio(double ratio) {
		this.maxSellRatio = ratio;
		return this;
	}
	
	public MeanReversion setUpperSigma(double upperSigma) {
		this.upperSigma = upperSigma;
		return this;
	}

	public MeanReversion setLowerSigma(double lowerSigma) {
		this.lowerSigma = lowerSigma;
		return this;
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
		if (priceOptional.get() > upperSigma * upperBollingerOptional.get()) {
			if (asset != null && asset.numShares > 0) {
				int sharesToSell = computeSharesToSell(asset, time, priceOptional.get());
				portfolio.sellShares(time, symbol, sharesToSell, priceOptional.get());
			}
		} else if (priceOptional.get() < lowerSigma * lowerBollingerOptional.get()) {
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
		int minSharesToBuy = (int)(currentPortfolioValue * minBuyRatio / stockPrice);
		if (sharesToBuy < minSharesToBuy) {
			return 0;
		}
		int maxSharesToBuy = (int) (currentPortfolioValue * maxBuyRatio / stockPrice);
		return Math.max(0, maxSharesToBuy - currentHolding);
	}
	
	private int computeSharesToSell(Asset asset, DateTime time, double stockPrice) {
		int maxSharesToSell = (int) (portfolio.computePortfolioValue(time, cache)
						* maxSellRatio / stockPrice);
		return Math.min(asset.numShares, maxSharesToSell);
	}

}
