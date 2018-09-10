package com.poc.consul.configuration;

import atg.nucleus.GenericService;

public class ConsulConfiguration extends GenericService {
	String mConsulHostName = "consul.local.com";
	int mConsulPort = 8500;
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
