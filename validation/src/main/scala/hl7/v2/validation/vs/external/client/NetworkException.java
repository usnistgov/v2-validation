package hl7.v2.validation.vs.external.client;

public class NetworkException extends Exception {
	public NetworkException(String message, Exception reason) {
		super("External value set validation could not be performed due to network error: "+ message, reason);
	}
}
