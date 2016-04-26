package com.joe.finance.importer;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

import com.joe.finance.config.xml.PortfolioConfig;
import com.joe.finance.config.xml.RunnerConfigUtil;
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
		PortfolioConfig portfolioConfig = RunnerConfigUtil.importPortfolioFile().get();
		Portfolio portfolio = new Portfolio(portfolioConfig);
		importer.refreshHistoricalData(portfolio.getWatch(), 1);
		System.out.println("---Import Complete---");
	}
	
}
