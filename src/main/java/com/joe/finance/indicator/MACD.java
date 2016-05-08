package com.joe.finance.indicator;

import java.util.HashMap;
import java.util.Map;

import com.joe.finance.data.Key;
import com.joe.finance.data.QuoteCache;
import com.joe.finance.data.QuoteDao;
import com.joe.finance.portfolio.Portfolio;
import com.joe.finance.util.MarketDateTime;

public class MACD {
	
	private Portfolio portfolio;
	private QuoteCache cache;
	private Map<Key, Signal> keySigMap;
	private boolean debug = false;
	
	public MACD(Portfolio portfolio, MarketDateTime startTime, MarketDateTime endTime) {
		this.portfolio = portfolio;
		this.cache = QuoteDao.quoteDao().getCache();
		keySigMap = new HashMap<>();
		// MACD needs some time to get accurate.  Start 100 days prior.  
		init(startTime.minusDays(100), endTime);
	}
	
	private void init(MarketDateTime startTime, MarketDateTime endTime) {
		MarketDateTime iterationTime = new MarketDateTime(startTime.time());
		Map<String, Double> emaFastDays = new HashMap<>();
		Map<String, Double> emaSlowDays = new HashMap<>();
		Map<String, Double> sigDays = new HashMap<>();
		Map<String, Double> divergenceMap = new HashMap<>();
		double CP_MULTIPLIER12 = (2.0 / (12.0 + 1.0));
		double CP_MULTIPLIER26 = (2.0 / (26.0 + 1.0));
		double MACD_MULTIPLIER = (2.0 / (9.0 + 1.0));
		while (iterationTime.isBefore(endTime)) {
			for (String symbol : portfolio.getWatch()) {
				Double price = cache.getPrice(iterationTime.time(), symbol);
				if (price == null) {
					// Must be market holiday.  Need to verify.
					continue;
				}
 				double emaFast = computeEma(symbol, emaFastDays, CP_MULTIPLIER12, price);
				double emaSlow = computeEma(symbol, emaSlowDays, CP_MULTIPLIER26, price);
				double macd = emaFast - emaSlow;
				double sig = computeEma(symbol, sigDays, MACD_MULTIPLIER, macd);
				double divergence = macd - sig;
				if (divergenceMap.containsKey(symbol)) {
					double prevDivergence = divergenceMap.get(symbol);
					if (prevDivergence <= 0 && divergence > 0) {
						Key key = new Key(symbol, iterationTime.time());
						keySigMap.put(key, Signal.BUY);
					} else if (prevDivergence >= 0 && divergence < 0) {
						Key key = new Key(symbol, iterationTime.time());
						keySigMap.put(key, Signal.SELL);
					}
				}
				divergenceMap.put(symbol, divergence);
				if (debug) {
					printDebug(iterationTime, emaFast, emaSlow, macd, sig);
				}
			}
			iterationTime = iterationTime.plusDays(1);
		}
		if (debug) {
			for (Key key : keySigMap.keySet()) {
				System.out.println(
						String.format("Key : %s : Signal : %s", key, keySigMap.get(key)));
			}
		}
	}
	
	public Signal getSignal(Key key) {
		if (!keySigMap.containsKey(key)) {
			return Signal.HOLD;
		} else {
			return keySigMap.get(key);
		}
	}
	
	private double computeEma(String symbol, Map<String, Double> map, double k, double price) {
		if (!map.containsKey(symbol)) {
			map.put(symbol, price);
			return price;
		} 
		double prevPrice = map.get(symbol);
		double ema = prevPrice + k * (price - prevPrice);
		map.put(symbol, ema);
		return ema;
	}
	
	private void printDebug(MarketDateTime iterationTime, double emaFast, 
			double emaSlow, double macd, double sig ) {
		System.out.println(iterationTime  + ":\t" +emaFast + ":\t" + emaSlow + ":\t" 
				+ macd + ":\t" + sig);
	}
	
	public static void main(String[] args) {
		Portfolio portfolio = new Portfolio("Test",  1000000);
		portfolio.initWatch("NFLX");
		MACD d = new MACD(portfolio, MarketDateTime.now().minusDays(200), MarketDateTime.now());
		System.out.println(d);
	}

}
