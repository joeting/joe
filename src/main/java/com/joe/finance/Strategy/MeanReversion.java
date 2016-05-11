package com.joe.finance.Strategy;

import org.joda.time.DateTime;

import com.google.common.base.Optional;
import com.google.common.collect.Range;
import com.joe.finance.data.Key;
import com.joe.finance.indicator.BollingerBand;
import com.joe.finance.optimizer.Dimension;
import com.joe.finance.portfolio.Portfolio;
import com.joe.finance.portfolio.Portfolio.Asset;
import com.joe.finance.util.MarketDateTime;

public class MeanReversion extends Strategy implements IStrategy {
	
	public static final double UPPER_SIGMA = 1;
	public static final double LOWER_SIGMA = 1;
	
	public static Dimension ndays;
	public static Dimension upperSigma;
	public static Dimension lowerSigma;
	
	private BollingerBand stats;
	
	public MeanReversion(Portfolio portfolio, MarketDateTime startTime, MarketDateTime endTime) {
		super(portfolio, startTime, endTime);
		report.strategyName = "MeanReversion";
		dimValueMap.put(ndays, (double) 100);
		dimValueMap.put(upperSigma, UPPER_SIGMA);
		dimValueMap.put(lowerSigma, LOWER_SIGMA);
	}
	
	public static void init() {
		ndays = new Dimension("ndays", Range.closed(30.0, 120.0), 0);
		upperSigma = new Dimension("upperSigma", Range.closed(1.5, 2.5), 1);
		lowerSigma = new Dimension("lowerSigma", Range.closed(1.5, 2.5), 1);
		dims.add(ndays);
		dims.add(upperSigma);
		dims.add(lowerSigma);
	}

	@Override
	public void run(MarketDateTime iterationTime, boolean isBackTest) {
		if (stats == null) {
			int days = (int) dimValueMap.get(ndays).doubleValue();
			this.stats = BollingerBand.Factory.getInstance(days, 
					portfolio, 
					cache, 
					startTime.minusDays(days), 
					endTime);
		}
		for (String symbol : portfolio.getWatch()) {
			Key key = new Key(symbol, iterationTime.time());
			run(iterationTime.time(), symbol, key, isBackTest);
		}
	}
	
	private void run(DateTime time, String symbol, Key key, boolean isBackTest) {
		Optional<Double> priceOptional = cache.getPrice(key);
		Optional<Double> upperBollingerOptional = stats.getUpperBollinger(key, 
				dimValueMap.get(upperSigma));
		Optional<Double> lowerBollingerOptional = stats.getLowerBollinger(key,
				dimValueMap.get(lowerSigma));
		Optional<Double> movingAvgOptional = stats.getMovingAverage(key);
		if (!priceOptional.isPresent()
				|| !upperBollingerOptional.isPresent()
				|| !lowerBollingerOptional.isPresent()
				|| !movingAvgOptional.isPresent()) {
			return;
		}
		double price = priceOptional.get();
		Asset asset = portfolio.getAsset(symbol);
		if (triggerStopLoss(time, asset, price)) {
				return;
		}
		if (priceOptional.get() > upperBollingerOptional.get()) {
			if (asset != null && asset.numShares > 0) {
				int sharesToSell = computeSharesToSell(asset, time, price);
				portfolio.sellShares(time, symbol, sharesToSell, price, false);
				return;
			}
		}
		if (portfolio.canBuyBackShares(symbol, time)) {
				if (priceOptional.get() < lowerBollingerOptional.get()) {
					if (debug) {
						System.out.println(
								String.format("%s : %s below band %.2f : %.2f", time, symbol, price, 
										lowerBollingerOptional.get() ));
					}
				if (asset == null || portfolio.getCash() > 0) {
					int sharesToBuy = computeSharesToBuy(asset, time, price);
					portfolio.buyShares(time, symbol, sharesToBuy, price);
				}
			}
		}
	}
	
}

