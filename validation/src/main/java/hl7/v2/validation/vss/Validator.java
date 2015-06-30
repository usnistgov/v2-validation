package hl7.v2.validation.vss;

import gov.nist.validation.report.Entry;
import hl7.v2.instance.Complex;
import hl7.v2.instance.Simple;
import hl7.v2.instance.Message;
import hl7.v2.profile.ValueSetSpec;

/**
 * Interface describing the value set validator
 *
 * @author Salifou Sidi M. Malick <salifou.sidi@gmail.com>
 */
public interface Validator {

    /**
     * @return The value set library used by this validator
     */
    public ValueSetLibrary getValueSetLibray();

    /**
     * Checks every data element value set and return the list of problem
     * @param message - The message to be validated
     * @return The list of problem detected
     */
    public java.util.List<Entry> checkValueSet(Message message);

    /**
     * Checks the simple element against the value set specification and
     * return a report entry if a problem is found null otherwise
     * @param e    - The simple element to be validated
     * @param spec - The value set specification
     * @return A report entry if a problem is found null otherwise
     */
    public Entry checkValueSet(Simple e, ValueSetSpec spec);

    /**
     * Checks the complex element against the value set specification and
     * return a report entry if a problem is found null otherwise
     * @param e    - The complex element to be validated
     * @param spec - The value set specification
     * @return A report entry if a problem is found null otherwise
     */
    public Entry checkValueSet(Complex e, ValueSetSpec spec);
}
