package com.joe.finance;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.joe.finance.Strategy.IStrategy;
import com.joe.finance.Strategy.Report;
import com.joe.finance.util.MarketDateTime;

public class BackTest {
	
	private MarketDateTime startTime;
	private MarketDateTime endTime;
	private MarketDateTime iterationTime;
	private List<IStrategy> strategies;
	
	
	public BackTest(MarketDateTime startTime, MarketDateTime endTime, IStrategy strategy) {
		this(startTime, endTime, new ArrayList<>(
				Arrays.asList(strategy)));
	}
	
	public BackTest(MarketDateTime startTime, MarketDateTime endTime, List<IStrategy> strategies) {
		this.startTime = startTime;
		this.endTime = endTime;
		this.iterationTime = startTime;
		this.strategies = strategies;
	}
	
	public void run() {
		while (iterationTime.isBefore(endTime)) {
			for (IStrategy strategy : strategies) {
				strategy.run(iterationTime, true);
			}
			iterationTime = iterationTime.plusDays(1);
		}
		endTime = iterationTime.minusDays(1);
	}
	
	public void generateReport() {
		System.out.println("--------------------------------------------------");
		System.out.println("Start Time : " + startTime);
		System.out.println("End Time   : " + endTime);
		System.out.println("--------------------------------------------------");
		for (IStrategy strategy : strategies) {
			Report report = strategy.getReport(endTime);
			report.printReport();
		}
		System.out.println("--------------------------------------------------");
	}
	
}
