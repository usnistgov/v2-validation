package hl7.v2.validation.vs;

import gov.nist.validation.report.Entry;
import hl7.v2.instance.*;
import hl7.v2.profile.BindingLocation;
import hl7.v2.profile.Datatype;
import hl7.v2.profile.ValueSetSpec;
import hl7.v2.validation.report.Detections;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import static hl7.v2.validation.vs.SimpleElementValidator.checkValueSet;

/**
 * Module providing coded element value set validation logic.
 *
 * @author Salifou Sidi M. Malick <salifou.sidi@gmail.com>
 */
public class CodedElementValidator {

	/**
	 * @return True if the data type represent a coded element
	 */
	public static boolean isCodedElement(Datatype d) {
		return isCodedElement(d.name());
	}

	/**
	 * @return True if the data type represent a coded element
	 */
	public static boolean isCodedElement(String datatype) {
		return datatype.matches("C(W|N)?E");
	}

	/**
	 * @return True if the complex element is a coded element
	 */
	public static boolean isCodedElement(Complex e) {
		if (e instanceof ComplexField)
			return isCodedElement(((ComplexField) e).datatype());

		return e instanceof ComplexComponent
				&& isCodedElement(((ComplexComponent) e).datatype());
	}

	public static List<Entry> listify(Entry e){
		
		if(e instanceof TripletEntry)
			return ((TripletEntry) e).asList();
		else {
			List<Entry> detections = new ArrayList<Entry>();
			detections.add(e);
			return detections;
		}
		
	}
	
	public static List<Entry> check(Complex c, ValueSetSpec spec,
			ValueSetLibrary library) {
		
		if (!isCodedElement(c))
			return null;

		if (spec.valueSetId().contains(":")) {
			return CodedElementValidatorM.checkMultiple(c, spec, library);
		}

		// Return a detection if the value set is excluded from the validation
		if (library.isExcludedFromTheValidation(spec.valueSetId())){
			return listify(Detections.vsNoVal(c.location(), spec.valueSetId()));
		}

		try {
			ValueSet vs = library.get(spec.valueSetId());

			if (vs.isEmpty())
				return listify(Detections.emptyVS(c.location(), vs, spec));

			if (spec.bindingLocation().isEmpty())
				return listify(Detections.vsError(c.location(),
						"The binding location is missing", vs, spec));

			BindingLocation bl = spec.bindingLocation().get();

			if (bl instanceof BindingLocation.Position) {
				int p = ((BindingLocation.Position) bl).value();
				return listify(checkPosition(c, p, vs, spec));
			} else if (bl instanceof BindingLocation.XOR) {
				int p1 = ((BindingLocation.XOR) bl).position1();
				int p2 = ((BindingLocation.XOR) bl).position2();
				return checkXOR(c, p1, p2, vs, spec);
			} else {
				String msg = "Invalid binding location " + bl;
				return listify(Detections.vsError(c.location(), msg, vs, spec));
			}

		} catch (ValueSetNotFoundException e) {
			String msg = "Value set '" + spec.valueSetId()
					+ "' cannot be found in the library";
			return listify(Detections.codedElem(c.location(), msg, null, spec, null));
		}
	}

	private static List<Entry> checkXOR(Complex c, int p1, int p2, ValueSet vs,
			ValueSetSpec spec) {
		List<Entry> detections = new ArrayList<Entry>();
		Entry e1 = checkPosition(c, p1, vs, spec);
		Entry e2 = checkPosition(c, p2, vs, spec);
		if(!pass(e1) && !pass(e2)){
			addEntry(detections,e1);
			addEntry(detections,e2);
		}
		else if(pass(e1) && pass(e2)){
			
			if(hasEntry(e1)){
				addEntry(detections,e1);
			}
			
			if(hasEntry(e2)){
				addEntry(detections,e2);
			}
			
			String msg = "One of the triplet (but not both) should be valued from the"
					+ " value set '" + vs.id() + "'";
			detections.add(Detections.codedElem(c.location(), msg, vs, spec, null));
		}
		else if(!pass(e1) && pass(e2)){
			System.out.println("Pass 1");
			
			if(hasEntry(e2)){
				addEntry(detections,e2);
			}
			
			addEntry(detections,Detections.toAlert(e1));
		}
		else if(pass(e1) && !pass(e2)){
			System.out.println("Pass 2");
			if(hasEntry(e1)){
				addEntry(detections,e1);
			}
			
			addEntry(detections,Detections.toAlert(e2));
		}
		
		return detections; 
	}
	
	protected static void addEntry(List<Entry> l, Entry e){
		if(e != null){
			if(e instanceof TripletEntry){
				l.addAll(((TripletEntry) e).asList());
			}
			else {
				l.add(e);
			}
		}
	}
	
	protected static boolean hasEntry(Entry e){
		if(e instanceof TripletEntry){
			return ((TripletEntry) e).asList().size() > 0;
		}
		else {
			return e != null;
		}
	}
	
	protected static boolean pass(Entry e){
		if(e == null)
			return true;
		else {
			
			if(e instanceof EnhancedEntry){
				return ((EnhancedEntry) e).isOk();
			}
			else if(e instanceof TripletEntry){
				return ((TripletEntry) e).isValid();
			}
			else
				return false;
		}
	}

	private static Entry checkPosition(Complex c, int p, ValueSet vs,
			ValueSetSpec spec) {
		try {
			
			Simple s1 = query(c, p);
			Simple s2 = query(c, p + 2); // FIXME Can make the error msg more
											// explicit
			return checkTriplet(s1, s2, vs, spec);
		} catch (Exception e) {
			System.out.println("ERR "+e.getMessage());
			e.printStackTrace();
			return Detections.bindingLocation(c.location(), e.getMessage(), vs, spec);
		}
	}

	/**
	 * Checks the triplet and return a detection if any
	 */
	private static TripletEntry checkTriplet(Simple s1, Simple s2, ValueSet vs,
			ValueSetSpec spec) {
		TripletEntry te = new TripletEntry();
		
		Entry v = checkValueSet(s1.location(), s1.value().raw(), vs, spec);
		Entry c = null;
		if(pass(v)){
			c = checkCodeSys(s2, vs.getCodes(s1.value().raw()).get(0), vs, spec);
		}
		
		te.setValue(v);
		te.setCodeSys(c);
		
		return te;
	}

	/**
	 * Checks the code system and return a detection if any
	 */
	private static Entry checkCodeSys(Simple s, Code code, ValueSet vs,
			ValueSetSpec spec) {
		if (s.value().raw().equals(code.codeSys()))
			return null;

		String msg = "Invalid Code System. Expected: '" + code.codeSys()
				+ "', Found: '" + s.value().raw() + "'";
		return Detections.codedElem(s.location(), msg, vs, spec, null);
	}

	public static Simple query(Complex c, int position) throws Exception {
		String details;
		try {		
			scala.collection.immutable.List<Simple> l = Query.queryAsSimple(c,
					position + "[1]").get();
			
			
			int count = l.size();
			if (count == 1)
				return l.head();
			details = "Querying " + c.location().prettyString()
					+ " for the position '" + position + "' returned " + count
					+ " element(s)";
		} catch (Exception e) {
			details = e.getMessage();
		}
		throw new Exception(
				"An error occurred while resolving the binding location. Detail: "
						+ details);
	}
}
