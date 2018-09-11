package hl7.v2.validation.plugin;

import hl7.v2.instance.Element;

public interface BooleanValidationPlugin {

	public boolean assertion(Element e);
	
}
