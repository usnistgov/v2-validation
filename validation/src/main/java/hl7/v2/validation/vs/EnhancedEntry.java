package hl7.v2.validation.vs;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import gov.nist.validation.report.Entry;
import gov.nist.validation.report.Trace;

public class EnhancedEntry implements Entry {

	private Entry e;
	private boolean ok;
	
	public EnhancedEntry(Entry e, boolean p){
		this.e = e;
		this.setOk(p);
	}
	
	
	@Override
	public String getCategory() {
		// TODO Auto-generated method stub
		return e.getCategory();
	}
	
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return e.toString();
	}

	@Override
	public String getClassification() {
		// TODO Auto-generated method stub
		return e.getClassification();
	}

	@Override
	public int getColumn() {
		// TODO Auto-generated method stub
		return e.getColumn();
	}

	@Override
	public String getDescription() {
		// TODO Auto-generated method stub
		return e.getDescription();
	}

	@Override
	public int getLine() {
		// TODO Auto-generated method stub
		return e.getLine();
	}

	@Override
	public Map<String, Object> getMetaData() {
		// TODO Auto-generated method stub
		return e.getMetaData();
	}

	@Override
	public String getPath() {
		// TODO Auto-generated method stub
		return e.getPath();
	}

	@Override
	public List<Trace> getStackTrace() {
		// TODO Auto-generated method stub
		return e.getStackTrace();
	}

	@Override
	public String toJson() throws Exception {
		// TODO Auto-generated method stub
		return e.toJson();
	}

	@Override
	public String toText() {
		// TODO Auto-generated method stub
		return e.toText();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		EnhancedEntry that = (EnhancedEntry) o;
		return ok == that.ok &&
				e.equals(that.e);
	}

	@Override
	public int hashCode() {
		return Objects.hash(e, ok);
	}

	public boolean isOk() {
		return ok;
	}

	public void setOk(boolean ok) {
		this.ok = ok;
	}

}
