package com.joe.finance.optimizer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Range;
import com.joe.finance.BackTest;
import com.joe.finance.Strategy.IStrategy;
import com.joe.finance.Strategy.MeanReversion;
import com.joe.finance.config.xml.PortfolioConfig;
import com.joe.finance.config.xml.RunnerConfig;
import com.joe.finance.config.xml.RunnerConfigUtil;
import com.joe.finance.order.Order;
import com.joe.finance.portfolio.Portfolio;
import com.joe.finance.util.MarketDateTime;

public class ParticleSwarm {
	
	private static final int NUM_PARTICLES = 10;
	private static final int MAX_SWARM_ITERATIONS = 10;
	private Set<Dimension> dims;
	private List<Particle> particles;
	private MarketDateTime startTime;
	private MarketDateTime endTime;
	private List<Order> winningTrades;
	
	private Map<Dimension, Double> globalMaxPosition;
	private double globalMaxFitness = -1;
	
	public ParticleSwarm(Set<Dimension> dims, 
			MarketDateTime startTime, 
			MarketDateTime endTime) {
		this.dims = dims;
		this.startTime = startTime;
		this.endTime = endTime;
		particles = new ArrayList<>();
		for (int i = 0; i < NUM_PARTICLES; i++) { 
			Particle p = new Particle(dims).init();
			particles.add(p);
		}
	}
	
	public void execute() {
		PortfolioConfig portfolioConfig = RunnerConfigUtil.importPortfolioFile().get();
		for (int i = 0; i < MAX_SWARM_ITERATIONS; i++) {
			String o = String.format("Particle swarm running iteration %d / %d", i+1, 
					MAX_SWARM_ITERATIONS);
			System.out.println(o);
			List<IStrategy> strategies = new ArrayList<>();
			// Compute fitness for each particle
			// Each particle converts to a strategy instance
			for (Particle particle : particles) {
				int n = 0;
				double us = MeanReversion.UPPER_SIGMA;
				double ls = MeanReversion.LOWER_SIGMA;
				double mbr = 1.0;
				double msr = 1.0;
				for (Dimension dim : dims) {
					if (dim.getName().equals("n")) {
						n = (int) particle.getCurrentPosition().get(dim).doubleValue();
					} else if ("us".equals(dim.getName())) {
						us = particle.getCurrentPosition().get(dim);
					} else if ("ls".equals(dim.getName())) {
						ls = particle.getCurrentPosition().get(dim);
					} else if ("mbr".equals(dim.getName())) {
						mbr = particle.getCurrentPosition().get(dim);
					} else if ("msr".equals(dim.getName())) {
						msr = particle.getCurrentPosition().get(dim);
					}
				}
				Portfolio folio = new Portfolio(portfolioConfig);
				strategies.add(new MeanReversion(n, folio, startTime, endTime)
						.setUpperSigma(us)
						.setLowerSigma(ls)
						.setMaxBuyRatio(mbr)
						.setMaxSellRatio(msr));
			}
			BackTest test = new BackTest(
					startTime,
					endTime,
					strategies);
			test.run();
			int index = 0;
			for (IStrategy strategy : strategies) {
				double fitnessValue = strategy.getReport(endTime).cummulativeReturn;
				Particle particle = particles.get(index);
				if (fitnessValue > globalMaxFitness) {
					globalMaxFitness = fitnessValue;
					globalMaxPosition = particle.cloneCurrentPosition();
					winningTrades = strategy.getTrades();
				}
				for (Dimension dim : dims) {
					double globalMaxPositionDim = globalMaxPosition.containsKey(dim) 
							? globalMaxPosition.get(dim) : -1;
					particle.update(dim, fitnessValue, globalMaxPositionDim);
				}
				index++;
			}
		}
		if (winningTrades != null) {
			Order.logTrades(winningTrades);
		}
		
	}
	
	private void importPreviousRun() throws Exception {
		Optional<RunnerConfig> oConfig = RunnerConfigUtil.importOutputFile();
		globalMaxPosition = new HashMap<>();
		if (!oConfig.isPresent()) { 
			return;
		}
		RunnerConfig config = oConfig.get();
		for (Dimension d : dims) {
			switch(d.getName()) {
				case "n":
					globalMaxPosition.put(d, config.n);
				break;
				case "us":
					globalMaxPosition.put(d, config.upperSigma);
				break;
				case "ls":
					globalMaxPosition.put(d, config.lowerSigma);
				break;
				case "mbr":
					globalMaxPosition.put(d, config.maxBuyRatio);
				break;
				case "msr":
					globalMaxPosition.put(d, config.maxSellRatio);
				break;
			}
		}
		this.globalMaxFitness = config.fitnessValue;
	}
	
	private void printResults() {
		for (Dimension d : dims) {
			String output = String.format("Best %s value : %f", d.getName(), 
					globalMaxPosition.get(d));
			System.out.println(output);
		}
		System.out.println("Best fitness : " + globalMaxFitness);
	}
	
	public static void main(String argv[]) throws Exception {
		
		Dimension d = new Dimension("n", Range.closed(30.0, 120.0), 0);
		Dimension dus = new Dimension("us", Range.closed(0.5, 2.0), 1);
		Dimension dls = new Dimension("ls", Range.closed(0.5, 2.0), 1);
		Dimension mbr = new Dimension("mbr", Range.closed(0.1, 0.3), 2);
		Dimension msr = new Dimension("msr", Range.closed(0.2, 1.0), 2);
		RunnerConfig config = new RunnerConfig();
		config.startNowMinusNDays = 720;
		ParticleSwarm p = new ParticleSwarm(ImmutableSet.of(d, dus, dls, mbr, msr),
				MarketDateTime.nowMinusNDays(config.startNowMinusNDays),
				MarketDateTime.nowMinusNDays(1));
	  p.importPreviousRun();
		p.execute();
		p.printResults(); 
		config.n = p.globalMaxPosition.get(d);
		config.lowerSigma = p.globalMaxPosition.get(dls);
		config.upperSigma = p.globalMaxPosition.get(dus);
		config.maxBuyRatio = p.globalMaxPosition.get(mbr);
		config.maxSellRatio = p.globalMaxPosition.get(msr);
		config.fitnessValue = p.globalMaxFitness;
		RunnerConfigUtil.exportConfig(config);
		
	}
	
}
