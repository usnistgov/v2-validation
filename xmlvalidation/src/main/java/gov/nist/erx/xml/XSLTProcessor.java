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

    private static final String WARNING = "Warning";
    private static final String ERROR = "Error";
    private static final String ALL = "ALL";
    private static int file_num = 0;
    private static final String XML_HEADER = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
    private static ArrayList<String> fileNames = new ArrayList<>();

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
        fileNames.add(resultFile);
        xalan_process(schematronFile, skeletonFile, resultFile);
        return resultFile;
    }

    private static String processSecondStep(String firstStepResult, String xml) {
        String xmlFile = writeFile(xml);
        file_num++;
        String resultFile = "Result" + file_num + ".txt";
        fileNames.add(resultFile);
        xalan_process(xmlFile, firstStepResult, resultFile);
        return resultFile;
    }

    private static ArrayList<XMLEntry> parseResult(String result, String phase) {
        ArrayList<XMLEntry> entries = new ArrayList<>();
        try {
            String report = FileUtils.readFileToString(new File(result));
            ArrayList<String> lines = new ArrayList<>();
            report = report.substring(XML_HEADER.length());
            String[] items = report.split("((?<=(" + WARNING.toUpperCase() + "|" + ERROR.toUpperCase() + "))|(?=(" + WARNING.toUpperCase() + "|" + ERROR.toUpperCase() + ")))");
            if (items.length > 1) {
                for (int i = 0; i < items.length; i++) {
                    String message = items[i+1].substring(2);
                    if ("".equals(items[i])) {
                        i++;
                    }
                    if (items[i].equals(WARNING.toUpperCase())) {
                        if (ALL.equals(phase) || WARNING.equals(phase)) {
                            entries.add(XMLDetections.contentWarning(message));
                            i++;
                        }
                    } else if (items[i].equals(ERROR.toUpperCase())) {
                        if (ALL.equals(phase) || ERROR.equals(phase)) {
                            entries.add(XMLDetections.contentError(message));
                            i++;
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            cleanFiles();
        }
        return entries;
    }

    private static void cleanFiles() {
        for(String filename : fileNames){
            FileUtils.deleteQuietly(new File(filename));
        }
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
        fileNames.add(filename);
        return filename;
    }

    private static void xalan_process(String in, String xsl, String out) {
        String[] args = {"-IN", in, "-XSL", xsl, "-OUT", out};
        Process.main(args);
    }
}
