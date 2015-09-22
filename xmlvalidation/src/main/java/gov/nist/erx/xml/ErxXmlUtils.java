package gov.nist.erx.xml;

import com.helger.commons.state.EValidity;
import com.helger.schematron.ISchematronResource;
import com.helger.schematron.pure.SchematronResourcePure;
import org.xml.sax.SAXException;

import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.Charset;

/**
 * Created by mcl1 on 9/15/15.
 */
public class ErxXmlUtils {

    public static XMLReport validateAgainstXSD(XMLFile xmlFile, Schema schema)
    {
        String result = "";
        /*URL schemaURL = Thread.currentThread().getContextClassLoader().getResource(fullPathToXsd);
        SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        Schema schema = schemaFactory.newSchema(schemaURL);*/
        StringReader reader = new StringReader(xmlFile.getContent());
        javax.xml.validation.Validator validator = schema.newValidator();
        XSDValidationErrorHandler errorHandler = new XSDValidationErrorHandler(xmlFile);
        validator.setErrorHandler(errorHandler);
        try {
            validator.validate(new StreamSource(reader,result));
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return errorHandler.getXmlReport();
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
