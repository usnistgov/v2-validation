package hl7.v2.validation.vss;

/**
 * Exception thrown when the value set cannot be found in the library
 *
 * @author Salifou Sidi M. Malick <salifou.sidi@gmail.com>
 */
public class ValueSetNotFoundException extends Exception {

    public ValueSetNotFoundException(String id) {
        super("The value set '"+id+"' cannot be found in the library");
    }
}
