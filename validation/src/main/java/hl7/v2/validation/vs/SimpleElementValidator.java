package hl7.v2.validation.vs;

import gov.nist.validation.report.Entry;
import hl7.v2.instance.Location;
import hl7.v2.instance.Simple;
import hl7.v2.profile.BindingStrength;
import hl7.v2.profile.ValueSetSpec;
import hl7.v2.validation.report.ConfigurableDetections;

/**
 * Module for validating simple element against a value set specification
 *
 * @author Salifou Sidi M. Malick <salifou.sidi@gmail.com>
 */
public class SimpleElementValidator extends ConfigurableValidation {

    public SimpleElementValidator(ConfigurableDetections detections) {
		super(detections);
		// TODO Auto-generated constructor stub
	}

	/**
     * Checks the simple element against the value set specification
     * and returns a detection if a problem is found, null otherwise
     * @param e       - The simple element to be validated
     * @param spec    - The value set specification
     * @param library - The value set library to be used
     * @return A detection if a problem is found, null otherwise
     */
    public  Entry check(Simple e, ValueSetSpec spec,
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
    public  Entry check(Location location, String value, ValueSetSpec spec,
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

    public  Entry checkValueSet(Location location, String value, ValueSet vs,
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
    private  Entry checkCode(Location location, String value, ValueSet vs,
                                   ValueSetSpec spec) {
        java.util.List<Code> codes = vs.getCodes(value);
        int nbOfCodes = codes.size();
        if( nbOfCodes == 0 ) {
            Extensibility ext = vs.extensibility() != null ? vs.extensibility().getOrElse(() -> null) : null;
            Stability sta = vs.stability() != null ? vs.stability().getOrElse(() -> null) : null;

//            BindingStrength bs = spec != null ? spec.bindingStrength() != null ? spec.bindingStrength().getOrElse(null) : null : null;

            return Detections.codeNotFound(location, value, vs.id(), sta, ext, null);
        }
        if( nbOfCodes == 1)
            return checkCodeUsage(location, value, codes.get(0), vs, spec);

        String msg = "Multiple occurrences of the code '"+value+"' found.";
        return Detections.vsError(location, msg, vs, spec);
    }

    /**
     * Checks the code usage and return a detection if any otherwise return null
     */
    private  Entry checkCodeUsage(Location location, String value, Code code,
                                        ValueSet vs, ValueSetSpec spec) {
        Extensibility ext = vs.extensibility() != null ? vs.extensibility().getOrElse(() -> null) : null;
        Stability sta = vs.stability() != null ? vs.stability().getOrElse(() -> null) : null;
//        BindingStrength bs = spec != null ? spec.bindingStrength() != null ? spec.bindingStrength().getOrElse(null) : null : null;

    	 if( code.usage() instanceof CodeUsage.E$ )
             return new EnhancedEntry(Detections.vsCodeFound("evs", location, value, vs.id(), sta, ext, null),false);
         if( code.usage() instanceof CodeUsage.P$ )
             return new EnhancedEntry(Detections.vsCodeFound("pvs", location, value, vs.id(), sta, ext, null),true);
        if( code.usage() instanceof CodeUsage.R$ )
             return new EnhancedEntry(Detections.vsCodeFound("rvs", location, value, vs.id(), sta, ext, null),true);
        return null;
    }

    /**
     * Returns true if the code check should be skipped
     */
    private  boolean skipCodeCheck(String vsID, String value) {
        return vsID.matches("(HL7)?0396(_[a-zA-Z0-9]+)?") &&
                (value.matches("HL7[0-9]{4}") || value.matches("99[a-zA-Z0-9]{3}"));
    }

}
