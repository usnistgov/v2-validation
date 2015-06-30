package hl7.v2.validation.vss.impl;

import gov.nist.validation.report.Entry;
import hl7.v2.instance.Complex;
import hl7.v2.profile.ValueSetSpec;
import hl7.v2.validation.vss.ValueSetLibrary;

/**
 * Module for validating complex element against a value set specification
 *
 * @author Salifou Sidi M. Malick <salifou.sidi@gmail.com>
 */
public class ComplexElementValidator {

    /**
     * Checks the simple element against the value set specification
     * and a detection if a problem if found, null otherwise
     * @param e       - The complex element to be validated
     * @param spec    - The value set specification
     * @param library - The value set library to be used
     * @return A detection if a problem is found, null otherwise
     */
    public static Entry check(Complex e, ValueSetSpec spec, ValueSetLibrary library) {
        if( spec == null )
            return null;

        return null; //FIXME;
    }
}
