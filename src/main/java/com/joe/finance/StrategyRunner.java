package com.joe.finance;

import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Optional;
import com.joe.finance.Strategy.IStrategy;
import com.joe.finance.Strategy.MeanReversion;
import com.joe.finance.config.xml.PortfolioConfig;
import com.joe.finance.config.xml.RunnerConfig;
import com.joe.finance.config.xml.RunnerConfigUtil;
import com.joe.finance.optimizer.Dimension;
import com.joe.finance.order.Order;
import com.joe.finance.portfolio.Portfolio;
import com.joe.finance.util.MarketDateTime;

public class StrategyRunner {
	
	private MarketDateTime startTime;
	private List<IStrategy> strategies;
	
	public StrategyRunner() {
		Optional<RunnerConfig> oConfig = RunnerConfigUtil.importConfigFile();
		if (!oConfig.isPresent()) {
			System.out.println("Nothing to run..  Config.xml not imported properly.");
			return;
		}
		RunnerConfig config = oConfig.get();
		PortfolioConfig portfolioConfig = RunnerConfigUtil.importPortfolioFile().get();
		Portfolio folio = new Portfolio(portfolioConfig);
		MarketDateTime startTime = MarketDateTime.nowMinusNDays(config.startNowMinusNDays);
		MeanReversion strategy = new MeanReversion(folio, startTime, MarketDateTime.now());
		
		for (Dimension dim : MeanReversion.dims) {
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
			strategy.run(MarketDateTime.now(), true);
			Order.logTrades(strategy.getTrades());
		}
	}
	
	public static void main(String argv[]) throws Exception {
		StrategyRunner runner = new StrategyRunner();
		runner.run();
	}
	
}
