package com.joe.finance.optimizer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.base.Optional;
import com.joe.finance.BackTest;
import com.joe.finance.Strategy.IStrategy;
import com.joe.finance.Strategy.MeanReversion;
import com.joe.finance.config.xml.DimValueConfig;
import com.joe.finance.config.xml.PortfolioConfig;
import com.joe.finance.config.xml.RunnerConfig;
import com.joe.finance.config.xml.RunnerConfigUtil;
import com.joe.finance.order.Order;
import com.joe.finance.portfolio.Portfolio;
import com.joe.finance.util.MarketDateTime;

public class ParticleSwarm {
	
	private static final int NUM_PARTICLES = 10;
	private static final int MAX_SWARM_ITERATIONS = 10;
	private List<Particle> particles;
	private MarketDateTime startTime;
	private MarketDateTime endTime;
	private List<Order> winningTrades;
	
	private Map<Dimension, Double> globalMaxPosition;
	private double globalMaxFitness = -1;
	
	public ParticleSwarm(Set<Dimension> dims,
			MarketDateTime startTime, 
			MarketDateTime endTime) {
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
				Portfolio folio = new Portfolio(portfolioConfig);
				IStrategy strategy = new MeanReversion(folio, startTime, endTime);
				for (Dimension dim : strategy.getDimensions()) {
					strategy.setDimValue(dim, particle.getCurrentPosition().get(dim));
				}
				strategies.add(strategy);
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
				for (Dimension dim : strategy.getDimensions()) {
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
		for (Dimension d : MeanReversion.dims) {
			globalMaxPosition.put(d, config.getDimValue(d.getName()));
		}
		this.globalMaxFitness = config.fitnessValue;
	}
	
	private void printResults() {
		for (Dimension d : MeanReversion.dims) {
			String output = String.format("Best %s value : %f", d.getName(), 
					globalMaxPosition.get(d));
			System.out.println(output);
		}
		System.out.println("Best fitness : " + globalMaxFitness);
	}
	
	public static void main(String argv[]) throws Exception {
		
		RunnerConfig config = new RunnerConfig();
		config.dimValues = new ArrayList<>();
		config.startNowMinusNDays =  720;
		ParticleSwarm p = new ParticleSwarm(MeanReversion.dims,
				MarketDateTime.nowMinusNDays(config.startNowMinusNDays),
				MarketDateTime.nowMinusNDays(1));
	  p.importPreviousRun();
		p.execute();
		p.printResults(); 
		
		for (Dimension dim : MeanReversion.dims) {
			DimValueConfig dimConfig = new DimValueConfig();
			dimConfig.name = dim.getName();
			dimConfig.value = p.globalMaxPosition.get(dim);
			config.dimValues.add(dimConfig);
		}
		config.fitnessValue = p.globalMaxFitness;
		RunnerConfigUtil.exportConfig(config);
		
	}
	
}
