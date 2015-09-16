package gov.nist.erx.xml;

import gov.nist.validation.report.Report;
import gov.nist.validation.report.impl.EntryImpl;

import javax.xml.validation.Schema;

/**
 * Created by mcl1 on 9/10/15.
 */
public class Validator {

    public static Report validate(String message, Schema schema, String schematron){
        XMLReport report = new XMLReport();
        //TODO get exceptions if there are some and fill the report
        ErxXmlUtils.validateAgainstXSD(message,schema);
        //report.addStructureEntry(new XMLEntry());
        ErxXmlUtils.validateAgainstXSLT(message,schematron);
        return report;
    }
}
