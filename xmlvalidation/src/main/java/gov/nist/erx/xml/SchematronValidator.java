package gov.nist.erx.xml;

import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.w3c.dom.Node;

import javax.xml.transform.*;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;

/**
 * Created by mcl1 on 11/16/15.
 */
public class SchematronValidator {


    private static final String XML_HEADER = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
    private static final String WARNING = "Warning";
    private static final String ERROR = "Error";
    private static final String ALL = "ALL";


    public static ArrayList<XMLEntry> validateAgainstSchematron(String xmlContent,
                                               InputStream schematronInput, InputStream skeletonInput, String phase) throws XmlException {
        StringBuilder sbresult = new StringBuilder();
        XmlObject xml = XmlObject.Factory.parse(xmlContent);
        StreamSource schematron = new StreamSource(schematronInput);
        StreamSource skeleton = new StreamSource(skeletonInput);
        Node schematronTransform = doTransform(schematron, skeleton);
        String validationResult = doTransform(xml.getDomNode(), schematronTransform);
        return parseResult(validationResult,phase);
    }

    private static ArrayList<XMLEntry> parseResult(String result, String phase) {
        ArrayList<XMLEntry> entries = new ArrayList<>();
        try {
            ArrayList<String> lines = new ArrayList<>();
            result = result.substring(XML_HEADER.length());
            String[] items = result.split("((?<=(" + WARNING.toUpperCase() + "|" + ERROR.toUpperCase() + "))|(?=(" + WARNING.toUpperCase() + "|" + ERROR.toUpperCase() + ")))");
            if (items.length > 1) {
                for (int i = 0; i < items.length; i+=2) {
                    if ("".equals(items[i])) {
                        i++;
                    }
                    entries = parseEntry(entries,items[i],items[i+1],phase);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return entries;
    }

    private static ArrayList<XMLEntry> parseEntry(ArrayList<XMLEntry> entries, String classification, String message, String phase){
        if(message.length()>2){
            message = message.substring(2);
        }
        if (classification.equals(WARNING.toUpperCase())) {
            if (ALL.equals(phase) || WARNING.equals(phase)) {
                entries.add(XMLDetections.contentWarning(message));
            }
        } else if (classification.equals(ERROR.toUpperCase())) {
            if (ALL.equals(phase) || ERROR.equals(phase)) {
                entries.add(XMLDetections.contentError(message));
            }
        }
        return entries;
    }

    private static Node doTransform(StreamSource xmlSource, StreamSource xsltSource) {
        System.setProperty("javax.xml.transform.TransformerFactory",
                "net.sf.saxon.TransformerFactoryImpl");
        DOMResult result = new DOMResult();
        try {
            TransformerFactory tFactory = TransformerFactory.newInstance();
            URIResolver resolver = new SkeletonURIResolver();
            tFactory.setURIResolver(resolver);
            Transformer transformer = tFactory.newTransformer(xsltSource);
            transformer.transform(xmlSource, result);
        } catch (TransformerConfigurationException tce) {
            tce.printStackTrace();
            return null;
        } catch (TransformerException te) {
            te.printStackTrace();
            return null;
        } finally {
            System.clearProperty("javax.xml.transform.TransformerFactory");
        }
        return result.getNode();
    }

    private static String doTransform(Node originalXml, Node transform) {
        System.setProperty("javax.xml.transform.TransformerFactory",
                "net.sf.saxon.TransformerFactoryImpl");
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        StreamResult result = new StreamResult(os);
        try {
            Source xmlSource = new DOMSource(originalXml);
            Source xsltSource = new DOMSource(transform);
            Transformer transformer = TransformerFactory.newInstance().newTransformer(
                    xsltSource);
            transformer.transform(xmlSource, result);
        } catch (TransformerConfigurationException tce) {
            tce.printStackTrace();
            return null;
        } catch (TransformerException te) {
            te.printStackTrace();
            return null;
        } finally {
            System.clearProperty("javax.xml.transform.TransformerFactory");
        }
        return os.toString();
    }

}
