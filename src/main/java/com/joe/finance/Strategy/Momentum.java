package com.joe.finance.Strategy;

import org.joda.time.DateTime;

import com.google.common.base.Optional;
import com.joe.finance.data.Key;
import com.joe.finance.indicator.MACD;
import com.joe.finance.indicator.Signal;
import com.joe.finance.portfolio.Portfolio;
import com.joe.finance.portfolio.Portfolio.Asset;
import com.joe.finance.util.MarketDateTime;

public class Momentum extends Strategy implements IStrategy {
	
	private MACD macd;
	
	public Momentum(Portfolio portfolio, MarketDateTime startTime, MarketDateTime endTime) {
		super(portfolio, startTime, endTime);
		report.strategyName = "Momentum";
		macd = new MACD(portfolio, startTime, endTime);
	}
	
	@Override
	public void run(MarketDateTime iterationTime, boolean isBackTest) {
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
