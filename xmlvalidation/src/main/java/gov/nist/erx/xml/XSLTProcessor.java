package gov.nist.erx.xml;

import org.apache.commons.io.FileUtils;
import org.apache.xalan.xslt.Process;

import javax.xml.transform.dom.DOMResult;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by mcl1 on 9/25/15.
 */
public class XSLTProcessor {

    private static final String WARNING = "WARNING";
    private static final String ERROR = "ERROR";
    private static final String ALL = "ALL";
    private static int file_num = 0;
    private static final String XML_HEADER = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";

    public static ArrayList<XMLEntry> process(String schematron, String skeleton, String xmlFile, String phase) {
        return parseResult(processSecondStep(processFirstStep(schematron, skeleton), xmlFile), phase);
    }

    private static String processFirstStep(String schematron, String skeleton) {
        System.setProperty("javax.xml.transform.TransformerFactory",
                "net.sf.saxon.TransformerFactoryImpl");
        DOMResult result = new DOMResult();
        String schematronFile = writeFile(schematron);
        String skeletonFile = writeFile(skeleton);
        file_num++;
        String resultFile = "Validator" + file_num + ".xsl";
        xalan_process(schematronFile, skeletonFile, resultFile);
        return resultFile;
    }

    private static String processSecondStep(String firstStepResult, String xml) {
        String xmlFile = writeFile(xml);
        file_num++;
        String resultFile = "Result" + file_num + ".txt";
        xalan_process(xmlFile, firstStepResult, resultFile);
        return resultFile;
    }

    private static ArrayList<XMLEntry> parseResult(String result, String phase) {
        ArrayList<XMLEntry> entries = new ArrayList<>();
        try {
            String report = FileUtils.readFileToString(new File(result));
            ArrayList<String> lines = new ArrayList<>();
            report = report.substring(XML_HEADER.length());
            String[] items = report.split("((?<=(" + WARNING + "|" + ERROR + "))|(?=(" + WARNING + "|" + ERROR + ")))");
            System.out.println(report);
            System.out.println(items);
            for (int i = 1; i < items.length; i += 2) {
                if (items[i].equals(WARNING)) {
                    if (ALL.equals(phase) || WARNING.equals(phase)) {
                        entries.add(XMLDetections.contentWarning(items[i + 1]));
                    }
                } else if (items[i].equals(ERROR)) {
                    if (ALL.equals(phase) || ERROR.equals(phase)) {
                        entries.add(XMLDetections.contentError(items[i + 1]));
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return entries;
    }

    private static String writeFile(String content) {
        file_num++;
        String filename = "tmp_file_" + file_num;
        File f = new File(filename);
        try {
            FileUtils.writeStringToFile(f, content);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return filename;
    }

    private static void xalan_process(String in, String xsl, String out) {
        String[] args = {"-IN", in, "-XSL", xsl, "-OUT", out};
        Process.main(args);
    }
}
