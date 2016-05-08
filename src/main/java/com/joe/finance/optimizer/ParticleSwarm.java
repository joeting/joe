package com.joe.finance.optimizer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.base.Optional;
import com.joe.finance.BackTest;
import com.joe.finance.Strategy.IStrategy;
import com.joe.finance.config.xml.DimValueConfig;
import com.joe.finance.config.xml.PortfolioConfig;
import com.joe.finance.config.xml.StrategyConfig;
import com.joe.finance.config.xml.StrategyFactory;
import com.joe.finance.config.xml.XmlConfigUtil;
import com.joe.finance.order.Order;
import com.joe.finance.portfolio.Portfolio;
import com.joe.finance.util.MarketDateTime;

public class ParticleSwarm {
	
	private static final int NUM_PARTICLES = 50;
	private static final int MAX_SWARM_ITERATIONS = 10;
	
	private StrategyConfig config;
	private List<Particle> particles;
	private MarketDateTime startTime;
	private MarketDateTime endTime;
	private List<Order> winningTrades;
	
	private Map<Dimension, Double> globalMaxPosition;
	private double globalMaxFitness = -1;
	
	public ParticleSwarm(MarketDateTime endTime) {
		this.config = XmlConfigUtil.importConfigFile();
		importPreviousRun();
		this.startTime = MarketDateTime.nowMinusNDays(config.startNowMinusNDays);
		this.endTime = endTime;
		particles = new ArrayList<>();
		StrategyFactory.init(config);
		for (int i = 0; i < NUM_PARTICLES; i++) { 
			Particle p = new Particle(StrategyFactory.getStrategyDimension(config)).init();
			particles.add(p);
		}
	}
	
	public void execute() {
		PortfolioConfig portfolioConfig = XmlConfigUtil.importPortfolioFile().get();
		for (int i = 0; i < MAX_SWARM_ITERATIONS; i++) {
			String o = String.format("Particle swarm running iteration %d / %d", i+1, 
					MAX_SWARM_ITERATIONS);
			System.out.println(o);
			List<IStrategy> strategies = new ArrayList<>();
			// Compute fitness for each particle
			// Each particle converts to a strategy instance
			for (Particle particle : particles) {
				Portfolio folio = new Portfolio(portfolioConfig);
				IStrategy strategy = StrategyFactory.buildStrategy(config, folio, endTime);
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

	public void exportResults() throws Exception {
		StrategyConfig eConfig = new StrategyConfig();
		eConfig.dimValues = new ArrayList<>();
		eConfig.name = config.name;
		eConfig.startNowMinusNDays = config.startNowMinusNDays;
		for (Dimension dim : StrategyFactory.getStrategyDimension(config)) {
			DimValueConfig dimConfig = new DimValueConfig();
			dimConfig.name = dim.getName();
			dimConfig.value = globalMaxPosition.get(dim);
			eConfig.dimValues.add(dimConfig);
		}
		eConfig.fitnessValue = globalMaxFitness;
		XmlConfigUtil.export(eConfig);
	}
	
	private void importPreviousRun() {
		Optional<StrategyConfig> oConfig = XmlConfigUtil.importOutputFile();
		globalMaxPosition = new HashMap<>();
		if (!oConfig.isPresent()) { 
			return;
		}
		StrategyConfig config = oConfig.get();
		for (Dimension d : StrategyFactory.getStrategyDimension(config)) {
			globalMaxPosition.put(d, config.getDimValue(d.getName()));
		}
		this.globalMaxFitness = config.fitnessValue;
	}
	
	private void printResults() {
		for (Dimension d : StrategyFactory.getStrategyDimension(config)) {
			String output = String.format("Best %s value : %f", d.getName(), 
					globalMaxPosition.get(d));
			System.out.println(output);
		}
		System.out.println("Best fitness : " + globalMaxFitness);
	}
	
	public static void main(String argv[]) throws Exception {
		
		ParticleSwarm p = new ParticleSwarm(MarketDateTime.nowMinusNDays(1));
		p.execute();
		p.printResults(); 
		p.exportResults();
		
	}
	
}
