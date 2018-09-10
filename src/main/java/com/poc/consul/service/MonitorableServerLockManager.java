package com.poc.consul.service;

import com.ecwid.consul.v1.catalog.CatalogConsulClient;
import com.ecwid.consul.v1.catalog.model.CatalogDeregistration;
import com.ecwid.consul.v1.catalog.model.CatalogRegistration;
import com.poc.consul.annotation.DynamicConfig;
import com.poc.consul.configuration.ConsulConfiguration;

import atg.nucleus.ServiceException;
import atg.service.lockmanager.ServerLockManager;

/**
 * 
 * @author vrakoton
 * 
 * Description:
 * 
 * This is an extension of the @see {@link ServerLockManager} class which has 
 * a self registration capability to Consul.
 *
 */
public class MonitorableServerLockManager extends ServerLockManager implements DiscoverableService {
	CatalogRegistration mRegistration;
	String mServiceId = "slm";
	ConsulConfiguration mConsulConfiguration;
	
	/**
	 * We need to add the slm to the Consul's service catalog for other instances to see it
	 */
	public void doStartService() throws ServiceException {
		super.doStartService();
		
		// --- now publish the status to Consul
		register();
	}
	
	@DynamicConfig(tokenName = "token.slm.port")
	public int getPort() {
		return super.getPort();
	}
	
	
	/**
	 * We need to unregister the service when the ATG component is brought down
	 */
	public void doStopService() throws ServiceException {
		super.doStopService();
		unregister();
	}

	public String getServiceId() {
		return mServiceId;
	}

	public void setServiceId(String pServiceId) {
		mServiceId = pServiceId;
	}

	public ConsulConfiguration getConsulConfiguration() {
		return mConsulConfiguration;
	}

	public void setConsulConfiguration(ConsulConfiguration pConsulConfiguration) {
		mConsulConfiguration = pConsulConfiguration;
	}

	/**
	 * registers the service in Consul
	 */
	@Override
	public boolean register() {
		mRegistration = new CatalogRegistration();
		
		CatalogRegistration.Service service = new CatalogRegistration.Service();
		service.setId(getServiceId());
		service.setService("SLM");
		service.setPort(getPort());
		service.setAddress(getHostName());
		
		mRegistration.setService(service);
		mRegistration.setNode(getHostName());
		mRegistration.setAddress(getHostAddr().toString());
		
		CatalogConsulClient client = new CatalogConsulClient(getConsulConfiguration().getConsulHostName());
		client.catalogRegister(mRegistration);
		vlogInfo("Server lock manager has been registerd to consul as {0}", mRegistration);
		return true;
	}

	/**
	 * unregisters the service from Consul
	 */
	@Override
	public boolean unregister() {
		if (mRegistration != null) {
			CatalogDeregistration r = new CatalogDeregistration();
			r.setNode(getHostName());
			r.setServiceId(getServiceId());
			CatalogConsulClient client = new CatalogConsulClient(getConsulConfiguration().getConsulHostName());
			client.catalogDeregister(r);
		}
		return true;
	}

}
