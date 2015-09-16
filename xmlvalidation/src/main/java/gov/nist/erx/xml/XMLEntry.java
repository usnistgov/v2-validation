package gov.nist.erx.xml;

import gov.nist.validation.report.Entry;
import gov.nist.validation.report.Trace;

import java.util.List;
import java.util.Map;

/**
 * Created by mcl1 on 9/16/15.
 */
public class XMLEntry implements Entry {
    @Override
    public int getLine() {
        return 0;
    }

    @Override
    public int getColumn() {
        return 0;
    }

    @Override
    public String getPath() {
        return null;
    }

    @Override
    public String getDescription() {
        return null;
    }

    @Override
    public String getCategory() {
        return null;
    }

    @Override
    public String getClassification() {
        return null;
    }

    @Override
    public List<Trace> getStackTrace() {
        return null;
    }

    @Override
    public Map<String, Object> getMetaData() {
        return null;
    }

    @Override
    public String toJson() throws Exception {
        return null;
    }

    @Override
    public String toText() {
        return null;
    }
}
