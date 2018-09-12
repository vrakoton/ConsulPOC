package com.poc.consul.service;

import java.rmi.RemoteException;

import com.ecwid.consul.v1.catalog.CatalogConsulClient;
import com.ecwid.consul.v1.catalog.model.CatalogDeregistration;
import com.ecwid.consul.v1.catalog.model.CatalogRegistration;
import com.poc.consul.configuration.ConsulConfiguration;

import atg.nucleus.ServiceException;
import atg.server.rmi.RmiServer;

/**
 * 
 * @author vrakoton 
 * Description:
 * 
 *         This is an implementation of the RMI server which registers available
 *         services to Consul on startup.
 *         
 *         RMI services can be queried on Consul using the following curl command to test:
 *         
 *         curl  http://localhost:8500/v1/catalog/service/RMI
 *
 */
public class DiscoverableRmiServer extends RmiServer implements DiscoverableService {
	ConsulConfiguration mConsulConfiguration;

	/**
	 * overrides the original start method to register this server to Consul
	 */
	public void doStartService() throws ServiceException {
		super.doStartService();
		register();
	}
	
	/**
	 * overrides the original stop method to unregister this service from Consul
	 */
	public void doStopService() {
		unregister();
		super.doStopService();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean register() {
		
		try {
			String [] services = getRegistry().list();
			if (services == null || services.length < 1) {
				vlogInfo("No Rmi services to register on this instance");
				return true;
			}
	
			for (int i = 0; i < services.length; i++) {
				final String s = services[i];
				
				CatalogRegistration.Service service = new CatalogRegistration.Service();
				service.setId("/" + s);
				service.setService("RMI");
				service.setPort(getPort());
				service.setAddress(getHostName());
				
				CatalogRegistration registration = new CatalogRegistration();
				registration.setService(service);
				registration.setNode(getHostName());
				registration.setAddress(getHostName());
				
				CatalogConsulClient client = new CatalogConsulClient(getConsulConfiguration().getConsulHostName());
				client.catalogRegister(registration);
				vlogInfo("Server lock manager has been registerd to consul as {0}", registration);
			}
		} catch (RemoteException exc) {
			vlogError(exc, "An error occured during RMI service registration");
			return false;
		}

		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean unregister() {
		try {
			String [] services = getRegistry().list();
			for (int i = 0; i < services.length; i++) {
				final String s = services[i];
				CatalogDeregistration r = new CatalogDeregistration();
				r.setNode(getHostName());
				r.setServiceId("/" + s);
				CatalogConsulClient client = new CatalogConsulClient(getConsulConfiguration().getConsulHostName());
				client.catalogDeregister(r);
			}
		} catch (RemoteException re) {
			vlogError(re, "unable to unregister all RMI services");
			return false;
		}
		
		return true;
	}

	public ConsulConfiguration getConsulConfiguration() {
		return mConsulConfiguration;
	}

	public void setConsulConfiguration(ConsulConfiguration pConsulConfiguration) {
		mConsulConfiguration = pConsulConfiguration;
	}

}
