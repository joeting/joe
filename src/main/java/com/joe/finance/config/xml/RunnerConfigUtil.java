package com.joe.finance.config.xml;

import java.io.File;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import com.google.common.base.Optional;

public class RunnerConfigUtil {
	
	private static final String FILE_PREFIX = "/Users/IPL/git/joe";

	public static final String CONFIG_FILE = FILE_PREFIX +
			"/src/main/java/com/joe/finance/config/xml/config.xml";
	
	public static final String PORTFOLIO_CONFIG_FILE = FILE_PREFIX +
			"/src/main/java/com/joe/finance/config/xml/portfolio.xml";
	
	public static final String OUTPUT_FILE = FILE_PREFIX  
			+ "/src/main/java/com/joe/finance/config/xml/pso_output.xml";
	
	
	public static Optional<RunnerConfig> importConfigFile() {
		try {
			return RunnerConfigUtil.importConfig(CONFIG_FILE, RunnerConfig.class);
		} catch (Exception e) {
			return Optional.absent();
		}
	}
	
	public static Optional<PortfolioConfig> importPortfolioFile()  {
		try {
			return RunnerConfigUtil.<PortfolioConfig>importConfig(PORTFOLIO_CONFIG_FILE, 
					PortfolioConfig.class);
		} catch (Exception e) {
			return Optional.absent();
		}
	}
	
	public static Optional<RunnerConfig> importOutputFile() throws Exception {
		return RunnerConfigUtil.<RunnerConfig>importConfig(OUTPUT_FILE, RunnerConfig.class);
	}

	@SuppressWarnings("unchecked")
	public static <T> Optional<T> importConfig(String fileName, 
			@SuppressWarnings("rawtypes") Class T) throws Exception {
		JAXBContext jaxbContext = JAXBContext.newInstance(T);
    Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
    T config = null;
    try {
	     config = (T) jaxbUnmarshaller.unmarshal( 
	    		new File(fileName));
    } catch (Exception e) {
    	System.out.println("Cannot read conf for " + T);
    }
    Optional<T> oConfig = Optional.fromNullable(config);
    return oConfig;
	}
	
	public static void exportConfig(RunnerConfig config) throws Exception {
		JAXBContext jaxbContext = JAXBContext.newInstance(RunnerConfig.class);
    Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
    jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
    jaxbMarshaller.marshal(config, new File(
    		OUTPUT_FILE));
	}

}
