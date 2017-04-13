package hl7.v2.validation.vs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import gov.nist.validation.report.Entry;
import gov.nist.validation.report.Trace;
import hl7.v2.validation.report.Detections;

public class TripletEntry implements Entry {
	private Entry value;
	private Entry codeSys;
	
	public boolean passValue(){
		return CodedElementValidator.pass(value);
	}
	
	public boolean passCodeSys(){
		return codeSys == null;
	}
	
	public boolean isValid(){
		return this.passValue() && this.passCodeSys();
	}
	
	public List<Entry> asList(){
		List<Entry> ls = new ArrayList<>();
		if(value != null){
			ls.add(value);
		}
		if(codeSys != null){
			ls.add(codeSys);
		}
		return ls;
	}
	
	public TripletEntry toAlert(){
		TripletEntry al = new TripletEntry();
		if(value != null){
			al.setValue(Detections.toAlert(value));
		}
		if(codeSys != null){
			al.setCodeSys(Detections.toAlert(codeSys));
		}
		return al;
	}
	
	public Entry getValue() {
		return value;
	}
	public void setValue(Entry value) {
		this.value = value;
	}
	public Entry getCodeSys() {
		return codeSys;
	}
	public void setCodeSys(Entry codeSys) {
		this.codeSys = codeSys;
	}

	public String toString(){
		return "VAL : "+value+"\n"+"CODESYS : "+codeSys;
	}
	@Override
	public String getCategory() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getClassification() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getColumn() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getDescription() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getLine() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Map<String, Object> getMetaData() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getPath() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Trace> getStackTrace() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String toJson() throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String toText() {
		// TODO Auto-generated method stub
		return null;
	}
	
	
	
	
}
