package hl7.v2.validation.vs.factory.impl.java;

import hl7.v2.validation.vs.Code;
import hl7.v2.validation.vs.ExternalValueSet;
import hl7.v2.validation.vs.ValueSet;
import hl7.v2.validation.vs.external.client.*;
import hl7.v2.validation.vs.factory.ValueSetFacade;
import org.apache.http.impl.client.CloseableHttpClient;

import java.util.List;

public class ExternalValueSetImpl extends ValueSetFacade {

	ExternalValueSet valueSet;
	CloseableHttpClient client;
	boolean enableHttp;

	public ExternalValueSetImpl(ExternalValueSet value, CloseableHttpClient client, boolean enableHttp) {
		this.valueSet = value;
		this.client = client;
		this.enableHttp = enableHttp;
	}

	@Override
	public ValueSet getValueSet() {
		return valueSet;
	}

	@Override
	public List<Code> getCodeMatches(String value) throws ExternalCodeValidationException {
		try {
			if(enableHttp && this.client != null) {
				return ExternalValueSetClient.getCodeMatches(client, valueSet.id(), valueSet.url(), value);
			} else {
				throw new ValueSetValidationHttpClientDisableException();
			}
		} catch(Exception e) {
			throw new ExternalCodeValidationException(e.getMessage(), this.valueSet, e);
		}
	}

	@Override
	public boolean contains(String value) throws ExternalCodeValidationException {
		return getCodeMatches(value).size() > 0;
	}

	@Override
	public List<Code> getCodeList() throws ExternalCodeValidationException {
		try {
			if(enableHttp && this.client != null) {
				return ExternalValueSetClient.getCodes(client, valueSet.id(), valueSet.url());
			} else {
				throw new ValueSetValidationHttpClientDisableException();
			}
		} catch(Exception e) {
			throw new ExternalCodeValidationException(e.getMessage(), this.valueSet, e);
		}
	}
}
