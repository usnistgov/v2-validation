package hl7.v2.validation.vss;

import hl7.v2.validation.vs.ValueSet;

/**
 * The value set library
 *
 * @author Salifou Sidi M. Malick <salifou.sidi@gmail.com>
 */
public interface ValueSetLibrary {

    /**
     * Returns true if the value set with the specified
     * id is excluded from the validation
     * @param id - The value set if
     * @return True if the value set is excluded from the validation
     */
    public Boolean isExcludedFromTheValidation(String id);

    /**
     * Returns the value set with the specified id
     * @param id - The id of the value set
     * @return The value set with the specified id
     * @throws ValueSetNotFoundException if the value set is not in the library
     */
    public ValueSet get(String id) throws ValueSetNotFoundException;

}
