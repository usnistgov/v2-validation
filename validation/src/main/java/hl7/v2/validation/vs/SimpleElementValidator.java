package hl7.v2.validation.vs;

import gov.nist.validation.report.Entry;
import hl7.v2.instance.Location;
import hl7.v2.instance.Simple;
import hl7.v2.profile.ValueSetSpec;
import hl7.v2.validation.report.Detections;

/**
 * Module for validating simple element against a value set specification
 *
 * @author Salifou Sidi M. Malick <salifou.sidi@gmail.com>
 */
public class SimpleElementValidator {

    /**
     * Checks the simple element against the value set specification
     * and returns a detection if a problem is found, null otherwise
     * @param e       - The simple element to be validated
     * @param spec    - The value set specification
     * @param library - The value set library to be used
     * @return A detection if a problem is found, null otherwise
     */
    public static Entry check(Simple e, ValueSetSpec spec,
                              ValueSetLibrary library) {
        if( e.value().isNull() )
            return null;
        return check(e.location(), e.value().raw(), spec, library);
    }

    /**
     * Checks the value against the value set specification and
     * returns a detection if a problem is found, null otherwise
     * @param location - The location
     * @param value    - The value to be validated
     * @param spec     - The value set specification
     * @param library  - The value set library
     * @return A detection if a problem is found, null otherwise
     */
    public static Entry check(Location location, String value, ValueSetSpec spec,
                              ValueSetLibrary library) {
    	
        if( spec == null )
            return null;
        
        if(spec.valueSetId().contains(":")){
        	return Detections.vsError(location, "Value Set Specification error, multiple bindings can only be specified for CodedElements");
        }
    		

        // Return a detection if the value set is excluded from the validation
        if( library.isExcludedFromTheValidation( spec.valueSetId() ) )
            return Detections.vsNoVal(location, spec.valueSetId());

        try {
            ValueSet vs = library.get( spec.valueSetId() );
            return checkValueSet(location, value, vs, spec);
        } catch ( ValueSetNotFoundException e) {
            return Detections.vsNotFound(location, value, spec);
        }
    }

    public static Entry checkValueSet(Location location, String value, ValueSet vs,
                              ValueSetSpec spec) {
        if( vs.isEmpty() )
            return Detections.emptyVS(location, vs, spec);

        if( skipCodeCheck(vs.id(), value) )
            return null;

        return checkCode(location, value, vs, spec);
    }

    /**
     * Checks the code and return a detection if any otherwise return null
     */
    private static Entry checkCode(Location location, String value, ValueSet vs,
                                   ValueSetSpec spec) {
        java.util.List<Code> codes = vs.getCodes(value);
        int nbOfCodes = codes.size();

        if( nbOfCodes == 0 )
            return Detections.codeNotFound(location, value, vs, spec);

        if( nbOfCodes == 1)
            return checkCodeUsage(location, value, codes.get(0), vs, spec);

        String msg = "Multiple occurrences of the code '"+value+"' found.";
        return Detections.vsError(location, msg, vs, spec);
    }

    /**
     * Checks the code usage and return a detection if any otherwise return null
     */
    private static Entry checkCodeUsage(Location location, String value, Code code,
                                        ValueSet vs, ValueSetSpec spec) {
    	 if( code.usage() instanceof CodeUsage.E$ )
             return new EnhancedEntry(Detections.evs(location, value, vs, spec),true);
         if( code.usage() instanceof CodeUsage.P$ )
             return new EnhancedEntry(Detections.pvs(location, value, vs, spec),true);
         return null;
    }

    /**
     * Returns true if the code check should be skipped
     */
    private static boolean skipCodeCheck(String vsID, String value) {
        return vsID.matches("(HL7)?0396(_[a-zA-Z0-9]+)?") &&
                (value.matches("HL7[0-9]{4}") || value.matches("99[a-zA-Z0-9]{3}"));
    }

}
