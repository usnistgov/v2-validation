package hl7.v2.validation.vs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import gov.nist.validation.report.Entry;
import gov.nist.validation.report.Trace;
import hl7.v2.instance.Complex;
import hl7.v2.instance.Query;
import hl7.v2.instance.Simple;
import hl7.v2.profile.BindingLocation;
import hl7.v2.profile.ValueSetSpec;
import hl7.v2.validation.report.Detections;

/**
 * Module for validating complex element against a value set specification
 *
 * @author Salifou Sidi M. Malick <salifou.sidi@gmail.com>
 */
public class ComplexElementValidator {

	public static List<Entry> listify(Entry e){
		List<Entry> detections = new ArrayList<Entry>();
		detections.add(e);
		return detections;
	}
	
	/**
	 * Checks the complex element against the value set specification and
	 * returns a detection if a problem is found, null otherwise
	 * 
	 * @param e
	 *            - The complex element to be validated
	 * @param spec
	 *            - The value set specification
	 * @param library
	 *            - The value set library to be used
	 * @return A detection if a problem is found, null otherwise
	 */
	public static List<Entry> check(Complex e, ValueSetSpec spec,
			ValueSetLibrary library) {
		if (spec == null)
			return null;

		if (CodedElementValidator.isCodedElement(e))
			return CodedElementValidator.check(e, spec, library);
		else {

			if (spec.valueSetId().contains(":")) {
				return listify(Detections
						.vsError(
								e.location(),
								"Value Set Specification error, multiple bindings can only be specified for Coded Elements"));
			}

			if (library.isExcludedFromTheValidation(spec.valueSetId()))
				return listify(Detections.vsNoVal(e.location(), spec.valueSetId()));

			try {
				ValueSet vs = library.get(spec.valueSetId());

				if (vs.isEmpty())
					return listify(Detections.emptyVS(e.location(), vs, spec));

				if (spec.bindingLocation().isEmpty())
					return listify(Detections.vsError(e.location(),
							"The binding location is missing", vs, spec));

				BindingLocation bl = spec.bindingLocation().get();

				if (bl instanceof BindingLocation.Position) {
					int p = ((BindingLocation.Position) bl).value();
					return listify(checkPosition(e, p, vs, spec));
				} else if (bl instanceof BindingLocation.XOR) {
					int p1 = ((BindingLocation.XOR) bl).position1();
					int p2 = ((BindingLocation.XOR) bl).position2();
					return checkXOR(e, p1, p2, vs, spec);
				} else {
					String msg = "Invalid binding location " + bl;
					return listify(Detections.vsError(e.location(), msg, vs, spec));
				}

			} catch (ValueSetNotFoundException er) {
				String msg = "Value set '" + spec.valueSetId()
						+ "' cannot be found in the library";
				return listify(Detections
						.codedElem(e.location(), msg, null, spec, null));
			}
		}
	}

	private static List<Entry> checkXOR(Complex c, int p1, int p2, ValueSet vs,
			ValueSetSpec spec) {
		List<Entry> detections = new ArrayList<Entry>();
		Entry e1 = checkPosition(c, p1, vs, spec);
		Entry e2 = checkPosition(c, p2, vs, spec);
		
		if (e2 != null){
			detections.add(e2);
		}
		
		if(e1 != null){
			detections.add(e1);
		}
		
		if(e1 == null && e2 == null){
			String msg = "One of the triplet (but not both) should be valued from the"
					+ " value set '" + vs.id() + "'";
			detections.add(Detections.codedElem(c.location(), msg, vs, spec, null));
		}
		
		return detections; // No detection
	}

	private static Entry checkPosition(Complex c, int p, ValueSet vs,
			ValueSetSpec spec) {
		try {
			Simple s1 = CodedElementValidator.query(c, p);
			return SimpleElementValidator.checkValueSet(s1.location(), s1
					.value().raw(), vs, spec);
		} catch (Exception e) {
			return Detections.bindingLocation(c.location(), e.getMessage(), vs, spec);
		}
	}

}
