package hl7.v2.validation.vs;

public class EmptyValueSetLibrary implements ValueSetLibrary {

    private static EmptyValueSetLibrary ourInstance = new EmptyValueSetLibrary();

    public static EmptyValueSetLibrary getInstance() {
        return ourInstance;
    }

    private EmptyValueSetLibrary() {}

    @Override
    public Boolean isExcludedFromTheValidation(String id) {
        return false;
    }

    @Override
    public ValueSet get(String id) throws ValueSetNotFoundException {
        throw new ValueSetNotFoundException(id);
    }

    @Override
    public boolean containsLegacy0396Codes() {
        return false;
    }
}
