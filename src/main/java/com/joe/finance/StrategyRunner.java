package com.joe.finance;

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
	private IStrategy strategy;
	
	public StrategyRunner() {
		StrategyConfig config = XmlConfigUtil.importConfigFile();
		PortfolioConfig portfolioConfig = XmlConfigUtil.importPortfolioFile().get();
		Portfolio portfolio = new Portfolio(portfolioConfig);
		MarketDateTime startTime = MarketDateTime.nowMinusNDays(config.startNowMinusNDays);
		IStrategy strategy = StrategyFactory.buildStrategy(config, portfolio, MarketDateTime.now());
		// strategy.setDebug();
		for (Dimension dim : StrategyFactory.getStrategyDimension(config)) {
			strategy.setDimValue(dim, config.getDimValue(dim.getName()));
		}
		this.startTime = startTime;
		this.strategy = strategy;
	}
	
	public StrategyRunner(MarketDateTime startTime, IStrategy strategy) {
		this.startTime = startTime;
		this.strategy = strategy;
	}
	
	public void run() {
		BackTest test = new BackTest(
				startTime,
				MarketDateTime.now(),
				strategy);
		test.run();
		test.generateReport();
		Order.logTrades(strategy.getTrades());
	}
	
	public static void main(String argv[]) throws Exception {
		StrategyRunner runner = new StrategyRunner();
		runner.run();
	}
	
}
