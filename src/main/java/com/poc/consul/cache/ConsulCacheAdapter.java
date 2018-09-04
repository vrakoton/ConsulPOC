package com.poc.consul.cache;

import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.kv.model.GetValue;

import atg.core.util.StringUtils;
import atg.nucleus.GenericService;
import atg.nucleus.ServiceException;
import atg.service.cache.CacheAdapter;

/**
 * 
 * @author vrakoton
 * @version 1.0
 * 
 * Description:
 * 
 * This class is used to retrieve the value of a token from Consul. The fetched
 * value is locally cahced in an ATG cache component to avoid repeated calls.
 *
 */
public class ConsulCacheAdapter extends GenericService implements CacheAdapter {
	
	String mConsulUrl;
	String mConsulHostName;
	int mConsulPort;
	ConsulClient mConsulClient;
	
	/**
	 * When starting the Nucleus service, we create a new consul client
	 */
	public void doStartService() throws ServiceException {
		super.doStartService();
		
		String hostname = StringUtils.isBlank(getConsulHostName()) ? "localhost" : getConsulHostName();
		
		vlogDebug("Using hostname {0} to connect to Consul", getConsulHostName());
		mConsulClient = new ConsulClient(hostname);
	}
	
	/**
	 * returns the token value retrieved from consul
	 * @param pTokenName the token name
	 * @return a string value from the Key/value store of Consul
	 * @throws Exception
	 */
	@Override
	public Object getCacheElement(Object pTokenName) throws Exception {
		GetValue rawValue = mConsulClient.getKVValue((String)pTokenName).getValue();
 		Object consulValue = (rawValue == null) ? null : rawValue.getDecodedValue();
		vlogDebug("Found value {0} for token {1}", consulValue, pTokenName);
		if (consulValue == null) {
			vlogWarning("Could not find a value for token {0} in Consul", pTokenName);
		}
		return consulValue;
	}

	@Override
	public int getCacheElementSize(Object arg0, Object arg1) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Object[] getCacheElements(Object[] pKeys) throws Exception {
		if (pKeys == null) return null;
		Object [] res = new Object[ pKeys.length];
		int i = 0;
		for (Object k : pKeys) {
			res[i] = getCacheElement(k);
		}
		return res;
	}

	@Override
	public int getCacheKeySize(Object arg0) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void removeCacheElement(Object arg0, Object arg1) {
		return;
	}

	public String getConsulUrl() {
		return mConsulUrl;
	}

	public void setConsulUrl(String pConsulUrl) {
		mConsulUrl = pConsulUrl;
	}

	public String getConsulHostName() {
		return mConsulHostName;
	}

	public void setConsulHostName(String pConsulHostName) {
		mConsulHostName = pConsulHostName;
	}

	public int getConsulPort() {
		return mConsulPort;
	}

	public void setConsulPort(int pConsulPort) {
		mConsulPort = pConsulPort;
	}

}
