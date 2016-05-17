package com.joe.finance.Strategy;

import java.util.HashMap;

import com.joe.finance.portfolio.Portfolio;
import com.joe.finance.portfolio.Portfolio.Asset;

public class Report {
	public String strategyName;
	public Portfolio portfolio;
	public Double cummulativeReturn;
	public Double startValue;
	public Double endValue;
	public Report() {
		startValue = 0d;
		endValue = 0d;
		cummulativeReturn = 0d;
	}
	
	public void printReport() {
		System.out.println("Strategy name : " + strategyName);
		System.out.println(
				String.format("Portfolio start value : $%.2f", startValue));
		System.out.println(
				String.format("Portfolio end value : $%.2f", endValue));
		String s = 
				String.format("Cummlative return : +%.2f%%", 100 * cummulativeReturn);
		System.out.println(s);
		System.out.println("--------------------------------------------------");
		printPortfolio();
	}
	
	public void printPortfolio() {
		HashMap<String, Asset> position = portfolio.getPosition();
		for (String symbol : position.keySet()) {
			Asset asset = position.get(symbol);
			System.out.println(
					String.format("%s : %d shares", symbol, asset.numShares));
		}
		System.out.println(
				String.format("Cash : $%.2f", portfolio.getCash()));
	}
}
