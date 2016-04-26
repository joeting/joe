package com.joe.finance.config.xml;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="config")
public class RunnerConfig {
	
	public int startNowMinusNDays;
	@XmlElementWrapper(name="dimValues")
	@XmlElement(name="dimValue")
	public List<DimValueConfig> dimValues;
	public double fitnessValue;
	
	public double getDimValue(String name) {
		for (DimValueConfig config : dimValues) {
			if (config.name.equals(name)) {
				return config.value;
			}
		}
		return 0;
	}
	
}
