package hl7.v2.validation.vs.factory.impl.java;

public class DefaultValueSetFactoryConfiguration {
	boolean cacheInstances;
	boolean enableExternalHttpRequests;

	public DefaultValueSetFactoryConfiguration(boolean cacheInstances, boolean enableExternalHttpRequests) {
		this.cacheInstances = cacheInstances;
		this.enableExternalHttpRequests = enableExternalHttpRequests;
	}

	public DefaultValueSetFactoryConfiguration() {
	}

	public boolean isCacheInstances() {
		return cacheInstances;
	}

	public void setCacheInstances(boolean cacheInstances) {
		this.cacheInstances = cacheInstances;
	}

	public boolean isEnableExternalHttpRequests() {
		return enableExternalHttpRequests;
	}

	public void setEnableExternalHttpRequests(boolean enableExternalHttpRequests) {
		this.enableExternalHttpRequests = enableExternalHttpRequests;
	}
}
