package com.joe.finance.importer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.joe.finance.data.Quote;

import yahoofinance.Stock;
import yahoofinance.YahooFinance;
import yahoofinance.histquotes.HistoricalQuote;
import yahoofinance.histquotes.Interval;
import yahoofinance.quotes.stock.StockQuote;

public class YahooReader {

	public static Map<String, Quote> getQuote(Set<String> symbols) throws IOException {
		Map<String, Quote> quotes = new HashMap<>();
		if (symbols == null || symbols.isEmpty()) {
			return quotes;
		}
		String[] symbolArray = new String[symbols.size()];
		symbols.toArray(symbolArray);
		Map<String, Stock> map = YahooFinance.get(symbolArray);
		for (String symbol : map.keySet()) {
			StockQuote stockQuote = map.get(symbol).getQuote();
			Quote quote = new Quote(symbol, stockQuote.getLastTradeTime(), 
					null);
			quote.setLastPrice(stockQuote.getPrice().toString());
			quotes.put(symbol, quote);
		}
		return quotes;
	}
	
	public static List<Quote> getHistoricalDataByMonth (String symbol, int months) 
			throws IOException {
		List<Quote> quotes = new ArrayList<>();
		Calendar from = Calendar.getInstance();
		Calendar to = Calendar.getInstance();
		int numMonths = -months;
		from.add(Calendar.MONTH, numMonths); 

		getHistoricalData(symbol, quotes, from, to);
		return quotes;
	}
	
	public static List<Quote> getHistoricalData(String symbol, int years) throws IOException {
		List<Quote> quotes = new ArrayList<>();
		Calendar from = Calendar.getInstance();
		Calendar to = Calendar.getInstance();
		int numYears = -years;
		from.add(Calendar.YEAR, numYears); 

		getHistoricalData(symbol, quotes, from, to);
		return quotes;
	}

	private static void getHistoricalData(String symbol, List<Quote> quotes, Calendar from, 
			Calendar to) throws IOException {
		Stock stock = YahooFinance.get(symbol);
		List<HistoricalQuote> histQuotes = stock.getHistory(from, to, Interval.DAILY);
		for (HistoricalQuote h : histQuotes) {
			Quote quote = new Quote(symbol, h.getDate(), h.getAdjClose().toString());
			quote.setAdjClosePrice(h.getAdjClose().toString());
			quote.setClosePrice(h.getClose().toString());
			quote.setHighPrice(h.getHigh().toString());
			quote.setLowPrice(h.getLow().toString());
			quote.setOpenPrice(h.getOpen().toString());
			quote.setVolume(h.getVolume());
			quotes.add(quote);
		}
	}

}
