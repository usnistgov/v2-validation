package hl7.v2.validation.vs.external.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import hl7.v2.validation.vs.Code;
import hl7.v2.validation.vs.CodeUsage;
import hl7.v2.validation.vs.external.model.CodeSetQueryResult;
import hl7.v2.validation.vs.external.model.ExternalCode;
import hl7.v2.validation.vs.external.model.ResponseMessage;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import scala.Option;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

public class ExternalValueSetClient {
	static final ObjectMapper mapper = new ObjectMapper();
	public static final String HTTP_CONTEXT_VS_URL = "valueSetURL";
	public static final String HTTP_CONTEXT_VS_BINDING_IDENTIFIER = "valueSetBindingIdentifier";

	public static List<Code> getCodes(CloseableHttpClient client, String bindingIdentifier, String valueSetURL) throws InvalidContentTypeException, FailureResponseException, ResponseParsingException, NetworkException {
		return query(client, bindingIdentifier, valueSetURL, null);
	}

	public static List<Code> getCodeMatches(CloseableHttpClient client, String bindingIdentifier, String valueSetURL, String value) throws InvalidContentTypeException, FailureResponseException, NetworkException, ResponseParsingException {
		return query(client, bindingIdentifier, valueSetURL, value);
	}

	public static List<Code> query(CloseableHttpClient client, String bindingIdentifier, String valueSetURL, String value) throws InvalidContentTypeException, FailureResponseException, NetworkException, ResponseParsingException {
		try {
			URIBuilder builder = new URIBuilder(new URI(valueSetURL));
			if(value != null && !value.isEmpty()) {
				builder.setParameter("match", value);
			}
			HttpGet request = new HttpGet(builder.build());
			HttpContext context = new BasicHttpContext();
			context.setAttribute(HTTP_CONTEXT_VS_URL, valueSetURL);
			context.setAttribute(HTTP_CONTEXT_VS_BINDING_IDENTIFIER, bindingIdentifier);
			request.setHeader("Accept", "application/json");
			return run(client, request, context);
		} catch(URISyntaxException e) {
			throw new NetworkException("Unknown host", e);
		}
	}

	public static List<Code> run(CloseableHttpClient client, HttpUriRequest request, HttpContext context) throws NetworkException, InvalidContentTypeException, ResponseParsingException, FailureResponseException {
		try(final CloseableHttpResponse response = client.execute(request, context)) {
			return processResponse(response);
		} catch(UnknownHostException unknownHostException) {
			throw new NetworkException("Unknown host", unknownHostException);
		} catch(IOException e) {
			throw new NetworkException(e.getMessage(), e);
		}
	}

	public static List<Code> processResponse(HttpResponse response) throws InvalidContentTypeException, FailureResponseException, ResponseParsingException {
		if(response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
			final ContentType contentType = ContentType.parse(response.getEntity().getContentType().getValue());
			if(contentType.getMimeType().equals(ContentType.APPLICATION_JSON.getMimeType())) {
				try {
					CodeSetQueryResult result = mapper.readValue(response.getEntity().getContent(), CodeSetQueryResult.class);
					List<Code> codes = new ArrayList<>();
					if(result.getCodes() != null) {
						for(ExternalCode code: result.getCodes()) {
							codes.add(
									new Code(
											code.getValue(),
											code.getDisplayText(),
											CodeUsage.fromString(code.getUsage()),
											code.getCodeSystem(),
											code.isPattern() ? Option.apply(code.getRegularExpression()) : Option.empty()
									)
							);
						}
					}
					return codes;
				} catch(IOException e) {
					throw new ResponseParsingException(e);
				}
			} else {
				throw new InvalidContentTypeException(contentType);
			}
		} else {
			try {
				ResponseMessage errorMessage = mapper.readValue(response.getEntity().getContent(), ResponseMessage.class);
				throw new FailureResponseException(response.getStatusLine().getStatusCode(), errorMessage.getMessage());
			} catch(IOException e) {
				throw new ResponseParsingException(e);
			}
		}
	}
}
