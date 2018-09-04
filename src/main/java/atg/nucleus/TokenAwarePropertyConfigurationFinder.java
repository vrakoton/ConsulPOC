package atg.nucleus;

import atg.nucleus.logging.ApplicationLogging;
import atg.vfs.VirtualFileSystem;

public class TokenAwarePropertyConfigurationFinder extends PropertyConfigurationFinder {
	/**
	 * Just use an explicit constructor which calls the super class' one.
	 * @param pConfigSystems
	 * @param pBeanConfigurator
	 */
	public TokenAwarePropertyConfigurationFinder(VirtualFileSystem[] pConfigSystems, BeanConfigurator pBeanConfigurator) {
		super(pConfigSystems, pBeanConfigurator);
	}
	
	/**
	 * 
	 */
	public Configuration findConfiguration(String pName, boolean pSearchUp, String pBaseName, ApplicationLogging pLog) {
		Configuration conf = super.findConfiguration(pName, pSearchUp, pBaseName, pLog);
		return conf;
	}
}
