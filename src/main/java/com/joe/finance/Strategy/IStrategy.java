package com.joe.finance.Strategy;

import java.util.List;
import java.util.Set;

import com.joe.finance.optimizer.Dimension;
import com.joe.finance.order.Order;
import com.joe.finance.util.MarketDateTime;

public interface IStrategy {
	public void run(MarketDateTime currentIteration, boolean isBackTest);
	public Report getReport(MarketDateTime now);
	public List<Order> getTrades();
	public Set<Dimension> getDimensions();
	public IStrategy setDimValue(Dimension dim, double value);
	public IStrategy setDebug();
}
