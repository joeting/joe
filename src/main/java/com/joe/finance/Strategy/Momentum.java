package com.joe.finance.Strategy;

import org.joda.time.DateTime;

import com.google.common.base.Optional;
import com.google.common.collect.Range;
import com.joe.finance.data.Key;
import com.joe.finance.indicator.MACD;
import com.joe.finance.indicator.Signal;
import com.joe.finance.optimizer.Dimension;
import com.joe.finance.portfolio.Portfolio;
import com.joe.finance.portfolio.Portfolio.Asset;
import com.joe.finance.util.MarketDateTime;

public class Momentum extends Strategy implements IStrategy {
	
	private MACD macd;
	
	public static Dimension fast;
	public static Dimension slow;
	public static Dimension sig;
	
	public Momentum(Portfolio portfolio, MarketDateTime startTime, MarketDateTime endTime) {
		super(portfolio, startTime, endTime);
		report.strategyName = "Momentum";
		dimValueMap.put(fast, 12.0);
		dimValueMap.put(slow, 26.0);
		dimValueMap.put(sig, 26.0);
	}
	
	public static void init() {
		fast = new Dimension("fast", Range.closed(14.0, 20.0), 0);
		slow = new Dimension("slow", Range.closed(22.0, 40.0), 0);
		sig = new Dimension("sig", Range.closed(6.0, 12.0), 0);
		dims.add(fast);
		dims.add(slow);
		dims.add(sig);
	}
	
	@Override
	public void run(MarketDateTime iterationTime, boolean isBackTest) {
		if (macd == null) {
			macd = MACD.Factory.getInstance((int) dimValueMap.get(fast).doubleValue(),
					(int) dimValueMap.get(slow).doubleValue(),
					(int) dimValueMap.get(sig).doubleValue(),
					portfolio, startTime, endTime);
		}
		for (String symbol : portfolio.getWatch()) {
			Key key = new Key(symbol, iterationTime.time());
			run(iterationTime.time(), symbol, key, isBackTest);
		}
	}
	
	private void run(DateTime time, String symbol, Key key, boolean isBackTest) {
		Optional<Double> priceOptional = cache.getPrice(key);
		if (!priceOptional.isPresent()) {
			return;
		}
		double price = priceOptional.get();
		Asset asset = portfolio.getAsset(symbol);
		if (triggerStopLoss(time, asset, price)) {
				return;
		}
		
		if (Signal.BUY == macd.getSignal(key)) {
			if (portfolio.canBuyBackShares(symbol, time)) {
				if (asset == null || portfolio.getCash() > 0) {
					int sharesToBuy = computeSharesToBuy(asset, time, price);
					portfolio.buyShares(time, symbol, sharesToBuy, price);
				}
			}
		} else if (Signal.SELL == macd.getSignal(key)) {
			if (asset != null && asset.numShares > 0) {
				int sharesToSell = computeSharesToSell(asset, time, price);
				portfolio.sellShares(time, symbol, sharesToSell, price, false);
				return;
			}
		}
	}
	
}
