package gov.nist.erx.xml;

import com.fasterxml.jackson.core.JsonProcessingException;
import gov.nist.validation.report.Entry;
import gov.nist.validation.report.Trace;
import gov.nist.validation.report.impl.JsonObjectMapper;
import hl7.v2.instance.Location;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by mcl1 on 9/16/15.
 */
public class XMLEntry implements Entry {

    private int line;
    private int column;
    private String path;
    private String description;
    private String category;
    private String classification;
    private List<Trace> stackTrace;
    private Map<String, Object> metaData;

    public XMLEntry(int line, int column, String path, String description, String category, String classification) {
        this.line = -1;
        this.column = -1;
        this.line = line;
        this.column = column;
        this.path = path;
        this.description = description;
        this.category = category;
        this.classification = classification;
    }
    public XMLEntry(int line, int column, String path, String description, String category, String classification, List<Trace> stackTrace, Map<String, Object> metaData) {
        this(line, column, path, description, category, classification);
        this.stackTrace = stackTrace;
        this.metaData = metaData;
    }

    public int getLine() {
        return this.line;
    }

    public int getColumn() {
        return this.column;
    }

    public String getPath() {
        return this.path;
    }

    public String getDescription() {
        return this.description;
    }

    public String getCategory() {
        return this.category;
    }

    public String getClassification() {
        return this.classification;
    }

    public List<Trace> getStackTrace() {
        return this.stackTrace;
    }

    public Map<String, Object> getMetaData() {
        return this.metaData;
    }

    public String toJson() throws JsonProcessingException {
        return JsonObjectMapper.mapper.writeValueAsString(this);
    }

    public String toText() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("[%s][%d,%d] %s # %s", new Object[]{this.classification, Integer.valueOf(this.line), Integer.valueOf(this.column), this.category, this.description}));
        if(this.stackTrace != null) {
            Iterator i$ = this.stackTrace.iterator();

            while(i$.hasNext()) {
                Trace t = (Trace)i$.next();
                sb.append("\n").append(t.toString());
            }
        }

        return sb.toString();
    }

    public boolean equals(Object o) {
        if(this == o) {
            return true;
        } else if(!(o instanceof XMLEntry)) {
            return false;
        } else {
            XMLEntry entry = (XMLEntry)o;
            return this.column != entry.column?false:(this.line != entry.line?false:(!this.category.equals(entry.category)?false:(!this.classification.equals(entry.classification)?false:(!this.description.equals(entry.description)?false:this.path.equals(entry.path)))));
        }
    }

    public int hashCode() {
        int result = this.line;
        result = 31 * result + this.column;
        result = 31 * result + this.path.hashCode();
        result = 31 * result + this.description.hashCode();
        result = 31 * result + this.category.hashCode();
        result = 31 * result + this.classification.hashCode();
        return result;
    }

    public String toString() {
        return this.toText();
    }
}
