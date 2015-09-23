package gov.nist.erx.xml;

import gov.nist.validation.report.Entry;
import gov.nist.validation.report.Report;
import gov.nist.validation.report.impl.EntryImpl;
import hl7.v2.validation.report.Detections;
import org.xml.sax.SAXParseException;

import javax.xml.validation.Schema;

/**
 * Created by mcl1 on 9/10/15.
 */
public class Validator {

    public static XMLReport validate(XMLFile xmlFile, Schema schema, String schematron){
        //TODO get exceptions if there are some and fill the report
        XMLReport report = ErxXmlUtils.validateAgainstXSD(xmlFile,schema);
        /*if(e instanceof SAXParseException){
            SAXParseException saxError = (SAXParseException) e;
            report.addStructureEntry(new XMLEntry(saxError.getLineNumber(),saxError.getColumnNumber(),xmlFile.getPath(),"description","category","classification"));
        }*/
        //ErxXmlUtils.validateAgainstXSLT(xmlFile.content, schematron);
        return report;
    }

}
