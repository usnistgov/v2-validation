package hl7.v2.validation.vs;

import static hl7.v2.validation.vs.SimpleElementValidator.checkValueSet;
import gov.nist.validation.report.Entry;
import gov.nist.validation.report.Trace;
import hl7.v2.instance.Complex;
import hl7.v2.instance.Simple;
import hl7.v2.profile.BindingLocation;
import hl7.v2.profile.ValueSetSpec;
import hl7.v2.validation.report.Detections;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CodedElementValidatorM extends CodedElementValidator {

	public static Map<String, String> getVSID(ValueSetSpec spec,
			ValueSetLibrary lib, String cs) throws ValueSetSpecException {

		String[] bindings = spec.valueSetId().split(":");
		int notFound = 0;
		Map<String, String> map = new HashMap<String, String>();
		for (String b : bindings) {
			try {
				String codeSys = checkCodeSys(b, lib);
				if (codeSys.equals("")) {
					throw new ValueSetSpecException(
							"Value Set Specification error, the value sets specified for multiple bindings should have codes from the same Code System");
				} else {
					map.put(codeSys, b);
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

	public static String checkCodeSys(String vs, ValueSetLibrary library)
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
	
	private static Entry checkPositionM(Complex c, int p, ValueSetSpec spec,
			ValueSetLibrary library, Map<String, String> bindings) {
		try {
			Simple s1 = query(c, p);
			Simple s2 = query(c, p + 2); // FIXME Can make the error msg more explicit
			
			String codeSys = s2.value().raw();
			if(bindings.containsKey(codeSys)){
				ValueSet vs = library.get(bindings.get(codeSys));
				return checkValueSet(s1.location(), s1.value().raw(), vs, spec);
			}
			else {
				String msg = "Code System : "+codeSys+", not found in any of the Value Sets bindings";
				return Detections.codedElem(s2.location(), msg, null);
			}
		} catch (Exception e) {
			return Detections.vsError(c.location(), e.getMessage());
		}
	}
	
	private static Entry checkXORM(Complex c, int p1, int p2, ValueSetLibrary library,
			ValueSetSpec spec, Map<String, String> bindings) {
		Entry e1 = checkPositionM(c, p1, spec, library, bindings);
		Entry e2 = checkPositionM(c, p2, spec, library, bindings);
		if ((e1 == null && e2 != null) || (e1 != null && e2 == null))
			return null; // No detection

		List<String> reasons = new ArrayList<String>();
		if (e1 != null)
			reasons.add(e1.getDescription());
		if (e2 != null)
			reasons.add(e2.getDescription());

		List<Trace> stack = Arrays.asList(new Trace("", reasons));

		String msg = "One of the triplet (but not both) should be valued from one of the specified value sets"
				+ spec.valueSetId();
		return Detections.codedElem(c.location(), msg, stack);
	}
	
	public static Entry checkMultiple(Complex c, ValueSetSpec spec,
			ValueSetLibrary library) {
		try {

			// -- Get Map of Valid (CodeSys -> VS)
			Map<String, String> map = getVSID(spec, library, spec.valueSetId());

			// -- Resolve Binding Location
			if (spec.bindingLocation().isEmpty())
				return Detections.vsError(c.location(),
						"The binding location is missing");

			BindingLocation bl = spec.bindingLocation().get();

			if (bl instanceof BindingLocation.Position) {
				int p = ((BindingLocation.Position) bl).value();
				return checkPositionM(c, p, spec, library, map);
			} else if (bl instanceof BindingLocation.XOR) {
				int p1 = ((BindingLocation.XOR) bl).position1();
				int p2 = ((BindingLocation.XOR) bl).position2();
				return checkXORM(c, p1, p2, library, spec, map);

			} else {
				String msg = "Invalid binding location " + bl;
				return Detections.vsError(c.location(), msg);
			}

		} catch (ValueSetSpecException e) {
			return Detections.vsError(c.location(), e.getMessage());
		}
	}
}
