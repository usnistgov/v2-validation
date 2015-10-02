package gov.nist.erx.xml;

import org.xml.sax.SAXException;

import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;

/**
 * Created by mcl1 on 9/15/15.
 */
public class ErxXmlUtils {

    public static ArrayList<XMLEntry> validateAgainstXSD(String xmlFile, Schema schema) {
        StringReader reader = new StringReader(xmlFile);
        javax.xml.validation.Validator validator = schema.newValidator();
        XSDValidationErrorHandler errorHandler = new XSDValidationErrorHandler(xmlFile);
        validator.setErrorHandler(errorHandler);
        try {
            validator.validate(new StreamSource(reader));
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return errorHandler.getXmlEntries();
    }

    public static ArrayList<XMLEntry> validateAgainstXSLT(String xml, String schematronSchema, String skeleton, String phase) {
        //For more information, see http://www.xfront.com/schematron/dynamic.html
        ArrayList<XMLEntry> xmlEntries = XSLTProcessor.process(schematronSchema, skeleton, xml, phase);
        return xmlEntries;
    }

}