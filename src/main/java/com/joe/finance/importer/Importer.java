package com.joe.finance.importer;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.joe.finance.config.xml.PortfolioConfig;
import com.joe.finance.config.xml.XmlConfigUtil;
import com.joe.finance.data.Quote;
import com.joe.finance.data.QuoteDao;
import com.joe.finance.portfolio.Portfolio;

public class Importer {
	
	private QuoteDao dao;
	
	public Importer() {
		dao = QuoteDao.quoteDao();
	}

	public void refreshHistoricalData(Collection<String> symbols, int months) 
			throws IOException {
		for (String symbol : symbols) {
			refreshHistoricalData(symbol, months);
		}
	}
	
	public void importHistoricalData(Collection<String> symbols, int years) 
			throws IOException {
		for (String symbol : symbols) {
			importHistoricalData(symbol, years);
		}
	}
	
	public void refreshHistoricalData(String symbol, int months) throws IOException {
		List<Quote> quotes = YahooReader.getHistoricalDataByMonth(symbol, months);
		dao.insert(quotes);
	}
	
	public void importHistoricalData(String symbol, int years) throws IOException {
		List<Quote> quotes = YahooReader.getHistoricalData(symbol, years);
		dao.insert(quotes);
	}
	
	public static void main(String[] args) throws Exception {
		Importer importer = new Importer();
		PortfolioConfig portfolioConfig = XmlConfigUtil.importPortfolioFile().get();
		DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd");
		Portfolio portfolio = new Portfolio(portfolioConfig);
		DateTime now = DateTime.now();
		if (portfolioConfig.lastImportDate == null) {
			importer.importHistoricalData(portfolio.getWatch(),
					3);
		} else {
			DateTime time = formatter.parseDateTime(portfolioConfig.lastImportDate);
			Days days = Days.daysBetween(time, now);
			int monthsElapsed = days.getDays() / 30 + 1;
			importer.refreshHistoricalData(portfolio.getWatch(), monthsElapsed);
		}
		portfolioConfig.lastImportDate = formatter.print(now);
		XmlConfigUtil.export(portfolioConfig);
		System.out.println("---Import Complete---");
	}
	
}
