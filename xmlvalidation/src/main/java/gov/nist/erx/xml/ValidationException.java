package gov.nist.erx.xml;

/**
 * Created by mcl1 on 11/3/15.
 */
public class ValidationException extends Exception {

    private String message;
    public ValidationException(String msg) {
        super();
        this.message = msg;
    }

    @Override
    public void printStackTrace() {

        super.printStackTrace();
    }
}
