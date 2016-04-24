package com.joe.finance.optimizer;

import com.google.common.collect.Range;

public class Dimension {
	
	private String name;
	private Range<Double> valueRange;
	
	public Dimension(String name, Range<Double> valueRange) {
		this.name = name;
		this.valueRange = valueRange;
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
