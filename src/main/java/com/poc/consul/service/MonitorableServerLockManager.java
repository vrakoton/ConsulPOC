package com.poc.consul.service;

import com.ecwid.consul.v1.catalog.CatalogConsulClient;
import com.ecwid.consul.v1.catalog.model.CatalogDeregistration;
import com.ecwid.consul.v1.catalog.model.CatalogRegistration;

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
public class MonitorableServerLockManager extends ServerLockManager {
	CatalogRegistration mRegistration;
	String mServiceId = "slm";
	
	/**
	 * We need to add the slm to the Consul's service catalog for other instances to see it
	 */
	public void doStartService() throws ServiceException {
		super.doStartService();
		
		// --- now publish the status to Consul
		
		mRegistration = new CatalogRegistration();
		
		CatalogRegistration.Service service = new CatalogRegistration.Service();
		service.setId(getServiceId());
		service.setService("SLM");
		service.setPort(getPort());
		service.setAddress(getHostName());
		
		mRegistration.setService(service);
		mRegistration.setNode(getHostName());
		mRegistration.setAddress(getHostAddr().toString());
		
		CatalogConsulClient client = new CatalogConsulClient("consul.local.com");
		client.catalogRegister(mRegistration);
		vlogInfo("Server lock manager has been registerd to consul as {0}", mRegistration);
	}
	
	/**
	 * We need to unregister the service when the ATG component is brought down
	 */
	public void doStopService() throws ServiceException {
		super.doStopService();
		if (mRegistration != null) {
			CatalogDeregistration r = new CatalogDeregistration();
			r.setNode(getHostName());
			r.setServiceId(getServiceId());
			CatalogConsulClient client = new CatalogConsulClient("consul.local.com");
			client.catalogDeregister(r);
		}
	}

	public String getServiceId() {
		return mServiceId;
	}

	public void setServiceId(String pServiceId) {
		mServiceId = pServiceId;
	}

}
