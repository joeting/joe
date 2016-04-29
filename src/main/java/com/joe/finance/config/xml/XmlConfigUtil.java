package com.joe.finance.config.xml;

import static com.joe.finance.config.xml.ProjectSettings.CONFIG_LOCATION_PREFIX;
import static com.joe.finance.config.xml.ProjectSettings.FUND_NAME;

import java.io.File;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import com.google.common.base.Optional;

public class XmlConfigUtil {
	
	private static final String SRC_LOCATION_PREFIX = "/src/main/java/com/joe/finance/config/xml/";

	public static final String CONFIG_FILE = CONFIG_LOCATION_PREFIX +
			SRC_LOCATION_PREFIX
			+ FUND_NAME + "/strategy_config.xml";
	
	public static final String PORTFOLIO_CONFIG_FILE = CONFIG_LOCATION_PREFIX +
			SRC_LOCATION_PREFIX
			+ FUND_NAME + "/portfolio.xml";
	
	public static final String OUTPUT_FILE = CONFIG_LOCATION_PREFIX  
			+ SRC_LOCATION_PREFIX 
			+ FUND_NAME + "/pso_output.xml";
	
	
	public static StrategyConfig importConfigFile() {
		try {
			Optional<StrategyConfig> oConfig = 
					XmlConfigUtil.importConfig(CONFIG_FILE, StrategyConfig.class);
			return oConfig.get();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public static Optional<PortfolioConfig> importPortfolioFile()  {
		try {
			return XmlConfigUtil.<PortfolioConfig>importConfig(PORTFOLIO_CONFIG_FILE, 
					PortfolioConfig.class);
		} catch (Exception e) {
			return Optional.absent();
		}
	}
	
	public static Optional<StrategyConfig> importOutputFile()  {
		try {
			Optional<StrategyConfig> config = 
					XmlConfigUtil.<StrategyConfig>importConfig(OUTPUT_FILE, StrategyConfig.class);
			return config;
		} catch (Exception e) {
			System.out.println("Error reading previous PSO output.");
		}
		return Optional.absent();
	}

	public static void export(StrategyConfig config) throws Exception {
		exportConfig(OUTPUT_FILE, config);
	}
	
	public static void export(PortfolioConfig config) throws Exception {
		exportConfig(PORTFOLIO_CONFIG_FILE, config);
	}
	
	@SuppressWarnings("unchecked")
	private static <T> Optional<T> importConfig(String fileName, 
			@SuppressWarnings("rawtypes") Class T) throws Exception {
		JAXBContext jaxbContext = JAXBContext.newInstance(T);
    Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
    T config = null;
    try {
	     config = (T) jaxbUnmarshaller.unmarshal( 
	    		new File(fileName));
    } catch (Exception e) {
    	System.out.println(
    			String.format("Cannot read config file %s for class %s. ",  fileName, T));
    }
    Optional<T> oConfig = Optional.fromNullable(config);
    return oConfig;
	}
	
	private static <T> void exportConfig(String fileName, T classInstance) 
			throws Exception {
		JAXBContext jaxbContext = JAXBContext.newInstance(classInstance.getClass());
		 Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
	    jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
	    jaxbMarshaller.marshal(classInstance, new File(fileName));
	}

}
