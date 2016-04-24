package com.joe.finance.optimizer;

import com.google.common.collect.Range;

public class Dimension {
	
	private String name;
	private Range<Double> valueRange;
	private int precision; 
	
	public Dimension(String name, Range<Double> valueRange, int precision) {
		this.name = name;
		this.valueRange = valueRange;
		this.precision = precision;
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}

	public Range<Double> getValueRange() {
		return valueRange;
	}
	
	public void setValueRange(Range<Double> valueRange) {
		this.valueRange = valueRange;
	}
	
	
	public int getPrecision() {
		return precision;
	}

	public void setPrecision(int precision) {
		this.precision = precision;
	}

	@Override 
	public int hashCode() {
		return name.hashCode();
	}
	
	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		Dimension dim = (Dimension) o;
		boolean result = getName().equals(dim.getName());
		return result;
	}
	
}
