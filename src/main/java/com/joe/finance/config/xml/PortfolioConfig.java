package com.joe.finance.config.xml;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="portfolio")
public class PortfolioConfig {	
	
	public String name;
	public String lastImportDate;
	@XmlElementWrapper(name="symbols")
	@XmlElement(name="symbol")
	public List<String> symbols;
	public double portfolioStartValue;
	
}
