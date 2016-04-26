package com.joe.finance.optimizer;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Particle {
	
	public static double C1 = 2;
	public static double C2 = 2.5;
	
	private Set<Dimension> dimensions;
	private Map<Dimension, Double> bestPosition;
	private Map<Dimension, Double> currentPosition;
	private Map<Dimension, Double> velocity;
	private Double bestPositionValue;
	
	public Particle(Set<Dimension> dimensions) {
		this.dimensions = dimensions;
		bestPositionValue = -1.0;
		bestPosition = new HashMap<>();
		currentPosition = new HashMap<>();
		velocity = new HashMap<>();
	}
	
	public Particle init() {
		for (Dimension dim : dimensions) {
			currentPosition.put(dim, getRandomValue(dim));
			velocity.put(dim, new Double(0));
		}	
		bestPosition = currentPosition;
		return this;
	}
	
	public void update(Dimension dim, double fitness, double globalMaxPosition) {
		if (fitness > bestPositionValue) {
			bestPosition = cloneCurrentPosition();
			bestPositionValue = fitness;
		}
		Double v = velocity.get(dim);
		v = v + C1 * Math.random() * (bestPosition.get(dim) - currentPosition.get(dim))
					+ C2 * Math.random() * (globalMaxPosition - currentPosition.get(dim));
		velocity.put(dim, v);
		Double p = currentPosition.get(dim) + v;
		p = round(p, dim.getPrecision());
		if (p > dim.getValueRange().upperEndpoint()) {
			p = dim.getValueRange().upperEndpoint();
		} else if (p < dim.getValueRange().lowerEndpoint()) {
			p = dim.getValueRange().lowerEndpoint();
		}
		currentPosition.put(dim, p);
	}

	public Set<Dimension> getDimensions() {
		return dimensions;
	}

	public Map<Dimension, Double> getBestPosition() {
		return bestPosition;
	}
	
	public Map<Dimension, Double> cloneCurrentPosition() {
		Map<Dimension, Double> map = new HashMap<>();
		for (Dimension dim : currentPosition.keySet()) {
			map.put(dim, currentPosition.get(dim));
		}
		return map;
	}

	public Map<Dimension, Double> getCurrentPosition() {
		return currentPosition;
	}

	public Map<Dimension, Double> getVelocity() {
		return velocity;
	}

	public Double getBestPositionValue() {
		return bestPositionValue;
	}

	private double getRandomValue(Dimension dim) {
		double value =  dim.getValueRange().lowerEndpoint() + 
				Math.random() *
					(dim.getValueRange().upperEndpoint() - dim.getValueRange().lowerEndpoint());
		return round(value, dim.getPrecision());
	}
	
	private double round(double value, int precision) {
		BigDecimal bd = new BigDecimal(value).setScale(precision, RoundingMode.HALF_EVEN);
		return bd.doubleValue();
	}
	
}
