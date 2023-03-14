# HL7 Validation Library

### Dependencies

#### xml-util

* __GitHub Repository__ : https://github.com/usnistgov/xml-util
* __Version__ : 2.1.0

#### validation-report

* __GitHub Repository__ : https://github.com/usnistgov/validation-report
* __Version__ : 1.1.0

#### hl7-v2-schemas

* __GitHub Repository__ : https://github.com/usnistgov/hl7-v2-schemas
* __Version__ : 1.6.0

### Build

1) First install Scala Build Tool (sbt) : http://www.scala-sbt.org/
2) Run "sbt" command on project root directory
3) To run tests you can use "test" command
4) To build the tool you can use "compile" command

### Usage

```java
import hl7.v2.validation.SyncHL7Validator;
import hl7.v2.validation.ValidationContext;
import hl7.v2.validation.ValidationContextBuilder;
import hl7.v2.validation.report.Report;

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
        InputStream message = Main.class.getResourceAsStream("/message.er7");

        // Create Validation Context object using builder
        ValidationContext context = new ValidationContextBuilder(profileXML)
                .useConformanceContext(Arrays.asList(constraintsXML)) // Optional
                .useValueSetLibrary(valueSetLibraryXML) // Optional
                .useVsBindings(valueSetBindingsXML) // Optional
                .useSlicingContext(slicingsXML) // Optional
                .useCoConstraintsContext(coConstraintsXML) // Optional
                .getValidationContext();

        // Instantiate the validator
        SyncHL7Validator validator = new SyncHL7Validator(context);
        
        // Validate the message
        Report report = validator.check(message, "MESSAGE_PROFILE_ID");
    }
    
}

```