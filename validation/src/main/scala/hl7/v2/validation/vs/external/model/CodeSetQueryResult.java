package hl7.v2.validation.vs.external.model;

import java.util.List;

public class CodeSetQueryResult {
	private String id;
	private String name;
	private Version latestStableVersion;
	private Version version;
	private boolean isLatestStable;
	private String codeMatchValue;
	private List<ExternalCode> codes;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Version getLatestStableVersion() {
		return latestStableVersion;
	}

	public void setLatestStableVersion(Version latestStableVersion) {
		this.latestStableVersion = latestStableVersion;
	}

	public Version getVersion() {
		return version;
	}

	public void setVersion(Version version) {
		this.version = version;
	}

	public boolean isLatestStable() {
		return isLatestStable;
	}

	public void setLatestStable(boolean latestStable) {
		isLatestStable = latestStable;
	}

	public String getCodeMatchValue() {
		return codeMatchValue;
	}

	public void setCodeMatchValue(String codeMatchValue) {
		this.codeMatchValue = codeMatchValue;
	}

	public List<ExternalCode> getCodes() {
		return codes;
	}

	public void setCodes(List<ExternalCode> codes) {
		this.codes = codes;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
