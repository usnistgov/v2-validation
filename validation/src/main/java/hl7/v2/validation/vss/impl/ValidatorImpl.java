package hl7.v2.validation.vss.impl;

import gov.nist.validation.report.Entry;
import hl7.v2.instance.Complex;
import hl7.v2.instance.Element;
import hl7.v2.instance.Message;
import hl7.v2.instance.Simple;
import hl7.v2.profile.Req;
import hl7.v2.profile.ValueSetSpec;
import hl7.v2.validation.vss.Validator;
import hl7.v2.validation.vss.ValueSetLibrary;

import java.util.List;

/**
 * An implementation of the value set validator
 *
 * @author Salifou Sidi M. Malick <salifou.sidi@gmail.com>
 */
public class ValidatorImpl implements Validator {

    private ValueSetLibrary library;

    public ValidatorImpl(ValueSetLibrary library) {
        this.library = library;
    }

    @Override
    public ValueSetLibrary getValueSetLibrary() {
        return library;
    }

    @Override
    public List<Entry> checkValueSet(Message message) {
        List<Entry> result = new java.util.ArrayList<Entry>();
        checkValueSet(result, message.asGroup());
        return result;
    }

    @Override
    public Entry checkValueSet(Simple e, ValueSetSpec spec) {
        return SimpleElementValidator.check(e, spec, library);
    }

    @Override
    public Entry checkValueSet(Complex e, ValueSetSpec spec) {
        return ComplexElementValidator.check(e, spec, library);
    }

    private void checkValueSet(List<Entry> result, Element e) {
        if( e instanceof Simple ) {
            Entry x = checkValueSet((Simple) e, getSpec(e.req()));
            if( x != null )
                result.add(x);
        } else {
            Entry x = checkValueSet((Complex) e, getSpec(e.req()));
            if( x != null )
                result.add(x);

            // Check the children
            scala.collection.Iterator<Element> it = ((Complex) e).children().iterator();
            while( it.hasNext() )
                checkValueSet(result, it.next());
        }
    }

    private ValueSetSpec getSpec(Req req) {
        return req.vsSpec().isEmpty() ? null : req.vsSpec().head();
    }

}
