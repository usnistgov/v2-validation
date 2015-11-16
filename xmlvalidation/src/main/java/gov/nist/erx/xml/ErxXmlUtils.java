package gov.nist.erx.xml;

import org.apache.commons.io.IOUtils;
import org.apache.xmlbeans.XmlException;
import org.xml.sax.SAXException;

import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

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

    public static ArrayList<XMLEntry> validateAgainstXSLT(String xml, List<String> schematronSchema, String skeleton, String phase) {
        //For more information, see http://www.xfront.com/schematron/dynamic.html
        ArrayList<ArrayList<XMLEntry>> entries = new ArrayList<>();
        for(String schematron : schematronSchema) {
            //entries.add(XSLTProcessor.process(schematron, skeleton, xml, phase));
            try {
                entries.add(SchematronValidator.validateAgainstSchematron(xml, IOUtils.toInputStream(schematron), IOUtils.toInputStream(skeleton), phase));
            } catch (XmlException e) {
                e.printStackTrace();
            }
        }
        return flatten(entries);
    }

    private static ArrayList<XMLEntry> flatten(ArrayList<ArrayList<XMLEntry>> entries) {
        ArrayList<XMLEntry> flat = new ArrayList<>();
        for(ArrayList<XMLEntry> xmlEntriesList : entries){
            for(XMLEntry entry : xmlEntriesList){
                flat.add(entry);
            }
        }
        return flat;
    }

}