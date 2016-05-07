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
	private Map<Key, Double> keySigMap;
	
	public MACD(Portfolio portfolio, MarketDateTime startTime, MarketDateTime endTime) {
		this.portfolio = portfolio;
		this.cache = QuoteDao.quoteDao().getCache();
		keySigMap = new HashMap<>();
		init(startTime, endTime);
	}
	
	private void init(MarketDateTime startTime, MarketDateTime endTime) {
		MarketDateTime iterationTime = new MarketDateTime(startTime.time());
		int iterationCount = 0;
		Map<String, Double> ema12Days = new HashMap<>();
		Map<String, Double> ema26Days = new HashMap<>();
		Map<String, Double> sig9Days = new HashMap<>();
		double CP_MULTIPLIER12 = (2.0 / (12.0 + 1.0));
		double CP_MULTIPLIER26 = (2.0 / (26.0 + 1.0));
		double MACD_MULTIPLIER9 = (2.0 / (9.0 + 1.0));
		
		
		while (iterationTime.isBefore(endTime)) {
			for (String symbol : portfolio.getWatch()) {
				Double price = cache.getPrice(iterationTime.time(), symbol);
				if (price == null) {
					// Must be market holiday.  Need to verify.
					continue;
				}
 				double ema12 = computeEma(symbol, ema12Days, CP_MULTIPLIER12, price);
				double ema26 = computeEma(symbol, ema26Days, CP_MULTIPLIER26, price);
				double macd = ema12 - ema26;
				double sig9 = computeEma(symbol, sig9Days, MACD_MULTIPLIER9, macd);
				
				if (iterationCount >= 30) {
					System.out.println(iterationTime  + ":\t" +ema12 + ":\t" + ema26 + ":\t" 
							+ macd + ":\t" + sig9);
				}
			}
			iterationCount++;
			iterationTime = iterationTime.plusDays(1);
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
	
	public static void main(String[] args) {
		Portfolio portfolio = new Portfolio("Test",  1000000);
		portfolio.initWatch("NFLX");
		MACD d = new MACD(portfolio, MarketDateTime.now().minusDays(200), MarketDateTime.now());
		System.out.println(d);
	}

}
