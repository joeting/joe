package com.joe.finance;

import java.util.ArrayList;
import java.util.List;

import com.joe.finance.Strategy.IStrategy;
import com.joe.finance.config.xml.PortfolioConfig;
import com.joe.finance.config.xml.StrategyConfig;
import com.joe.finance.config.xml.StrategyFactory;
import com.joe.finance.config.xml.XmlConfigUtil;
import com.joe.finance.optimizer.Dimension;
import com.joe.finance.order.Order;
import com.joe.finance.portfolio.Portfolio;
import com.joe.finance.util.MarketDateTime;

public class StrategyRunner {
	
	private MarketDateTime startTime;
	private List<IStrategy> strategies;
	
	public StrategyRunner() {
		StrategyConfig config = XmlConfigUtil.importConfigFile();
		PortfolioConfig portfolioConfig = XmlConfigUtil.importPortfolioFile().get();
		Portfolio portfolio = new Portfolio(portfolioConfig);
		MarketDateTime startTime = MarketDateTime.nowMinusNDays(config.startNowMinusNDays);
		IStrategy strategy = StrategyFactory.buildStrategy(config, portfolio, MarketDateTime.now());
		
		for (Dimension dim : StrategyFactory.getStrategyDimension(config)) {
			strategy.setDimValue(dim, config.getDimValue(dim.getName()));
		}
		
		List<IStrategy> list = new ArrayList<>();
		list.add(strategy);
		this.startTime = startTime;
		this.strategies = list;
	}
	
	public StrategyRunner(MarketDateTime startTime, List<IStrategy> strategies) {
		this.startTime = startTime;
		this.strategies = strategies;
	}
	
	public void run() {
		BackTest test = new BackTest(
				startTime,
				MarketDateTime.nowMinusNDays(1),
				strategies);
		test.run();
		test.generateReport();
		for (IStrategy strategy : strategies) {
			strategy.run(MarketDateTime.now(), false);
			Order.logTrades(strategy.getTrades());
		}
	}
	
	public static void main(String argv[]) throws Exception {
		StrategyRunner runner = new StrategyRunner();
		runner.run();
	}
	
}
