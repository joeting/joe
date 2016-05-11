package com.joe.finance.indicator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.google.common.base.Optional;
import com.joe.finance.data.Key;
import com.joe.finance.data.QuoteCache;
import com.joe.finance.portfolio.Portfolio;
import com.joe.finance.util.MarketDateTime;

public class BollingerBand {
	
	public static class Factory {
		
		private static Map<Integer, BollingerBand> instanceCache = new HashMap<>();
		
		public static BollingerBand getInstance(int days,
				Portfolio portfolio,
				QuoteCache cache,
				MarketDateTime startTime,
				MarketDateTime endTime) {
			if (instanceCache.containsKey(days)) {
				System.out.println("Cachehit  :"  + days);
				return instanceCache.get(days);
			} else {
				BollingerBand band = new BollingerBand(days,
						portfolio,
						cache,
						startTime,
						endTime);
				instanceCache.put(days, band);
				return band;
			}
		}
	}
	
	// moving average days.  ie. 5 = 5 day moving average.
	private int days;
	private List<String> symbols;
	// key is symbol
	private Map<String, LinkedList<Double>> lastXDaysPrice;
	// key is Quote.createId
	private Map<Key, Double> movingAverage;
	private Map<Key, Double> standardDeviation;
	
	private QuoteCache cache;

	public BollingerBand(int days, 
			Portfolio portfolio, 
			QuoteCache cache,
			MarketDateTime startTime, 
			MarketDateTime endTime) {
		this.days = days;
		this.symbols = new ArrayList<>(portfolio.getWatch());
		this.cache = cache;
		lastXDaysPrice = new HashMap<>();
		movingAverage = new HashMap<>();
		standardDeviation = new HashMap<>();
		init(startTime, endTime);
	}
	
	private void init(MarketDateTime startTime, MarketDateTime endTime) {
		MarketDateTime iterationTime = new MarketDateTime(startTime.time());
		while (iterationTime.isBefore(endTime)) {
			for (String symbol : symbols) {
				Double price = cache.getPrice(iterationTime.time(), symbol);
				if (price == null) {
					// Must be market holiday.  Need to verify.
					continue;
				}
			  LinkedList<Double> queue = 
						lastXDaysPrice.get(symbol);
				if (queue == null) {
					queue = new LinkedList<>();
					lastXDaysPrice.put(symbol, queue);
				}
				if (queue.size() == days) {
					queue.removeLast();
				} 
				queue.push(price);
				Key key = new Key(symbol, iterationTime.time());
				Double sum = new Double(0);
				for (Double p : queue) {
					sum += p;
				}
				Double average = sum / queue.size();
				Double sumDeltaSquared = new Double(0);
				for (Double p : queue) {
					Double delta = p - average;
					sumDeltaSquared += (delta * delta);
				}
				Double sd = Math.sqrt(sumDeltaSquared / queue.size());
				Double a = sum / queue.size();
				movingAverage.put(key, a);
				standardDeviation.put(key, sd);
				
				// System.out.println("a" + " " + key.getKey() + " : " + sum / queue.size());
				// System.out.println("u" + " " + key.getKey() + " : " + (a + 2 * sd));
				// System.out.println("l" + " " + key.getKey() + " : " + (a - 2 * sd));
				
			}
			iterationTime = iterationTime.plusDays(1);
		}
	}
	
	public Optional<Double> getMovingAverage(Key key) {
		Double avg = movingAverage.get(key);
		if (avg == null) {
			return Optional.absent();
		}
		return Optional.of(movingAverage.get(key));
	}
	
	public Optional<Double> getLowerBollinger(Key key, double lowerSigma) {
		Double avg = movingAverage.get(key);
		Double sd = standardDeviation.get(key);
		if (avg == null || sd == null) {
			return Optional.absent();
		}
		return Optional.of(avg - lowerSigma * sd);
	}
	
	public Optional<Double> getUpperBollinger(Key key, double upperSigma) {
		Double avg = movingAverage.get(key);
		Double sd = standardDeviation.get(key);
		if (avg == null || sd == null) {
			return Optional.absent();
		}
		return Optional.of(avg + upperSigma * sd);
	}
	
}
