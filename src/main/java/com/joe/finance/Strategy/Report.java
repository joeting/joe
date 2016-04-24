package com.joe.finance.Strategy;

public class Report {
	public String strategyName;
	public String portfolioName;
	public Double cummulativeReturn;
	public Double startValue;
	public Double endValue;
	public Report() {
		startValue = 0d;
		endValue = 0d;
		cummulativeReturn = 0d;
	}
}
