package hl7.v2.validation.vs;

import gov.nist.validation.report.Entry;
import hl7.v2.instance.Complex;
import hl7.v2.instance.Element;
import hl7.v2.instance.Message;
import hl7.v2.instance.Simple;
import hl7.v2.profile.Req;
import hl7.v2.profile.ValueSetSpec;

import java.util.ArrayList;
import java.util.List;

/**
 * An implementation of the value set validator
 *
 * @author Salifou Sidi M. Malick <salifou.sidi@gmail.com>
 */
public class Validator {

	public static List<Entry> listify(Entry e){
		List<Entry> detections = new ArrayList<Entry>();
		detections.add(e);
		return detections;
	}
	
    /**
     * Checks every data element value set and return the list of problem
     * @param message - The message to be validated
     * @param library - The value set library
     * @return The list of problem detected
     */
    public static List<Entry> checkValueSet(Message message, ValueSetLibrary library) {
        List<Entry> result = new java.util.ArrayList<Entry>();
        checkValueSet(result, message.asGroup(), library);
        return result;
    }

    /**
     * Checks the element against the value set specification and
     * return a report entry if a problem is found null otherwise
     * @param e       - The element to be validated
     * @param spec    - The value set specification
     * @param library - The value set library
     * @return A report entry if a problem is found null otherwise
     */
    public static Entry checkValueSet(Element e, ValueSetSpec spec,
                                      ValueSetLibrary library) {
        if( e instanceof Simple)
            return SimpleElementValidator.check((Simple) e, spec, library);
        else{
        	List<Entry> l = ComplexElementValidator.check((Complex) e, spec, library);
        	if( l != null && l.size() > 0)
        		return l.get(0);
        	else
        		return null;
        }
            
    }
    

    private static void checkValueSet(List<Entry> result, Element e, ValueSetLibrary library) {
        if( e instanceof Simple ) {
            Entry x = SimpleElementValidator.check((Simple) e, getSpec(e.req()), library);
            if( x != null )
                result.add(x);
        } else {
            List<Entry> l = ComplexElementValidator.check((Complex) e, getSpec(e.req()), library);
            if(l != null)
	            for(Entry x : l){
	            	if( x != null )
	                    result.add(x);
	            }
            // Check the children
            scala.collection.Iterator<Element> it = ((Complex) e).children().iterator();
            while( it.hasNext() )
                checkValueSet(result, it.next(), library);
        }
    }

    private static ValueSetSpec getSpec(Req req) {
        return req.vsSpec().isEmpty() ? null : req.vsSpec().head();
    }
}
