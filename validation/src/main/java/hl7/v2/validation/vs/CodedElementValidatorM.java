package hl7.v2.validation.vs;

import gov.nist.validation.report.Entry;
import gov.nist.validation.report.Trace;
import hl7.v2.instance.Complex;
import hl7.v2.instance.Simple;
import hl7.v2.profile.BindingLocation;
import hl7.v2.profile.ValueSetSpec;
import hl7.v2.validation.report.ConfigurableDetections;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CodedElementValidatorM extends CodedElementValidator {

	public CodedElementValidatorM(ConfigurableDetections detections, SimpleElementValidator simpleElementValidator) {
		super(detections, simpleElementValidator, false);
		// TODO Auto-generated constructor stub
	}

	public  Map<String, ArrayList<String>> getVSID(ValueSetSpec spec,
			ValueSetLibrary lib, String cs) throws ValueSetSpecException {

		String[] bindings = spec.valueSetId().split(":");
		int notFound = 0;
		Map<String, ArrayList<String>> map = new HashMap<String, ArrayList<String>>();
		for (String b : bindings) {
			try {
				String codeSys = checkCodeSys(b, lib);
				if (codeSys.equals("")) {
					throw new ValueSetSpecException(
							"Value Set Specification error, the value sets specified for multiple bindings should have codes from the same Code System");
				} else {
					if(map.containsKey(codeSys)){
						map.get(codeSys).add(b);
					}
					else {
						ArrayList<String> tmp = new ArrayList<String>();
						tmp.add(b);
						map.put(codeSys, tmp);
					}
						
				}
			} catch (ValueSetNotFoundException e) {
				notFound++;
			}
		}

		if (notFound == bindings.length) {
			throw new ValueSetSpecException(
					"Value Set Specification error, no value set was found from the list "
							+ spec.valueSetId());
		}

		return map;

	}

	public  String checkCodeSys(String vs, ValueSetLibrary library)
			throws ValueSetNotFoundException {

		ValueSet v = library.get(vs);
		String cs = "";
		for (Code c : v.codesList()) {
			if (cs == "") {
				cs = c.codeSys();
			} else if (!c.codeSys().equals(cs))
				return "";
		}
		return cs;

	}
	
	private  Entry checkPositionM(Complex c, int p, ValueSetSpec spec,
			ValueSetLibrary library, Map<String, ArrayList<String>> bindings) {
		try {
			Simple s1 = query(c, p);
			Simple s2 = query(c, p + 2); // FIXME Can make the error msg more explicit
			
			String codeSys = s2.value().raw();
			if(bindings.containsKey(codeSys)){
				ArrayList<String> bds = bindings.get(codeSys);
				String id = "";
				String delim = "";
				for(String binding : bds){
					id += delim + binding;
					delim = " or ";
					ValueSet vs = library.get(binding);
					Entry e = simpleElementValidator.checkValueSet(s1.location(), s1.value().raw(), vs, spec);
					if(pass(e)){
						return e;
					}
				}
				return Detections.codeNotFound(s1.location(), s1.value().raw(), id, spec);
				
			}
			else {
				String msg = "Code System : "+codeSys+", not found in any of the Value Sets bindings";
				return Detections.codedElem(s2.location(), msg, null);
			}
		} catch (Exception e) {
			return Detections.bindingLocation(c.location(), e.getMessage());
		}
	}
	
	
	private  List<Entry> checkXORM(Complex c, int p1, int p2, ValueSetLibrary library,
			ValueSetSpec spec, Map<String, ArrayList<String>> bindings) {
		
		List<Entry> detections = new ArrayList<Entry>();
		Entry e1 = checkPositionM(c, p1, spec, library, bindings);
		Entry e2 = checkPositionM(c, p2, spec, library, bindings);
		String bindingIds = "";
		
		for(String k : bindings.keySet()){
			bindingIds += k+" ";
		}
		
		if(!pass(e1) && !pass(e2)){
			detections.add(e1);
			detections.add(e2);
		}
		else if(pass(e1) && pass(e2)){
			
			if(e1 != null){
				detections.add(e1);
			}
			
			if(e2 != null){
				detections.add(e2);
			}
			
			String msg = "One of the triplet (but not both) should be valued from the"
					+ " value set '" + bindingIds + "'";
			detections.add(Detections.codedElem(c.location(), msg, null, spec, null));
		}
		else if(!pass(e1) && pass(e2)){
			
			if(e2 != null){
				detections.add(e2);
			}
			
			detections.add(Detections.toAlert(e1));
		}
		else if(pass(e1) && !pass(e2)){
			
			if(e1 != null){
				detections.add(e1);
			}
			
			detections.add(Detections.toAlert(e2));
		}
		
		return detections; 
	}
	
	public List<Entry> checkMultiple(Complex c, ValueSetSpec spec,
			ValueSetLibrary library) {
		try {

			// -- Get Map of Valid (CodeSys -> VS)
			Map<String, ArrayList<String>> map = getVSID(spec, library, spec.valueSetId());

			// -- Resolve Binding Location
			if (spec.bindingLocation().isEmpty())
				return listify(Detections.vsError(c.location(),
						"The binding location is missing"));

			BindingLocation bl = spec.bindingLocation().get();

			if (bl instanceof BindingLocation.Position) {
				int p = ((BindingLocation.Position) bl).value();
				return listify(checkPositionM(c, p, spec, library, map));
			} else if (bl instanceof BindingLocation.XOR) {
				int p1 = ((BindingLocation.XOR) bl).position1();
				int p2 = ((BindingLocation.XOR) bl).position2();
				return checkXORM(c, p1, p2, library, spec, map);

			} else {
				String msg = "Invalid binding location " + bl;
				return listify(Detections.vsError(c.location(), msg));
			}

		} catch (ValueSetSpecException e) {
			return listify(Detections.vsError(c.location(), e.getMessage()));
		}
	}
}
