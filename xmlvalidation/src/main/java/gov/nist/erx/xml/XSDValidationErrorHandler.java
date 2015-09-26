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
    private XMLFile xmlFile;

    public XSDValidationErrorHandler(XMLFile xmlFile) {
        this.xmlEntries = new ArrayList<XMLEntry>();
        this.xmlFile = xmlFile;
    }

    @Override
    public void warning(SAXParseException exception) throws SAXException {
        this.addEntry("WARNING", exception);
    }

    @Override
    public void error(SAXParseException exception) throws SAXException {
        this.addEntry("ERROR",exception);
    }

    @Override
    public void fatalError(SAXParseException exception) throws SAXException {
        this.addEntry("FATAL",exception);
    }

    private void addEntry(String classification,SAXParseException ex){
        xmlEntries.add(new XMLEntry(ex.getLineNumber(), ex.getColumnNumber(), this.xmlFile.getPath(), ex.getMessage(), "category", classification));
    }

    public ArrayList<XMLEntry> getXmlEntries() {
        return xmlEntries;
    }

}
