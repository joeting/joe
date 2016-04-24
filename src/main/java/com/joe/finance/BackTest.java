package com.joe.finance;

import java.util.ArrayList;
import java.util.List;

import com.joe.finance.Strategy.BuyHold;
import com.joe.finance.Strategy.IStrategy;
import com.joe.finance.Strategy.MeanReversion;
import com.joe.finance.Strategy.Report;
import com.joe.finance.data.QuoteCache;
import com.joe.finance.data.QuoteDao;
import com.joe.finance.portfolio.Portfolio;
import com.joe.finance.util.MarketDateTime;

public class BackTest {
	
	private MarketDateTime startTime;
	private MarketDateTime endTime;
	private MarketDateTime iterationTime;
	private List<IStrategy> strategies;
	
	private QuoteDao dao;
	
	public BackTest(MarketDateTime startTime, MarketDateTime endTime, List<IStrategy> strategies) {
		this.startTime = startTime;
		this.endTime = endTime;
		this.iterationTime = startTime;
		this.strategies = strategies;
		this.dao = QuoteDao.quoteDao();
	}
	
	public void run() {
		while (iterationTime.time().isBefore(endTime.time())) {
			for (IStrategy strategy : strategies) {
				strategy.run(iterationTime, true);
			}
			iterationTime = iterationTime.plusDays(1);
		}
		endTime = iterationTime.minusDays(1);
	}
	
	public QuoteCache getCache() {
		return dao.getCache();
	}
	
	public void generateReport() {
		System.out.println("--------------------------------------------------");
		System.out.println("Start Time : " + startTime);
		System.out.println("End Time   : " + endTime);
		System.out.println("--------------------------------------------------");
		for (IStrategy strategy : strategies) {
			Report report = strategy.getReport(endTime);
			System.out.println(
					String.format("Portfolio start value : $%.2f", report.startValue));
			System.out.println(
					String.format("Portfolio end value : $%.2f", report.endValue));
			String s = 
					String.format("Cummlative return : %.2f%%", report.cummulativeReturn);
			System.out.println(s);
		}
		System.out.println("--------------------------------------------------");
	}
	
	public static void main(String args[]) {
		// Starting date must be a valid Trading day...
		MarketDateTime startTime = 	MarketDateTime.nowMinusNDays(720);
		MarketDateTime endTime = MarketDateTime.nowMinusNDays(1);
		QuoteDao dao = QuoteDao.quoteDao();
		QuoteCache cache = dao.getCache();
		
		int portFolioStartValue = 1000000;
		Portfolio folio = new Portfolio("Buy Hold Portfolio", portFolioStartValue);
		int shares = (int) (portFolioStartValue / cache.getPrice(startTime.time(), "NFLX").get());
		folio.buyShares(startTime.time(), cache, "NFLX", shares);
		IStrategy buyHold = new BuyHold(folio, cache);
		
		List<IStrategy> strategies = new ArrayList<>();
		strategies.add(buyHold);
		strategies.addAll(MeanReversion.TestBuilder.buildTests(startTime, endTime));
		BackTest test = new BackTest(
				startTime,
				endTime,
				strategies);
		test.run();
		test.generateReport();
		// for (IStrategy strategy : strategies) {
		// 	Order.logTrades(strategy.getTrades());
		// }
	}
	
}
