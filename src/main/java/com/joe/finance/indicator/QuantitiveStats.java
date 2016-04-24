package com.joe.finance.indicator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import com.google.common.base.Optional;
import com.joe.finance.data.Key;
import com.joe.finance.data.QuoteCache;
import com.joe.finance.data.QuoteDao;
import com.joe.finance.portfolio.Portfolio;

public class QuantitiveStats {
	
	// moving average days.  ie. 5 = 5 day moving average.
	private int days;
	private List<String> symbols;
	// key is symbol
	private Map<String, LinkedList<Double>> lastXDaysPrice;
	// key is Quote.createId
	private Map<Key, Double> movingAverage;
	private Map<Key, Double> standardDeviation;
	
	private QuoteCache cache;

	public QuantitiveStats(int days, Portfolio portfolio, QuoteCache cache,
			DateTime startTime, DateTime endTime) {
		this(days, new ArrayList<>(portfolio.getWatch()), cache, startTime, endTime);
	}
	
	public QuantitiveStats(int days, List<String> symbols, QuoteCache cache,
			DateTime startTime, DateTime endTime) {
		this.days = days;
		this.symbols = symbols;
		this.cache = cache;
		lastXDaysPrice = new HashMap<>();
		movingAverage = new HashMap<>();
		standardDeviation = new HashMap<>();
		init(startTime, endTime);
	}
	
	public Map<Key, Double> getMovingAverage() {
		return movingAverage;
	}

	public Map<Key, Double> getStandardDeviation() {
		return standardDeviation;
	}

	private void init(DateTime startTime, DateTime endTime) {
		DateTime iterationTime = new DateTime(startTime);
		while (iterationTime.isBefore(endTime)) {
			for (String symbol : symbols) {
				Optional<Double> priceOptional = cache.getPrice(iterationTime, symbol);
				if (!priceOptional.isPresent()) {
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
				queue.push(priceOptional.get());
				Key key = new Key(symbol, iterationTime);
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
	
	public Optional<Double> getLowerBollinger(Key key) {
		Double avg = movingAverage.get(key);
		Double sd = standardDeviation.get(key);
		if (avg == null || sd == null) {
			return Optional.absent();
		}
		return Optional.of(avg - 2 * sd);
	}
	
	public Optional<Double> getUpperBollinger(Key key) {
		Double avg = movingAverage.get(key);
		Double sd = standardDeviation.get(key);
		if (avg == null || sd == null) {
			return Optional.absent();
		}
		return Optional.of(avg + 2 * sd);
	}
	
	public static void main(String arg[]) {
		DateTime startTime = new DateTime(2015, 1, 1, 4, 0, DateTimeZone.UTC);
		DateTime endTime = new DateTime(2016, 4, 12, 4, 0, DateTimeZone.UTC);
		QuoteDao dao = QuoteDao.quoteDao();
		QuantitiveStats mv = new QuantitiveStats(98, Arrays.asList("SPY"), dao.getCache(), startTime, endTime);
	}

}
