import gov.nist.erx.xml.Validator;
import gov.nist.erx.xml.XMLReport;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by mcl1 on 9/18/15.
 */
public class ValidatorTest {

    @Test
    public void testValidator() {
        //TODO tests
        XMLReport report_error = validate("error.xml", "ALL");
        XMLReport report_wellformed = validate("wellformed.xml", "ALL");
        System.out.println(report_error.toText());
        System.out.println(report_wellformed.toText());
        Assert.assertEquals(report_error.countStructureErrors(), 3);
        Assert.assertEquals(report_wellformed.countStructureErrors(), 0);

    }


    private XMLReport validate(String filepath, String phase) {
        XMLReport r = null;
        try {
            URL schemaURL = Thread.currentThread().getContextClassLoader().getResource("xsd/SCRIPT_XML_10_6-20121015.xsd");
            SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            Schema schema = schemaFactory.newSchema(schemaURL);
            InputStream isSchematron1 = Thread.currentThread().getContextClassLoader().getResource("xslt/TC_Tc_3.0_1.1_SchematronValidationContext.sch").openStream();
            InputStream isSchematron2 = Thread.currentThread().getContextClassLoader().getResource("xslt/TC_Tc_3.0_1.1_SchematronValidationContext2.sch").openStream();
            InputStream isSkeleton = Thread.currentThread().getContextClassLoader().getResource("xslt/skeleton1-5.xsl").openStream();
            ArrayList<String> schematrons = new ArrayList<>();
            schematrons.add(IOUtils.toString(isSchematron1));
            schematrons.add(IOUtils.toString(isSchematron2));
            String skeleton = IOUtils.toString(isSkeleton);
            isSkeleton.close();
            isSchematron1.close();
            isSchematron2.close();
            r = Validator.validate(getXMLFileFromResources(filepath), schema, schematrons, skeleton, phase);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        }
        return r;
    }

    private String getXMLFileFromResources(String filepath) throws java.io.IOException {
        InputStream xml = readfile(filepath);
        String xmlContent = IOUtils.toString(xml, "UTF-8");
        xml.close();
        return xmlContent;
    }

    private InputStream readfile(String path) {
        InputStream is = null;
        try {
            //is = new FileInputStream(getFileFromResources(path));
            is = new FileInputStream(this.getClass().getClassLoader().getResource(path).getFile());
            //is.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return is;
    }
}
