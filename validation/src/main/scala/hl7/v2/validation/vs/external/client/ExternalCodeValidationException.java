package hl7.v2.validation.vs.external.client;

import hl7.v2.validation.vs.ExternalValueSet;

public class ExternalCodeValidationException extends Exception {
	ExternalValueSet valueSet;

	public ExternalCodeValidationException(String message, ExternalValueSet valueSet, Exception reason) {
		super(message, reason);
		this.valueSet = valueSet;
	}

	public ExternalValueSet getValueSet() {
		return valueSet;
	}

	public void setValueSet(ExternalValueSet valueSet) {
		this.valueSet = valueSet;
	}
}
