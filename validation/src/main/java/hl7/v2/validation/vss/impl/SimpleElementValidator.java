package hl7.v2.validation.vss.impl;

import gov.nist.validation.report.Entry;
import hl7.v2.instance.Location;
import hl7.v2.instance.Simple;
import hl7.v2.profile.ValueSetSpec;
import hl7.v2.validation.report.Detections;
import hl7.v2.validation.vs.Code;
import hl7.v2.validation.vs.CodeUsage;
import hl7.v2.validation.vs.ValueSet;
import hl7.v2.validation.vss.ValueSetLibrary;
import hl7.v2.validation.vss.ValueSetNotFoundException;

public class SimpleElementValidator {

    /**
     * Checks the simple element against the value set specification
     * and a detection if a problem if found, null otherwise
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
     * Checks the value against the value set specification
     * and a detection if a problem if found, null otherwise
     * @param location - The location
     * @param value    - The value to be validated
     * @param spec     - The value set specification
     * @param library  - The value set library
     * @return A detection if a problem is found, null otherwise
     */
    public static Entry check(Location location, String value, ValueSetSpec spec,
                              ValueSetLibrary library) {

        // Return a detection if the value set is excluded from the validation
        if( library.isExcludedFromTheValidation( spec.valueSetId() ) )
            return Detections.vsNoVal(location, spec.valueSetId());

        try {
            ValueSet vs = library.get( spec.valueSetId() );
            // Return a detection in the value set is empty
            if( vs.isEmpty() ) return Detections.emptyVS(location, vs, spec);

            // Check if we should stop the validation here
            if( skipCodeCheck(vs.id(), value) ) return null;

            return checkCode(location, value, vs, spec);

        } catch ( ValueSetNotFoundException e) {
            return Detections.vsNotFound(location, value, spec);
        }
    }

    /**
     * Checks the code and return a detection if any otherwise return null
     */
    private static Entry checkCode(Location location, String value, ValueSet vs,
                                   ValueSetSpec spec) {
        java.util.List<Code> codes = vs.getCodes(value);
        if( codes.isEmpty() )
            return Detections.codeNotFound(location, value, vs, spec);
        else
            return checkCodeUsage(location, value, codes.get(0), vs, spec);
    }

    /**
     * Checks the code usage and return a detection if any otherwise return null
     */
    private static Entry checkCodeUsage(Location location, String value, Code code,
                                        ValueSet vs, ValueSetSpec spec) {
        if( code.usage() instanceof CodeUsage.E$ )
            return Detections.evs(location, value, vs, spec);
        if( code.usage() instanceof CodeUsage.P$ )
            return Detections.pvs(location, value, vs, spec);
         return null;
    }

    /**
     * Returns true if the code check should be skipped
     */
    private static boolean skipCodeCheck(String vsID, String value) {
        if( vsID.matches("(HL7)?0396") ) {
            if( value.matches("HL7[0-9]{4}") )
                return vsID.contains("HL7nnnn");
            if( value.matches("99[a-zA-Z0-9]{3}") )
                return vsID.contains("99ZZZ");
        }
        return false;
    }

}
