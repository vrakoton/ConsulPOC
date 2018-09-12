package com.poc.consul.service;

/**
 * 
 * @author vrakoton
 * Description:
 * 
 * A discoverable service is a service which can register/unregister itself
 * to Consul for other instances to know their location and use them.
 *
 */
public interface DiscoverableService {
	/**
	 * The register method is used to register the service in Consul service catalog.
	 * @return true if registration is successful
	 */
	public boolean register();
	/**
	 * The unregister method is used to unregister the service in Consul service catalog.
	 * @return true if de-registration is successful
	 */
	public boolean unregister();
}
