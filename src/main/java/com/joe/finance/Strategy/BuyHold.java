package com.joe.finance.Strategy;

import java.util.List;
import java.util.Set;

import com.joe.finance.data.QuoteCache;
import com.joe.finance.optimizer.Dimension;
import com.joe.finance.order.Order;
import com.joe.finance.portfolio.Portfolio;
import com.joe.finance.util.MarketDateTime;

// Buy / hold strategy...  Can be overriden.
public class BuyHold implements IStrategy {
	
	private Portfolio portfolio;
	private QuoteCache cache;
	private Report report;
	
	public BuyHold(Portfolio portfolio, QuoteCache cache) {
		this.portfolio = portfolio;
		this.cache = cache;
		report = new Report();
		report.strategyName = "BuyHold";
		report.portfolio = portfolio;
	}
	
	@Override
	public void run(MarketDateTime iterationTime, boolean isBackTest) {
	}
	
	@Override
	public Report getReport(MarketDateTime iterationTime) {
		portfolio.computeReturn(report, iterationTime.time(), cache);
		return report;
	}

	@Override
	public List<Order> getTrades() {
		return null;
	}
	
	@Override
	public Set<Dimension> getDimensions() {
		return null;
	}

	@Override
	public IStrategy setDimValue(Dimension dim, double value) {
		return null;
	}
	
	@Override
	public IStrategy setDebug() {
		return this;
	}
	
}
