package hl7.v2.validation.vs;

import gov.nist.validation.report.Entry;
import hl7.v2.instance.Complex;
import hl7.v2.instance.Element;
import hl7.v2.instance.Message;
import hl7.v2.instance.Simple;
import hl7.v2.profile.Req;
import hl7.v2.profile.Usage;
import hl7.v2.profile.ValueSetSpec;
import hl7.v2.validation.report.ConfigurableDetections;

import java.util.ArrayList;
import java.util.List;

/**
 * An implementation of the value set validator
 *
 * @author Salifou Sidi M. Malick <salifou.sidi@gmail.com>
 */
public class Validator extends ConfigurableValidation {

	ComplexElementValidator complexElementValidator;
	SimpleElementValidator simpleElementValidator;
	
	public Validator(ConfigurableDetections detections) {
		super(detections);
		this.simpleElementValidator = new SimpleElementValidator(detections);
		this.complexElementValidator = new ComplexElementValidator(detections, this.simpleElementValidator);
	}

	public  List<Entry> listify(Entry e){
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
    public  List<Entry> checkValueSet(Message message, ValueSetLibrary library) {
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
    public  Entry checkValueSet(Element e, ValueSetSpec spec, ValueSetLibrary library) {
    	Entry etr = null;
        if( e instanceof Simple)
            etr = simpleElementValidator.check((Simple) e, spec, library);
        else if(e instanceof Complex){
        	List<Entry> l = complexElementValidator.check((Complex) e, spec, library);
        	if( l != null && l.size() > 0)
        		etr = l.get(0);
        }

        if(etr == null || (etr instanceof EnhancedEntry && ((EnhancedEntry) etr).isOk()) ){
        	return null;
        }
        else {
        	return etr;
        }
            
    }
    

    private  void checkValueSet(List<Entry> result, Element e, ValueSetLibrary library) {
    	ValueSetSpec spec = getSpec(e.req());
    	
    	if(spec != null && e.req().usage() instanceof Usage.O$ ){
    		return;
    	}
    	
    	
    	if( e instanceof Simple ) {
            Entry x = simpleElementValidator.check((Simple) e, getSpec(e.req()), library);
            
            if( x != null ){
            	if(e.req().usage() instanceof Usage.O$)
        			result.add(Detections.toAlert(x));
        		else
        			result.add(checkExtensibility(spec, library, x));      
            }
            
        } 
    	else if(e instanceof Complex) {
            List<Entry> l = complexElementValidator.check((Complex) e, getSpec(e.req()), library);
            if(l != null)
            	for(Entry x : l){
	            	if( x != null )
	            		if(e.req().usage() instanceof Usage.O$)
	            			result.add(Detections.toAlert(x));
	            		else
	            			result.add(checkExtensibility(spec, library, x));      	
	            }
            // Check the children
            scala.collection.Iterator<Element> it = ((Complex) e).children().iterator();
            while( it.hasNext() )
                checkValueSet(result, it.next(), library);
        }
    }
    
    public  Entry checkExtensibility(ValueSetSpec spec, ValueSetLibrary library, Entry e){
    	try {
            ValueSet vs = library.get( spec.valueSetId());
            Extensibility ex = vs.extensibility().get();
        	
        	if(ex instanceof Extensibility.Open$){
        		return Detections.toAlert(e);
        	}
        	else {
        		return e;
        	}
        } catch (Exception exp) {
            return e;
        }
    }

    private  ValueSetSpec getSpec(Req req) {
        return req.vsSpec().isEmpty() ? null : req.vsSpec().head();
    }
}
