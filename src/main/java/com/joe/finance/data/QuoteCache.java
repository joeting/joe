package com.joe.finance.data;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

import org.joda.time.DateTime;

import com.google.common.base.Optional;
import com.joe.finance.importer.YahooReader;

public class QuoteCache {
	
	private Map<Key, Quote> cache;

	public QuoteCache(Map<Key, Quote> cache) {
		super();
		this.cache = cache;
	}
	
	public void loadCurrentQuote(Set<String> symbols) throws IOException {
		DateTime now = DateTime.now();
		Map<String, Quote> current = YahooReader.getQuote(symbols);
		for (String symbol : current.keySet()) {
			Key key = new Key(symbol, now);
			cache.put(key, current.get(symbol));
		}
	}
	
	public void put(Key key, Quote quote) {
		cache.put(key, quote);
	}
	
	public Optional<Double> getPrice(Key key) {
		Quote quote = cache.get(key);
		return getPrice(quote);
	}

	public Optional<Double> getPrice(DateTime nowDateTime, String symbol) {
		Quote quote = null;
		quote = cache.get(new Key(symbol, nowDateTime));
		return getPrice(quote);
	}
	
	private Optional<Double> getPrice(Quote quote) {
		if (quote == null) {
			return Optional.absent();
		}
		Double price = null;
		if (quote.getAdjClosePrice() != null) {
			price = Double.parseDouble(quote.getAdjClosePrice());
		} else if (quote.getLastPrice() != null){
			price = Double.parseDouble(quote.getLastPrice());
		}
		return Optional.fromNullable(price);
	}

}
