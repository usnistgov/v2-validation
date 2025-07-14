# HL7 Validation Library

### Dependencies

#### xml-util

* __GitHub Repository__ : https://github.com/usnistgov/xml-util
* __Version__ : 2.1.0

#### validation-report

* __GitHub Repository__ : https://github.com/usnistgov/validation-report
* __Version__ : 1.1.1

#### hl7-v2-schemas

* __GitHub Repository__ : https://github.com/usnistgov/hl7-v2-schemas
* __Version__ : 1.6.0

### Build

1) First install Scala Build Tool (sbt) : http://www.scala-sbt.org/
2) Run "sbt" command on project root directory
3) To run tests you can use "test" command
4) To build the tool you can use "compile" command

### Maven
You can use this project as a maven dependency
```xml
<dependency>
    <groupId>gov.nist</groupId>
    <artifactId>hl7-v2-validation</artifactId>
    <version>1.6.5</version>
</dependency>
```
This project is published in https://hit-nexus.nist.gov repository
```xml
<repository>
    <id>hit-nexus</id>
    <url>https://hit-nexus.nist.gov/repository/releases</url>
</repository>
```

### Usage

```java
import hl7.v2.validation.SyncHL7Validator;
import hl7.v2.validation.ValidationContext;
import hl7.v2.validation.ValidationContextBuilder;
import hl7.v2.validation.report.Report;
import org.apache.commons.io.IOUtils;
import java.io.InputStream;
import java.util.Arrays;

public class Main {

    public static void main(String[] args) {
        
        // Get Resource files (Validation XML files) as InputStream
        InputStream profileXML = Main.class.getResourceAsStream("/Profile.xml");
        InputStream valueSetLibraryXML = Main.class.getResourceAsStream("/ValueSets.xml");
        InputStream coConstraintsXML = Main.class.getResourceAsStream("/CoConstraints.xml");
        InputStream constraintsXML = Main.class.getResourceAsStream("/Constraints.xml");
        InputStream valueSetBindingsXML = Main.class.getResourceAsStream("/ValueSetBindings.xml");
        InputStream slicingsXML = Main.class.getResourceAsStream("/Slicings.xml");
        
        // Get HL7 Message 
        String message = IOUtils.toString(Main.class.getResourceAsStream("/message.er7"), StandardCharsets.UTF_8);

        // Create Validation Context object using builder
        ValidationContext context = new ValidationContextBuilder(profileXML)
                .useConformanceContext(Arrays.asList(constraintsXML)) // Optional
                .useValueSetLibrary(valueSetLibraryXML) // Optional
                .useVsBindings(valueSetBindingsXML) // Optional
                .useSlicingContext(slicingsXML) // Optional
                .useCoConstraintsContext(coConstraintsXML) // Optional
                .setFFLegacy0396(true) // Optional (Sets Feature Flag for legacy HL70396 value set values pattern matching behaviour)
                .getValidationContext();

        // Instantiate the validator
        SyncHL7Validator validator = new SyncHL7Validator(context);
        
        // Validate the message
        Report report = validator.check(message, "MESSAGE_PROFILE_ID");
    }
    
}

```

### External Value Set Support

If your profile contains external value sets, the validation engine will need to perform HTTP calls to the value set service URL specified for the value set to check that a code exists in the value set.

An HTTP call for each element in the message with a binding to an external value set will be made when external value set validation is enabled.

#### Default Option

```java
        ValidationContext context = new ValidationContextBuilder(profileXML)
                //...
                .useValueSetLibrary(valueSetLibraryXML); // Optional
                //...
```

Providing a value set library in the validation context as done above will have external value sets validation **DISABLED** by default.
When the external value set validation is disabled and an element in the message with a binding to an external value set was populated, a warning detection will be added to the validation report.

#### Enable External Value Sets Validation

To enable external value sets validation, you must provide an implementation of the `ValueSetFactory` interface, however of most use cases you can use the `DefaultValueSetFactory`

Instead of providing the value set library using `useValueSetLibrary` builder method, you need to use the `useDefaultValueSetFactory` method which takes as parameters:
* The valueSetLibraryXML InputStream
* A pre-configured instance of an HTTP Client which is going to be used to make the external HTTP calls

```java
import hl7.v2.validation.vs.external.client.ExternalValueSetClient;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustAllStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import hl7.v2.validation.SyncHL7Validator;
import hl7.v2.validation.ValidationContext;
import hl7.v2.validation.ValidationContextBuilder;
import hl7.v2.validation.report.Report;
import org.apache.commons.io.IOUtils;
import java.io.InputStream;
import java.util.Arrays;

public class Main {
	public static void main(String[] args) throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
		SSLContextBuilder builder = new SSLContextBuilder();

		builder.loadTrustMaterial(null, new TrustAllStrategy());
		SSLConnectionSocketFactory socketFactory = new SSLConnectionSocketFactory(
				builder.build(),
				NoopHostnameVerifier.INSTANCE
		);

		RequestConfig requestConfig = RequestConfig
				.custom()
				.setConnectionRequestTimeout(2 * 1000)
				.setConnectTimeout(2 * 1000)
				.setSocketTimeout(2 * 1000).build();

		// Create a pre-configured instance of CloseableHttpClient
		CloseableHttpClient httpClient = HttpClients.custom()
		                                        .setDefaultRequestConfig(requestConfig)
		                                        .disableCookieManagement()
		                                        .setSSLSocketFactory(socketFactory)
		                                        .addInterceptorFirst((HttpRequestInterceptor) (request, context) -> {
			                                        // You can get the binding identifier of the value set being validated against by this call
			                                        String bindingIdentifier = context.getAttribute(ExternalValueSetClient.HTTP_CONTEXT_VS_BINDING_IDENTIFIER).toString();
			                                        // You can get the URL of the value set being validated against by this call
			                                        String valueSetURL = context.getAttribute(ExternalValueSetClient.HTTP_CONTEXT_VS_URL).toString();
			                                        // You can modify the request as necessary to add extra headers such as API keys ("X-API-KEY" header)
		                                        }).build();

		// Get Resource files (Validation XML files) as InputStream
		InputStream profileXML = Main.class.getResourceAsStream("/Profile.xml");
		InputStream valueSetLibraryXML = Main.class.getResourceAsStream("/ValueSets.xml");
		InputStream coConstraintsXML = Main.class.getResourceAsStream("/CoConstraints.xml");
		InputStream constraintsXML = Main.class.getResourceAsStream("/Constraints.xml");
		InputStream valueSetBindingsXML = Main.class.getResourceAsStream("/ValueSetBindings.xml");
		InputStream slicingsXML = Main.class.getResourceAsStream("/Slicings.xml");

		// Get HL7 Message 
		String message = IOUtils.toString(Main.class.getResourceAsStream("/message.er7"), StandardCharsets.UTF_8);

		// Create Validation Context object using builder
		ValidationContext context = new ValidationContextBuilder(profileXML)
				.useConformanceContext(Arrays.asList(constraintsXML)) // Optional
                
                // Use Default ValueSetFactory with value set library XML and HTTP Client
				.useDefaultValueSetFactory(valueSetLibraryXML, httpClient)
                
				.useVsBindings(valueSetBindingsXML) // Optional
				.useSlicingContext(slicingsXML) // Optional
				.useCoConstraintsContext(coConstraintsXML) // Optional
				.setFFLegacy0396(true) // Optional (Sets Feature Flag for legacy HL70396 value set values pattern matching behaviour)
				.getValidationContext();

		// Instantiate the validator
		SyncHL7Validator validator = new SyncHL7Validator(context);

		// Validate the message
		Report report = validator.check(message, "MESSAGE_PROFILE_ID");
	}
}
```