package gov.nist.erx.xml;

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import java.util.ArrayList;

/**
 * Created by mcl1 on 9/22/15.
 */
public class XSDValidationErrorHandler implements ErrorHandler {


    private ArrayList<XMLEntry> xmlEntries;
    private String xmlFile;

    public XSDValidationErrorHandler(String xmlFile) {
        this.xmlEntries = new ArrayList<XMLEntry>();
        this.xmlFile = xmlFile;
    }

    @Override
    public void warning(SAXParseException exception) throws SAXException {
        this.addEntry("Warning", exception);
    }

    @Override
    public void error(SAXParseException exception) throws SAXException {
        this.addEntry("Error",exception);
    }

    @Override
    public void fatalError(SAXParseException exception) throws SAXException {
        this.addEntry("Error",exception);
    }

    private void addEntry(String classification,SAXParseException ex){
        //TODO Use XMLDetections and find the PATH
        xmlEntries.add(new XMLEntry(ex.getLineNumber(), ex.getColumnNumber(), "", ex.getMessage(), "category", classification));
    }

    public ArrayList<XMLEntry> getXmlEntries() {
        return xmlEntries;
    }

}
