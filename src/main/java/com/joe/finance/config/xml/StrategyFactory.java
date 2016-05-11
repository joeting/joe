package com.joe.finance.config.xml;

import java.util.HashSet;
import java.util.Set;

import com.joe.finance.Strategy.IStrategy;
import com.joe.finance.Strategy.MeanReversion;
import com.joe.finance.Strategy.Momentum;
import com.joe.finance.optimizer.Dimension;
import com.joe.finance.portfolio.Portfolio;
import com.joe.finance.util.MarketDateTime;

public class StrategyFactory {
	
	public static Set<Dimension> init(StrategyConfig config) {
		if ("MeanReversion".equalsIgnoreCase(config.name)) {
			MeanReversion.init();
			return MeanReversion.dims;
		} else if ("Momentum".equalsIgnoreCase(config.name)) {
			Momentum.init();
			return Momentum.dims;
		}
		return new HashSet<>();
	}
	
	public static IStrategy buildStrategy(StrategyConfig config,
			Portfolio portfolio,
			MarketDateTime endTime) {
		MarketDateTime startTime = MarketDateTime.nowMinusNDays(config.startNowMinusNDays);
		if ("MeanReversion".equalsIgnoreCase(config.name)) {
			return new MeanReversion(portfolio, startTime, endTime);
		} else if ("Momentum".equalsIgnoreCase(config.name)) {
			return new Momentum(portfolio, startTime, endTime);
		}
		throw new IllegalArgumentException("Invalid strategy configuration.");
	}
	
}
