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
        ArrayList<String> schemaspath = new ArrayList<>();
        ArrayList<String> schematronspath = new ArrayList<>();
        //schematronspath.add("global.sch");
        schematronspath.add("CF_SCRIPT_10_6.sch");
        schematronspath.add("SchematronValidationContext.sch");
        //XMLReport report_error = validate("error.xml", "ALL",schemaspath,schematronspath);
        //XMLReport report_wellformed = validate("wellformed.xml", "ALL",schemaspath,schematronspath);
        XMLReport report_message = validate("Message.xml", "ALL",schemaspath,schematronspath);
        //System.out.println(report_error.toText());
        //System.out.println(report_wellformed.toText());
        //Assert.assertEquals(report_error.countStructureErrors(), 3);
        //Assert.assertEquals(report_wellformed.countStructureErrors(), 0);
        System.out.println(report_message.toText());

    }


    private XMLReport validate(String filepath, String phase,ArrayList<String> schemaspath,ArrayList<String> schematronspath) {
        XMLReport r = null;
        try {
            ArrayList<String> schematrons = new ArrayList<>();
            for(String schematronpath : schematronspath){
                schematrons.add(IOUtils.toString(Thread.currentThread().getContextClassLoader().getResource(schematronpath).openStream()));
            }
            r = Validator.validate(getXMLFileFromResources(filepath), null, schematrons, null, phase);
        } catch (IOException e) {
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
