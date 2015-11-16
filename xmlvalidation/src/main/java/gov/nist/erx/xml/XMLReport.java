package gov.nist.erx.xml;

import gov.nist.validation.report.Entry;
import gov.nist.validation.report.Report;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by mcl1 on 9/10/15.
 */
public class XMLReport implements Report{

    List<Entry> structure;
    List<Entry> content;
    List<Entry> vs;

    public XMLReport() {
        this.structure = new ArrayList<>();
        this.content = new ArrayList<>();
        this.vs = new ArrayList<>();
    }

    @Override
    public Map<String, List<Entry>> getEntries() {
        HashMap<String,List<Entry>> map = new HashMap<>();
        map.put("structure", structure);
        map.put("content", content);
        map.put("value-set", vs);
        return map;
    }

    @Override
    public String toJson() throws Exception {
        return gov.nist.validation.report.impl.JsonObjectMapper.mapper.writeValueAsString(this);
    }

    @Override
    public String toText() {
        StringBuilder sb = new StringBuilder();
        if(structure.size()>0) {
            sb.append("\n########  structure check: ");
            sb.append(structure.size());
            sb.append(" problems detected.");
            sb.append(printEntries(structure));
        }
        if(content.size()>0) {
            sb.append("\n########  content check: ");
            sb.append(content.size());
            sb.append(" problems detected.");
            sb.append(printEntries(content));
        }
        if(vs.size()>0) {
            sb.append("\n########  value set check: ");
            sb.append(vs.size());
            sb.append(" problems detected.");
            sb.append(printEntries(vs));
        }
        return sb.toString();
    }

    private String printEntries(List<Entry> list){
        StringBuilder res = new StringBuilder();
        if(list.size()>0) {
            for (Entry e : list) {
                res.append("\n");
                res.append(e.toText());
            }
        }
        return res.toString();
    }

    public void addStructureEntry(Entry e){
        this.structure.add(e);
    }
    public void addContentEntry(Entry e){
        this.content.add(e);
    }
    public void addValueSetEntry(Entry e){
        this.vs.add(e);
    }

    public void addStructureEntries(ArrayList<XMLEntry> entries){
        if(null!=entries) {
            for (Entry e : entries) {
                this.structure.add(e);
            }
        }
    }

    public int countStructureErrors(){
        return this.structure.size();
    }


}
