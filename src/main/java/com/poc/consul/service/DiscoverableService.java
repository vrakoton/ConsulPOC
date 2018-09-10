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
	public boolean register();
	public boolean unregister();
}
