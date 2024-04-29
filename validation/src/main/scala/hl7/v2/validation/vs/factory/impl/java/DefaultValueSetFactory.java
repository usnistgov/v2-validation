package hl7.v2.validation.vs.factory.impl.java;

import hl7.v2.validation.vs.*;
import hl7.v2.validation.vs.factory.ValueSetFacade;
import hl7.v2.validation.vs.factory.ValueSetFactory;
import org.apache.http.impl.client.CloseableHttpClient;

import java.util.HashMap;
import java.util.Map;

public class DefaultValueSetFactory extends ValueSetFactory {

	Map<String, ValueSetFacade> instanceCache = new HashMap<>();
	ValueSetLibrary valueSetLibrary;
	CloseableHttpClient httpClient;
	DefaultValueSetFactoryConfiguration configuration;

	public DefaultValueSetFactory(CloseableHttpClient httpClient, ValueSetLibrary vsLibrary, DefaultValueSetFactoryConfiguration configuration) {
		super();
		this.valueSetLibrary = vsLibrary;
		this.configuration = configuration;
		this.httpClient = httpClient;
	}

	public DefaultValueSetFactory(
			ValueSetLibrary valueSetLibrary,
			DefaultValueSetFactoryConfiguration configuration
	) {
		this.valueSetLibrary = valueSetLibrary;
		this.httpClient = null;
		this.configuration = configuration;
	}

	@Override
	public boolean isExcludedFromValidation(String vsId) {
		return valueSetLibrary.isExcludedFromTheValidation(vsId);
	}

	@Override
	public ValueSetFacade getValueSetById(String id) throws Exception {
		ValueSet valueSet = this.valueSetLibrary.get(id);
		if(valueSet instanceof InternalValueSet) {
			return getInternalValueSet((InternalValueSet) valueSet);
		}
		if(valueSet instanceof ExternalValueSet) {
			return getExternalValueSet((ExternalValueSet) valueSet);
		}
		throw new Exception("Unknown value set type for binding '"+ valueSet.id()+"'");
	}

	public ValueSetFacade getExternalValueSet(ExternalValueSet metadata) {
		if(this.configuration.cacheInstances && this.instanceCache.containsKey(metadata.id())) {
			return this.instanceCache.get(metadata.id());
		}
		ValueSetFacade instance = new ExternalValueSetImpl(metadata, httpClient, configuration.enableExternalHttpRequests);
		if(this.configuration.cacheInstances) {
			this.instanceCache.put(metadata.id(), instance);
		}
		return instance;
	}

	public ValueSetFacade getInternalValueSet(InternalValueSet metadata) {
		if(this.configuration.cacheInstances && this.instanceCache.containsKey(metadata.id())) {
			return this.instanceCache.get(metadata.id());
		}
		ValueSetFacade instance = new InternalValueSetImpl(metadata);
		if(this.configuration.cacheInstances) {
			this.instanceCache.put(metadata.id(), instance);
		}
		return instance;
	}

	@Override
	public Boolean isExcludedFromTheValidation(String id) {
		return valueSetLibrary.isExcludedFromTheValidation(id);
	}

	@Override
	public ValueSet get(String id) throws ValueSetNotFoundException {
		return valueSetLibrary.get(id);
	}

	@Override
	public boolean containsLegacy0396Codes() {
		return valueSetLibrary.containsLegacy0396Codes();
	}
}