package gov.nist.erx.xml;

import gov.nist.validation.report.Entry;
import gov.nist.validation.report.Trace;
import gov.nist.validation.report.impl.EntryImpl;
import hl7.v2.instance.Location;
import hl7.v2.validation.report.Detections;

import java.util.List;
import java.util.Map;

/**
 * Created by mcl1 on 9/16/15.
 */
public class XMLDetections extends Detections{

    //int line, int column, String path, String description, String category, String classification
    public static Entry contentError(Location l,String msg){
        return new XMLEntry(l.line(),l.column(),l.path(),l.desc(),"","");
    }

    //Helpers (see hl7.v2.validation.report.Detections)

    private static Entry entry(
            int line,
            int column,
            String path,
            String description,
            String category,
            String classification,
            List<Trace> stackTrace,
            Map<String, Object> metaData
    ) {
        return new XMLEntry(line, column, path, description, category,
                classification, stackTrace, metaData);
    }

    private static Entry entry(
            int line,
            int column,
            String path,
            String description,
            String category,
            String classification
    ) {
        return entry(line, column, path, description, category,
                classification, null, null);
    }

    private static Entry entry(Location l, String description, String category,
                               String classification) {
        int line    = l.line();
        int column  = l.column();
        String path = l.uidPath();
        return entry(line, column, path, description, category, classification);
    }

    private static Entry entry(Location l, String description, String category,
                               String classification, List<Trace> stackTrace,
                               Map<String, Object> metaData) {
        int line    = l.line();
        int column  = l.column();
        String path = l.uidPath();
        return entry(line, column, path, description, category,
                classification, stackTrace, metaData);
    }
}
