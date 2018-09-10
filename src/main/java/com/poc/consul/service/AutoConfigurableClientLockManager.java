package com.poc.consul.service;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import com.ecwid.consul.v1.Response;
import com.ecwid.consul.v1.catalog.CatalogConsulClient;
import com.ecwid.consul.v1.catalog.model.CatalogService;
import com.poc.consul.configuration.ConsulConfiguration;

import atg.nucleus.ServiceException;
import atg.service.lockmanager.ClientLockManager;

/**
 * 
 * @author vrakoton
 * 
 * Description:
 * 
 * This is an extension of the client lock manager which will poll Consul to get the coordinates of
 * the started server lock managers in a given cluster.
 *
 */
public class AutoConfigurableClientLockManager extends ClientLockManager {
	ConsulConfiguration mConsulConfiguration;
	/**
	 * Starts the service and discovers automatically the SLM coordinates
	 */
	public void doStartService() throws ServiceException {
		CatalogConsulClient client = new CatalogConsulClient(getConsulConfiguration().getConsulHostName());
		Response<List<CatalogService>> services = client.getCatalogService("SLM", null);
		if (services == null) {
			vlogError("Could not get services named SLM");
			super.doStartService();
			return;
		}
		List<InetAddress> slmServers = new ArrayList<InetAddress>();
		List<Integer> slmPorts = new ArrayList<Integer>();
		for (CatalogService service : services.getValue()) {
			try {
				slmServers.add(InetAddress.getByName(service.getNode()));
			} catch (UnknownHostException uhe) {
				vlogError(uhe, "Can not resolve server lock manager address {0}", service.getNode());
			}
		  slmPorts.add(new Integer(service.getServicePort()));
		}
		
		InetAddress [] addresses = slmServers.toArray(new InetAddress [slmServers.size()]);
		int [] ports = new int[slmPorts.size()];
		int i = 0;
		for (Integer port : slmPorts) {
			ports[i] = port;
			i++;
		}
		setLockServerAddress(addresses);
		setLockServerPort(ports);
		
		
		// --- TODO get the values from Consul
		super.doStartService();
	}
	
	public ConsulConfiguration getConsulConfiguration() {
		return mConsulConfiguration;
	}

	public void setConsulConfiguration(ConsulConfiguration pConsulConfiguration) {
		mConsulConfiguration = pConsulConfiguration;
	}
}
