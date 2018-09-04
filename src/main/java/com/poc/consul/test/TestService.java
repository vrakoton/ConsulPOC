package com.poc.consul.test;

import com.poc.consul.annotation.DynamicConfig;

import atg.adapter.gsa.GSARepository;
import atg.nucleus.GenericService;

public class TestService extends GenericService {
	String mHostName;
	int mPort;
	GSARepository mRepository;
	
	public void reset() {
		setHostName(null);
		setRepository(null);
	}

	
	public GSARepository getRepository() {
		return mRepository;
	}
	public void setRepository(GSARepository pRepository) {
		mRepository = pRepository;
	}
	
	@DynamicConfig(tokenName = "token.hostname")
	public String getHostName() {
		return mHostName;
	}
	
	public void setHostName(String pHostName) {
		mHostName = pHostName;
	}
	
}
