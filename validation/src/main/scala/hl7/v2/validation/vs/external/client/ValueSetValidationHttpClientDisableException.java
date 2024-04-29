package hl7.v2.validation.vs.external.client;

public class ValueSetValidationHttpClientDisableException extends Exception {
	public ValueSetValidationHttpClientDisableException() {
		super("External value set validation was skipped because HTTP requests are disabled.");
	}
}
