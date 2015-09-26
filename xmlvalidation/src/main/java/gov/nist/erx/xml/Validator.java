package gov.nist.erx.xml;

import org.apache.commons.io.IOUtils;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by mcl1 on 9/10/15.
 */
public class Validator {

    private static final String XSD_PATH = "SCRIPT_XML_10_6-20121015.xsd";
    private static final String SKELETON_PATH = "skeleton1-5.xsl";

    public static XMLReport validate(XMLFile xmlFile, String schematron, String phase) {
        Schema schema = loadSchema();
        String skeleton = loadSkeleton();
        return validate(xmlFile, schema, schematron, skeleton, phase);
    }

    //TODO Remove schema and schematron args if xsd and xslt are stores in this project's resources
    public static XMLReport validate(XMLFile xmlFile, Schema schema, String schematron, String skeleton, String phase) {
        ArrayList<XMLEntry> xsdEntries = ErxXmlUtils.validateAgainstXSD(xmlFile, schema);
        ArrayList<XMLEntry> xsltEntries = ErxXmlUtils.validateAgainstXSLT(xmlFile.getContent(), schematron, skeleton, phase);
        XMLReport report = new XMLReport();
        report.addStructureEntries(xsdEntries);
        report.addStructureEntries(xsltEntries);
        return report;
    }

    private static String loadSkeleton() {
        try {
            InputStream is = Thread.currentThread().getContextClassLoader().getResource(SKELETON_PATH).openStream();
            return IOUtils.toString(is, "UTF-8");
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            return null;
        }
    }

    private static Schema loadSchema() {
        try {
            URL schemaURL = Thread.currentThread().getContextClassLoader().getResource(XSD_PATH);
            SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            return schemaFactory.newSchema(schemaURL);
        } catch (SAXException e) {
            e.printStackTrace();
            return null;
        }
    }

}
