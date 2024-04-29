package hl7.v2.validation.vs.external.client;

import org.apache.http.entity.ContentType;

public class InvalidContentTypeException extends Exception {
	public InvalidContentTypeException(ContentType contentType) {
		super("External value set validation responded with an invalid content type '"+ contentType.getMimeType()+"'. The expected content type is '"+ ContentType.APPLICATION_JSON.getMimeType()+"'");
	}
}
