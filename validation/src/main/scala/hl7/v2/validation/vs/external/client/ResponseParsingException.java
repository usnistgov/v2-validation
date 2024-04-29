package hl7.v2.validation.vs.external.client;

public class ResponseParsingException extends Exception {
	public ResponseParsingException(Exception reason) {
		super("External value set validation responded with an invalid content format, parsing failed due to: "+ reason.getMessage());
	}
}
