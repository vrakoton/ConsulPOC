package com.poc.consul.test;

import com.poc.consul.annotation.DynamicConfig;

import atg.adapter.gsa.GSARepository;
import atg.nucleus.GenericService;

public class TestService extends GenericService {
	String mHostName;
	int mPort;
	float mPrice;
	GSARepository mRepository;
	
	public void reset() {
		setHostName(null);
		setRepository(null);
	}

	@DynamicConfig(tokenName = "token.repository")
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

	@DynamicConfig(tokenName = "token.port")
	public int getPort() {
		return mPort;
	}
	public void setPort(int pPort) {
		mPort = pPort;
	}

	@DynamicConfig(tokenName = "token.price")
	public float getPrice() {
		return mPrice;
	}
	public void setPrice(float pPrice) {
		mPrice = pPrice;
	}
	
}
