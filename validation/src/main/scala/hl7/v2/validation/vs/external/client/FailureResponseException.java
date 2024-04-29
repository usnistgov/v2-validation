package hl7.v2.validation.vs.external.client;

public class FailureResponseException extends Exception {
	public FailureResponseException(int statusCode, String message) {
		super("External value set validation responded with a failure status code "+ statusCode +" due to: "+message);
	}
}
