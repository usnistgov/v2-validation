package gov.nist.erx.xml;

import com.helger.commons.state.EValidity;
import com.helger.schematron.ISchematronResource;
import com.helger.schematron.pure.SchematronResourcePure;

import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import java.io.StringReader;
import java.nio.charset.Charset;

/**
 * Created by mcl1 on 9/15/15.
 */
public class ErxXmlUtils {

    public static Exception validateAgainstXSD(String xml, Schema schema)
    {
        try
        {
            /*URL schemaURL = Thread.currentThread().getContextClassLoader().getResource(fullPathToXsd);
            SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            Schema schema = schemaFactory.newSchema(schemaURL);*/
            StringReader reader = new StringReader(xml);
            javax.xml.validation.Validator validator = schema.newValidator();
            String result = "";
            validator.validate(new StreamSource(reader,result));
            return null;
        }
        catch(Exception ex)
        {
            return ex;
        }
    }

    public static Exception validateAgainstXSLT(String xml, String schematronSchema) {
        try {
            final ISchematronResource aResPure = SchematronResourcePure.fromString(schematronSchema, Charset.defaultCharset());
            if (!aResPure.isValidSchematron())
                throw new IllegalArgumentException("Invalid Schematron!");
            StringReader reader = new StringReader(xml);
            EValidity ev = aResPure.getSchematronValidity(new StreamSource(reader));
            if (ev.isValid()){
                return null;
            } else {
                throw new Exception(ev.toString());
            }
        } catch(Exception ex){
            return ex;
        }
    }
}
