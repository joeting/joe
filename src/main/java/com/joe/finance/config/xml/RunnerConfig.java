package com.joe.finance.config.xml;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="config")
public class RunnerConfig {
	
	public int startNowMinusNDays;
	public double n;
	public double upperSigma;
	public double lowerSigma;
	public double maxBuyRatio;
	public double maxSellRatio;
	public double fitnessValue;
	
}
