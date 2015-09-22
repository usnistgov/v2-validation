package gov.nist.erx.xml;

import hl7.v2.instance.Location;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import sun.security.krb5.internal.crypto.EType;

/**
 * Created by mcl1 on 9/22/15.
 */
public class XSDValidationErrorHandler implements ErrorHandler {


    private XMLReport xmlReport;
    private XMLFile xmlFile;

    public XSDValidationErrorHandler(XMLFile xmlFile) {
        this.xmlReport = new XMLReport();
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
        xmlReport.addStructureEntry(new XMLEntry(ex.getLineNumber(),ex.getColumnNumber(),this.xmlFile.getPath(),ex.getMessage(),"category",classification));
    }

    public XMLReport getXmlReport() {
        return xmlReport;
    }

}
