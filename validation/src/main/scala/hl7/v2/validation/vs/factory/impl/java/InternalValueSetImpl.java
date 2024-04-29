package hl7.v2.validation.vs.factory.impl.java;

import hl7.v2.validation.vs.Code;
import hl7.v2.validation.vs.InternalValueSet;
import hl7.v2.validation.vs.ValueSet;
import hl7.v2.validation.vs.factory.ValueSetFacade;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

public class InternalValueSetImpl extends ValueSetFacade {
	InternalValueSet valueSet;

	public InternalValueSetImpl(InternalValueSet value) {
		this.valueSet = value;
	}

	@Override
	public ValueSet getValueSet() {
		return valueSet;
	}

	@Override
	public List<Code> getCodeMatches(String value) {
		return this.valueSet.codesAsJava().stream().filter((code) -> codeMatch(code, value)).collect(Collectors.toList());
	}

	@Override
	public boolean contains(String value) {
		return this.valueSet.codes().exists(code -> codeMatch(code, value));
	}

	@Override
	public List<Code> getCodeList() {
		return this.valueSet.codesAsJava();
	}

	private boolean codeMatch(Code code, String value) {
		if(code.pattern().isDefined()) {
			return value.matches(code.pattern().get());
		} else {
			return StringUtils.equals(code.value(), value);
		}
	}
}